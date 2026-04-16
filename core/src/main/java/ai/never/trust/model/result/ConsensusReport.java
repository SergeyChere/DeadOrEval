package ai.never.trust.model.result;

import java.util.Map;

public record ConsensusReport(
    double averageScore,
    int totalIncidents,
    Map<String, Double> tags
) {}
