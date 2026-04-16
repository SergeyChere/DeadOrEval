package ai.never.trust.interfaces.judge;

import java.util.List;
import java.util.Map;

import ai.never.trust.model.warmup.WarmupQuestion;

public interface WarmupStrategy {
    List<WarmupQuestion> getQuestions();
    Map<String, Double> calibrate(List<Judge> judges);
}
