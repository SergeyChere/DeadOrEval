package ai.never.trust.model.result;

import java.util.List;
import java.util.Map;

public record JudgeReport(
    String judgeName,
    double averageScore,
    double minScore,
    double maxScore,
    int totalIncidents,
    Map<String, Double> tags,
    List<EvalResult> incidents
) {}
