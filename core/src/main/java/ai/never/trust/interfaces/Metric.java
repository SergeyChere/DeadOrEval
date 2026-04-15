package ai.never.trust.interfaces;

import ai.never.trust.model.EvalResult;
import ai.never.trust.model.MetricScore;
import java.util.List;

public interface Metric {
    MetricScore calculate(List<EvalResult> results);
    String getName();
    String getDescription();
}
