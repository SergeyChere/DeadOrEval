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
 * Flow:
 * 1. Parse config, build judges and metrics
 * 2. For each TestCase:
 *    a. Resolve scenario
 *    b. Run userQuery against the agent N times
 *    c. For each run: all judges evaluate the response → consensus score
 *    d. Aggregate results → TestCaseReport
 * 3. Aggregate all TestCaseReports → EvalReport
 * 4. Return EvalReport
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

        // Build judges
        List<Judge> judges = buildJudges(config);
        if (judges.isEmpty()) {
            throw new IllegalStateException("No judges configured or available. At least one judge is required.");
        }

        // Build metrics
        List<Metric> metrics = MetricRegistry.build(config.getMetrics());

        // Run all test cases
        List<TestCaseReport> testCaseReports = new ArrayList<>();
        List<String> allIncidents = new ArrayList<>();

        List<TestCase> tests = config.getTests();
        if (tests == null || tests.isEmpty()) {
            throw new IllegalStateException("No test cases found in config.");
        }

        for (int i = 0; i < tests.size(); i++) {
            TestCase testCase = tests.get(i);
            System.out.printf("[%d/%d] Running: %s (%d runs)%n",
                    i + 1, tests.size(), testCase.getName(), testCase.getRuns());

            Scenario scenario = config.findScenario(testCase.getScenarioRef());

            TestCaseReport report = runTestCase(config, testCase, scenario, judges, metrics);
            testCaseReports.add(report);
            allIncidents.addAll(report.getIncidents() != null ? report.getIncidents() : List.of());

            printTestCaseProgress(report);
        }

        // Build final report
        return buildFinalReport(config.getName(), testCaseReports, allIncidents, metrics);
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

    private TestCaseReport runTestCase(
            EvalConfig config,
            TestCase testCase,
            Scenario scenario,
            List<Judge> judges,
            List<Metric> metrics) {

        List<TestResult> runResults = new ArrayList<>();
        List<String> incidents = new ArrayList<>();
        int runs = testCase.getRuns();

        for (int run = 0; run < runs; run++) {
            // Print progress every 10% or every run if < 10
            if (runs <= 10 || (run + 1) % Math.max(1, runs / 10) == 0) {
                System.out.printf("    Run %d/%d%n", run + 1, runs);
            }

            String agentOutput;
            try {
                agentOutput = agentCaller.call(config.getTestedAgent(), scenario, testCase);
            } catch (Exception e) {
                System.err.println("    ✗ Agent call failed (run " + (run + 1) + "): " + e.getMessage());
                continue;
            }

            // All judges evaluate this response
            List<JudgeOutput> judgeOutputs = new ArrayList<>();
            for (Judge judge : judges) {
                JudgeOutput output = judge.judge(scenario, testCase, agentOutput);
                normalizer.record(judge.getName(), output.getScore());
                judgeOutputs.add(output);
            }

            double consensusScore = normalizer.weightedConsensus(judgeOutputs);

            TestResult result = new TestResult();
            result.setTestName(testCase.getName());
            result.setAgentOutput(agentOutput);
            result.setJudgeOutputs(judgeOutputs);
            result.setConsensusScore(consensusScore);

            // Check thresholds → incidents
            List<String> resultIncidents = new ArrayList<>();
            if (testCase.getThresholds() != null) {
                for (Map.Entry<String, Double> threshold : testCase.getThresholds().entrySet()) {
                    if (consensusScore < threshold.getValue()) {
                        String incident = String.format("INCIDENT [%s] run=%d score=%.3f threshold=%.3f",
                                testCase.getName(), run + 1, consensusScore, threshold.getValue());
                        resultIncidents.add(incident);
                        incidents.add(incident);
                    }
                }
            }
            result.setIncidents(resultIncidents);
            result.setPassed(resultIncidents.isEmpty());

            runResults.add(result);
        }

        // Calculate metrics across all runs
        Map<String, Double> metricScores = new LinkedHashMap<>();
        for (Metric metric : metrics) {
            metricScores.put(metric.getName(), metric.calculate(runResults));
        }

        // Aggregate run stats
        DoubleSummaryStatistics stats = runResults.stream()
                .mapToDouble(TestResult::getConsensusScore)
                .summaryStatistics();

        double variance = 0;
        if (!runResults.isEmpty()) {
            double mean = stats.getAverage();
            variance = runResults.stream()
                    .mapToDouble(r -> (r.getConsensusScore() - mean) * (r.getConsensusScore() - mean))
                    .average()
                    .orElse(0);
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

    private EvalReport buildFinalReport(
            String evalName,
            List<TestCaseReport> testCaseReports,
            List<String> incidents,
            List<Metric> metrics) {

        DoubleSummaryStatistics stats = testCaseReports.stream()
                .mapToDouble(TestCaseReport::getAverageScore)
                .summaryStatistics();

        Map<String, Double> overallMetrics = new LinkedHashMap<>();
        for (Metric metric : metrics) {
            // Average metric score across all test cases
            double avg = testCaseReports.stream()
                    .map(r -> r.getMetricScores() != null ? r.getMetricScores().getOrDefault(metric.getName(), 0.0) : 0.0)
                    .mapToDouble(d -> d)
                    .average()
                    .orElse(0.0);
            overallMetrics.put(metric.getName(), avg);
        }

        EvalReport report = new EvalReport();
        report.setEvalName(evalName);
        report.setTotalTests(testCaseReports.size());
        report.setPassedTests((int) testCaseReports.stream().filter(TestCaseReport::isPassed).count());
        report.setFailedTests((int) testCaseReports.stream().filter(r -> !r.isPassed()).count());
        report.setAverageScore(stats.getCount() > 0 ? stats.getAverage() : 0.0);
        report.setMinScore(stats.getCount() > 0 ? stats.getMin() : 0.0);
        report.setMaxScore(stats.getCount() > 0 ? stats.getMax() : 0.0);
        report.setTestCaseReports(testCaseReports);
        report.setIncidents(incidents);
        report.setOverallMetrics(overallMetrics);

        return report;
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
}
