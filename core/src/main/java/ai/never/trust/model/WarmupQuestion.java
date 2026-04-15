package ai.never.trust.model;

public record WarmupQuestion(
    String context,
    String userQuery,
    String expectedOutput
) {}
