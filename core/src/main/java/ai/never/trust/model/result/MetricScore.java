package ai.never.trust.model.result;

public record MetricScore(
    String metricName,
    double score,
    String details,
    boolean passed
) {}
