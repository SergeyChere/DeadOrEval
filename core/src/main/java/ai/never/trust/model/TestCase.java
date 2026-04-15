package ai.never.trust.model;

import java.util.Map;

public record TestCase(
    String name,
    String description,
    String context,
    String userQuery,
    String agentOutput,
    String expectedOutput,
    int runs,
    Map<String, Double> thresholds
) {}
