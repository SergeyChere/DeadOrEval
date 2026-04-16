package ai.never.trust.model.result;

import java.util.List;

public record JudgeOutput(
    double score,
    List<String> tags
) {}
