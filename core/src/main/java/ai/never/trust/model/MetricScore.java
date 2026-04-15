package ai.never.trust.model;

public record MetricScore(
    String metricName,
    double score,
    String details,
    boolean passed
) {}
