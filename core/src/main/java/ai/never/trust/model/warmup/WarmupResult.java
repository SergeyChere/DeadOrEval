package ai.never.trust.model.warmup;

public record WarmupResult(
    String judgeName,
    double score,
    double weight
) {}
