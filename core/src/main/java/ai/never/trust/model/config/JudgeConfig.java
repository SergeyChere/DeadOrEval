package ai.never.trust.model.config;

public record JudgeConfig(
    String type,
    String model,
    String url,
    String apiKey
) {}
