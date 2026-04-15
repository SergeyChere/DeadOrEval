package ai.never.trust.model;

public record JudgeConfig(
    String type,
    String model,
    String url,
    String apiKey
) {}
