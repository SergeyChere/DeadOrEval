package ai.never.trust.interfaces;

import ai.never.trust.model.WarmupQuestion;
import java.util.List;
import java.util.Map;

public interface WarmupStrategy {
    List<WarmupQuestion> getQuestions();
    Map<String, Double> calibrate(List<Judge> judges);
}
