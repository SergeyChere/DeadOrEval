package ai.never.trust.model.config;

import java.util.List;

import ai.never.trust.model.test.Scenario;
import ai.never.trust.model.test.TestCase;

/**
 * The main configuration for a DeadOrEval evaluation run.
 * Defines everything needed to evaluate an agent — who to test,
 * how to judge, what to measure, and how to report.
 *
 * @param name          Unique identifier for this evaluation run.
 *                      Example: "dental-bot-eval-v1"
 *
 * @param testedAgent   The agent under evaluation and how to connect to it.
 *                      See {@link TestedAgent}
 *
 * @param judges        List of judges used to evaluate agent responses.
 *                      Multiple judges enable consensus evaluation.
 *                      See {@link JudgeConfig}
 *
 * @param metrics       List of metric names to calculate during evaluation.
 *                      Example: ["accuracy", "consistency", "hallucination"]
 *
 * @param report        Configuration for how evaluation results are reported.
 *                      See {@link ReportConfig}
 *
 * @param tests         List of test cases to execute against the agent.
 *                      See {@link TestCase}
 */
public record EvalConfig(
    String name,
    TestedAgent testedAgent,
    List<JudgeConfig> judges,
    List<MetricConfig> metrics,
    ReportConfig report,
    List<Scenario> scenarios,
    List<TestCase> tests
) {}
