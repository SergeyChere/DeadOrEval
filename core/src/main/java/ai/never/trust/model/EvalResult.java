package ai.never.trust.model;

import java.util.Map;

public record EvalResult(
    TestCase testCase,
    String judgeName,
    String metricName,
    double score,
    Map<String, Double> tags,
    boolean isIncident,
    EvalStatus status,
    long durationMs
) {}
