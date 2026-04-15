package ai.never.trust.model;

import java.util.List;

public record EvalConfig(
    String name,
    Target target,
    List<JudgeConfig> judges,
    List<String> metrics,
    ReportConfig report,
    List<TestCase> tests
) {}
