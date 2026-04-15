package ai.never.trust.model;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public record EvalReport(
    String name,
    LocalDateTime timestamp,
    int totalRuns,
    Map<String, JudgeReport> judgeReports,
    ConsensusReport consensus,
    List<MetricScore> metrics,
    double trustScore
) {}
