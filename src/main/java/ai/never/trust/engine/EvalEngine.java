package ai.never.trust.engine;

import ai.never.trust.judge.Judge;
import ai.never.trust.judge.JudgeFactory;
import ai.never.trust.judge.JudgeNormalizer;
import ai.never.trust.metric.Metric;
import ai.never.trust.metric.MetricRegistry;
import ai.never.trust.model.*;

import java.util.*;

/**
 * Orchestrates the full evaluation run.
 *
 * Cost-efficient flow:
 *
 *   1. Run the agent N times → collect all responses
 *   2. Pick a sample of min(judgeRuns, N) responses → send to judge
 *   3. From judge results: pick the best-scored response as the reference
 *   4. Score the remaining (non-judged) runs via text similarity vs the reference
 *   5. Merge judge scores + similarity scores → unified TestResult list
 *   6. Compute metrics, detect incidents, build report
 *
 * Why this works:
 *   - Accuracy: measured on the judge sample (statistically valid for N ≥ 30)
 *   - Consistency: measured across ALL N runs via pairwise similarity (free)
 *   - Hallucination: measured on the judge sample (Jev returns noul probability)
 *   - Cost: judge called judgeRuns times (default 10), not N times
 */
public class EvalEngine {

    private final AgentCaller agentCaller = new AgentCaller();
    private final JudgeNormalizer normalizer = new JudgeNormalizer();

    public EvalReport run(EvalConfig config) {
        System.out.println("\n╔══════════════════════════════════════════╗");
        System.out.println("║          DeadOrEval — Starting           ║");
        System.out.println("╚══════════════════════════════════════════╝");
        System.out.println("  Eval:    " + config.getName());
        System.out.println("  Agent:   " + (config.getTestedAgent() != null ? config.getTestedAgent().getUrl() : "N/A"));
        System.out.println("  Judges:  " + (config.getJudges() != null ? config.getJudges().size() : 0));
        System.out.println("  Tests:   " + (config.getTests() != null ? config.getTests().size() : 0));
        System.out.println();

        List<Judge> judges = buildJudges(config);
        if (judges.isEmpty()) {
            throw new IllegalStateException("No judges configured or available.");
        }

        List<Metric> metrics = MetricRegistry.build(config.getMetrics());

        List<TestCaseReport> testCaseReports = new ArrayList<>();
        List<String> allIncidents = new ArrayList<>();

        List<TestCase> tests = config.getTests();
        if (tests == null || tests.isEmpty()) {
            throw new IllegalStateException("No test cases found in config.");
        }

        for (int i = 0; i < tests.size(); i++) {
            TestCase testCase = tests.get(i);
            int judgeRuns = Math.min(testCase.getJudgeRuns(), testCase.getRuns());
            System.out.printf("[%d/%d] Running: %s  (agent×%d, judge×%d)%n",
                    i + 1, tests.size(), testCase.getName(), testCase.getRuns(), judgeRuns);

            Scenario scenario = config.findScenario(testCase.getScenarioRef());
            TestCaseReport report = runTestCase(config, testCase, scenario, judges, metrics);
            testCaseReports.add(report);
            allIncidents.addAll(report.getIncidents() != null ? report.getIncidents() : List.of());
            printTestCaseProgress(report);
        }

        return buildFinalReport(config.getName(), testCaseReports, allIncidents, metrics);
    }

    // ── Phase 1: collect all agent responses ────────────────────────────────

    private List<String> collectResponses(EvalConfig config, TestCase testCase, Scenario scenario) {
        List<String> responses = new ArrayList<>();
        int runs = testCase.getRuns();

        for (int run = 0; run < runs; run++) {
            if (runs <= 10 || (run + 1) % Math.max(1, runs / 10) == 0) {
                System.out.printf("    agent %d/%d%n", run + 1, runs);
            }
            try {
                String output = agentCaller.call(config.getTestedAgent(), scenario, testCase);
                responses.add(output);
            } catch (Exception e) {
                System.err.println("    ✗ Agent call failed (run " + (run + 1) + "): " + e.getMessage());
            }
        }
        return responses;
    }

    // ── Phase 2+3: judge a sample, find reference ────────────────────────────

    /**
     * Returns judge scores for the sampled responses.
     * The "reference" is the response with the highest consensus score.
     */
    private JudgeSample judgeResponses(
            List<String> responses,
            TestCase testCase,
            Scenario scenario,
            List<Judge> judges) {

        int judgeRuns = Math.min(testCase.getJudgeRuns(), responses.size());

        // Pick indices evenly spread across the response list
        List<Integer> sampleIndices = pickSampleIndices(responses.size(), judgeRuns);

        Map<Integer, Double> judgedScores = new LinkedHashMap<>();
        String bestResponse = responses.get(0);
        double bestScore = -1;

        System.out.printf("    judging %d/%d responses...%n", judgeRuns, responses.size());

        for (int idx : sampleIndices) {
            String response = responses.get(idx);
            List<JudgeOutput> outputs = new ArrayList<>();
            for (Judge judge : judges) {
                JudgeOutput out = judge.judge(scenario, testCase, response);
                normalizer.record(judge.getName(), out.getScore());
                outputs.add(out);
            }
            double score = normalizer.weightedConsensus(outputs);
            judgedScores.put(idx, score);

            if (score > bestScore) {
                bestScore = score;
                bestResponse = response;
            }
        }

        return new JudgeSample(judgedScores, bestResponse, bestScore);
    }

    // ── Phase 4: score non-judged runs via similarity ─────────────────────────

    private TestCaseReport runTestCase(
            EvalConfig config,
            TestCase testCase,
            Scenario scenario,
            List<Judge> judges,
            List<Metric> metrics) {

        // Phase 1: all agent runs
        List<String> responses = collectResponses(config, testCase, scenario);
        if (responses.isEmpty()) {
            return emptyReport(testCase.getName());
        }

        // Phase 2+3: judge sample
        JudgeSample sample = judgeResponses(responses, testCase, scenario, judges);

        // Phase 4: similarity scores for non-judged runs
        double[] similarityScores = TextSimilarity.againstReference(responses, sample.referenceResponse);

        // Phase 5: merge into unified TestResult list
        List<TestResult> runResults = new ArrayList<>();
        List<String> incidents = new ArrayList<>();

        for (int i = 0; i < responses.size(); i++) {
            double score;
            String source;

            if (sample.judgedScores.containsKey(i)) {
                // This run was judged
                score = sample.judgedScores.get(i);
                source = "judged";
            } else {
                // Scale similarity [0,1] → score anchored to judge reference
                // If similarity=1.0 → same score as reference; if 0 → 0
                score = similarityScores[i] * sample.referenceScore;
                source = "similarity";
            }

            TestResult result = new TestResult();
            result.setTestName(testCase.getName());
            result.setAgentOutput(responses.get(i));
            result.setConsensusScore(score);

            // Incidents
            List<String> resultIncidents = new ArrayList<>();
            if (testCase.getThresholds() != null) {
                for (Map.Entry<String, Double> threshold : testCase.getThresholds().entrySet()) {
                    if (score < threshold.getValue()) {
                        String incident = String.format(
                                "INCIDENT [%s] run=%d score=%.3f threshold=%.3f source=%s",
                                testCase.getName(), i + 1, score, threshold.getValue(), source);
                        resultIncidents.add(incident);
                        incidents.add(incident);
                    }
                }
            }
            result.setIncidents(resultIncidents);
            result.setPassed(resultIncidents.isEmpty());
            runResults.add(result);
        }

        // Consistency via pairwise similarity (free, across ALL runs)
        double consistencyRaw = TextSimilarity.averagePairwise(responses);

        // Metrics
        Map<String, Double> metricScores = new LinkedHashMap<>();
        for (Metric metric : metrics) {
            double val = metric.getName().equals("consistency")
                    ? consistencyRaw   // override: use similarity-based consistency
                    : metric.calculate(runResults);
            metricScores.put(metric.getName(), val);
        }

        DoubleSummaryStatistics stats = runResults.stream()
                .mapToDouble(TestResult::getConsensusScore)
                .summaryStatistics();

        double variance = 0;
        if (!runResults.isEmpty()) {
            double mean = stats.getAverage();
            variance = runResults.stream()
                    .mapToDouble(r -> (r.getConsensusScore() - mean) * (r.getConsensusScore() - mean))
                    .average().orElse(0);
        }

        TestCaseReport report = new TestCaseReport();
        report.setTestName(testCase.getName());
        report.setTotalRuns(runResults.size());
        report.setPassedRuns((int) runResults.stream().filter(TestResult::isPassed).count());
        report.setAverageScore(stats.getCount() > 0 ? stats.getAverage() : 0.0);
        report.setMinScore(stats.getCount() > 0 ? stats.getMin() : 0.0);
        report.setMaxScore(stats.getCount() > 0 ? stats.getMax() : 0.0);
        report.setScoreVariance(variance);
        report.setMetricScores(metricScores);
        report.setIncidents(incidents);
        report.setPassed(incidents.isEmpty());
        return report;
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    /**
     * Pick `count` indices evenly spread across [0, total).
     * If count >= total, returns all indices.
     */
    private List<Integer> pickSampleIndices(int total, int count) {
        if (count >= total) {
            List<Integer> all = new ArrayList<>();
            for (int i = 0; i < total; i++) all.add(i);
            return all;
        }
        List<Integer> indices = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            indices.add((int) Math.round((double) i * (total - 1) / (count - 1)));
        }
        return indices;
    }

    private List<Judge> buildJudges(EvalConfig config) {
        List<Judge> judges = new ArrayList<>();
        if (config.getJudges() == null) return judges;

        for (JudgeConfig jc : config.getJudges()) {
            try {
                Judge judge = JudgeFactory.create(jc);
                if (judge.isAvailable()) {
                    judges.add(judge);
                    System.out.println("  ✓ Judge available: " + judge.getName());
                } else {
                    System.out.println("  ✗ Judge unavailable (skipping): " + judge.getName());
                }
            } catch (Exception e) {
                System.out.println("  ✗ Failed to create judge " + jc.getModel() + ": " + e.getMessage());
            }
        }
        return judges;
    }

    private EvalReport buildFinalReport(
            String evalName,
            List<TestCaseReport> reports,
            List<String> incidents,
            List<Metric> metrics) {

        DoubleSummaryStatistics stats = reports.stream()
                .mapToDouble(TestCaseReport::getAverageScore)
                .summaryStatistics();

        Map<String, Double> overallMetrics = new LinkedHashMap<>();
        for (Metric metric : metrics) {
            double avg = reports.stream()
                    .mapToDouble(r -> r.getMetricScores() != null
                            ? r.getMetricScores().getOrDefault(metric.getName(), 0.0) : 0.0)
                    .average().orElse(0.0);
            overallMetrics.put(metric.getName(), avg);
        }

        EvalReport report = new EvalReport();
        report.setEvalName(evalName);
        report.setTotalTests(reports.size());
        report.setPassedTests((int) reports.stream().filter(TestCaseReport::isPassed).count());
        report.setFailedTests((int) reports.stream().filter(r -> !r.isPassed()).count());
        report.setAverageScore(stats.getCount() > 0 ? stats.getAverage() : 0.0);
        report.setMinScore(stats.getCount() > 0 ? stats.getMin() : 0.0);
        report.setMaxScore(stats.getCount() > 0 ? stats.getMax() : 0.0);
        report.setTestCaseReports(reports);
        report.setIncidents(incidents);
        report.setOverallMetrics(overallMetrics);
        return report;
    }

    private TestCaseReport emptyReport(String name) {
        TestCaseReport r = new TestCaseReport();
        r.setTestName(name);
        r.setPassed(false);
        r.setIncidents(List.of("All agent calls failed"));
        return r;
    }

    private void printTestCaseProgress(TestCaseReport report) {
        String status = report.isPassed() ? "✓ PASS" : "✗ FAIL";
        System.out.printf("    %s  avg=%.3f  min=%.3f  max=%.3f  variance=%.4f  incidents=%d%n",
                status,
                report.getAverageScore(),
                report.getMinScore(),
                report.getMaxScore(),
                report.getScoreVariance(),
                report.getIncidents() != null ? report.getIncidents().size() : 0);
    }

    // ── inner record ─────────────────────────────────────────────────────────

    private static class JudgeSample {
        final Map<Integer, Double> judgedScores;  // index → score for judged runs
        final String referenceResponse;            // best-scoring response text
        final double referenceScore;               // its judge score

        JudgeSample(Map<Integer, Double> judgedScores, String referenceResponse, double referenceScore) {
            this.judgedScores = judgedScores;
            this.referenceResponse = referenceResponse;
            this.referenceScore = referenceScore;
        }
    }
}
