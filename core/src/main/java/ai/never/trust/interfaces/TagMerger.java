package ai.never.trust.interfaces;

import ai.never.trust.model.JudgeOutput;
import java.util.List;
import java.util.Map;

public interface TagMerger {
    Map<String, Double> merge(List<JudgeOutput> outputs);
}
