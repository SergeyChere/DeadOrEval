package ai.never.trust.model;

import java.util.List;

public record JudgeOutput(
    double score,
    List<String> tags
) {}
