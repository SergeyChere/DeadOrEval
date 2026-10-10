package ai.never.trust.model;

import java.util.List;

/**
 * Main configuration for a DeadOrEval evaluation run.
 * Parsed from YAML by YamlConfigParser.
 */
public class EvalConfig {
    private String name;
    private TestedAgent testedAgent;
    private List<JudgeConfig> judges;
    private List<String> metrics;
    private ReportConfig report;
    private List<Scenario> scenarios;
    private List<TestCase> tests;

    public EvalConfig() {}

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public TestedAgent getTestedAgent() { return testedAgent; }
    public void setTestedAgent(TestedAgent testedAgent) { this.testedAgent = testedAgent; }

    public List<JudgeConfig> getJudges() { return judges; }
    public void setJudges(List<JudgeConfig> judges) { this.judges = judges; }

    public List<String> getMetrics() { return metrics; }
    public void setMetrics(List<String> metrics) { this.metrics = metrics; }

    public ReportConfig getReport() { return report; }
    public void setReport(ReportConfig report) { this.report = report; }

    public List<Scenario> getScenarios() { return scenarios; }
    public void setScenarios(List<Scenario> scenarios) { this.scenarios = scenarios; }

    public List<TestCase> getTests() { return tests; }
    public void setTests(List<TestCase> tests) { this.tests = tests; }

    public Scenario findScenario(String name) {
        if (scenarios == null || name == null) return null;
        return scenarios.stream()
                .filter(s -> name.equals(s.getName()))
                .findFirst()
                .orElse(null);
    }

    @Override
    public String toString() {
        return "EvalConfig{name='" + name + "', judges=" +
               (judges != null ? judges.size() : 0) + ", tests=" +
               (tests != null ? tests.size() : 0) + "}";
    }
}
