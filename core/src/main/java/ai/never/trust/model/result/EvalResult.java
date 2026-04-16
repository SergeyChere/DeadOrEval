package ai.never.trust.model.result;

import java.util.Map;

import ai.never.trust.model.test.TestCase;

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
