package ai.never.trust.model;

public record WarmupResult(
    String judgeName,
    double score,
    double weight
) {}
