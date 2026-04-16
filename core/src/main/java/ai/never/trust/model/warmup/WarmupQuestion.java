package ai.never.trust.model.warmup;

public record WarmupQuestion(
    String context,
    String userQuery,
    String expectedOutput
) {}
