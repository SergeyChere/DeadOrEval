package ai.never.trust.interfaces.metric;

import ai.never.trust.model.result.EvalResult;
import ai.never.trust.model.result.MetricScore;

import java.util.List;

public interface Metric {
    MetricScore calculate(List<EvalResult> results);
    String getName();
    String getDescription();
}
