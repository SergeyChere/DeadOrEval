package ai.never.trust.interfaces.config;

import java.util.List;
import java.util.Map;

import ai.never.trust.model.result.JudgeOutput;

public interface TagMerger {
    Map<String, Double> merge(List<JudgeOutput> outputs);
}
