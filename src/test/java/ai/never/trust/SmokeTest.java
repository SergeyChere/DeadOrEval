package ai.never.trust;

import ai.never.trust.config.YamlConfigParser;
import ai.never.trust.metric.*;
import ai.never.trust.model.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Manual smoke test — no JUnit dependency needed.
 * Run with: java -cp target/classes:snakeyaml.jar ai.never.trust.SmokeTest
 */
public class SmokeTest {

    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) throws Exception {
        System.out.println("=== DeadOrEval Smoke Tests ===\n");

        testConfigParser();
        testMetrics();
        testJudgeNormalizer();

        System.out.println("\n=== Results: " + passed + " passed, " + failed + " failed ===");
        if (failed > 0) System.exit(1);
    }

    static void testConfigParser() throws IOException {
        System.out.println("--- Config Parser ---");

        String yaml = """
                name: test-eval
                testedAgent:
                  url: http://localhost:8080/api/chat
                  method: POST
                judges:
                  - model: llama3.2:3b
                    url: http://localhost:11434
                metrics:
                  - accuracy
                  - consistency
                report:
                  type: html
                scenarios:
                  - name: basic
                    description: A user asks a question.
                tests:
                  - name: basic-test
                    scenarioRef: basic
                    userQuery: What are your hours?
                    expectedOutput: We are open 9am to 5pm.
                    runs: 10
                    thresholds:
                      accuracy: 0.9
                """;

        Path tmp = Files.createTempFile("doe-test-", ".yaml");
        Files.writeString(tmp, yaml);

        YamlConfigParser parser = new YamlConfigParser();
        EvalConfig config = parser.parse(tmp.toString());
        Files.deleteIfExists(tmp);

        assertEqual("name", "test-eval", config.getName());
        assertEqual("agent url", "http://localhost:8080/api/chat", config.getTestedAgent().getUrl());
        assertEqual("judge count", 1, config.getJudges().size());
        assertEqual("judge model", "llama3.2:3b", config.getJudges().get(0).getModel());
        assertEqual("metrics count", 2, config.getMetrics().size());
        assertEqual("report type", "html", config.getReport().getType());
        assertEqual("scenario count", 1, config.getScenarios().size());
        assertEqual("test count", 1, config.getTests().size());
        assertEqual("test runs", 10, config.getTests().get(0).getRuns());
        assertDoubleEqual("accuracy threshold", 0.9, config.getTests().get(0).getThreshold("accuracy"));

        // Scenario resolution
        Scenario found = config.findScenario("basic");
        assertNotNull("findScenario(basic)", found);
        assertNull("findScenario(missing)", config.findScenario("non-existent"));
    }

    static void testMetrics() {
        System.out.println("\n--- Metrics ---");

        List<TestResult> allGood = results(0.9, 0.8, 0.85, 0.95);
        List<TestResult> halfBad = results(0.9, 0.9, 0.2, 0.1);
        List<TestResult> allBad = results(0.1, 0.15, 0.05, 0.2);
        List<TestResult> stable = results(0.8, 0.8, 0.8, 0.8);
        List<TestResult> chaotic = results(0.0, 1.0, 0.0, 1.0);

        assertDoubleEqual("accuracy allGood", 1.0, new AccuracyMetric().calculate(allGood));
        assertDoubleEqual("accuracy halfBad", 0.5, new AccuracyMetric().calculate(halfBad));
        assertDoubleEqual("accuracy allBad", 0.0, new AccuracyMetric().calculate(allBad));

        assertDoubleEqual("consistency stable", 1.0, new ConsistencyMetric().calculate(stable));
        assertTrue("consistency chaotic < 0.1", new ConsistencyMetric().calculate(chaotic) < 0.1);

        assertDoubleEqual("hallucination allGood", 1.0, new HallucinationMetric().calculate(allGood));
        assertDoubleEqual("hallucination allBad", 0.0, new HallucinationMetric().calculate(allBad));

        assertDoubleEqual("incident allGood", 1.0, new IncidentMetric().calculate(allGood));
        assertDoubleEqual("incident allBad", 0.0, new IncidentMetric().calculate(allBad));
    }

    static void testJudgeNormalizer() {
        System.out.println("\n--- JudgeNormalizer ---");
        var normalizer = new ai.never.trust.judge.JudgeNormalizer();

        // Single judge consensus
        var outputs = List.of(new ai.never.trust.model.JudgeOutput("judge-a", 0.8, "good"));
        assertDoubleEqual("single judge consensus", 0.8, normalizer.weightedConsensus(outputs));

        // Multi judge consensus
        var multi = List.of(
            new ai.never.trust.model.JudgeOutput("a", 0.9, ""),
            new ai.never.trust.model.JudgeOutput("b", 0.7, ""),
            new ai.never.trust.model.JudgeOutput("c", 0.8, "")
        );
        assertDoubleEqual("3-judge consensus", 0.8, normalizer.weightedConsensus(multi));
    }

    // ---- helpers ----

    static List<TestResult> results(double... scores) {
        List<TestResult> list = new java.util.ArrayList<>();
        for (double s : scores) {
            TestResult r = new TestResult();
            r.setConsensusScore(s);
            r.setPassed(s >= 0.7);
            list.add(r);
        }
        return list;
    }

    static void assertEqual(String label, Object expected, Object actual) {
        if (expected.equals(actual)) {
            System.out.println("  ✓ " + label);
            passed++;
        } else {
            System.out.println("  ✗ " + label + ": expected=" + expected + " actual=" + actual);
            failed++;
        }
    }

    static void assertDoubleEqual(String label, double expected, double actual) {
        if (Math.abs(expected - actual) < 0.001) {
            System.out.println("  ✓ " + label + " = " + actual);
            passed++;
        } else {
            System.out.println("  ✗ " + label + ": expected=" + expected + " actual=" + actual);
            failed++;
        }
    }

    static void assertNotNull(String label, Object val) {
        if (val != null) { System.out.println("  ✓ " + label); passed++; }
        else { System.out.println("  ✗ " + label + " was null"); failed++; }
    }

    static void assertNull(String label, Object val) {
        if (val == null) { System.out.println("  ✓ " + label); passed++; }
        else { System.out.println("  ✗ " + label + " was not null: " + val); failed++; }
    }

    static void assertTrue(String label, boolean cond) {
        if (cond) { System.out.println("  ✓ " + label); passed++; }
        else { System.out.println("  ✗ " + label + " was false"); failed++; }
    }
}
