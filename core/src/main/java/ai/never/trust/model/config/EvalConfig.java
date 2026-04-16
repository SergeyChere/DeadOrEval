package ai.never.trust.model.config;

import java.util.List;

import ai.never.trust.model.test.TestCase;

public record EvalConfig(
    String name,
    Target target,
    List<JudgeConfig> judges,
    List<String> metrics,
    ReportConfig report,
    List<TestCase> tests
) {}
