package ai.never.trust.bias;

import java.util.List;

import ai.never.trust.interfaces.Metric;
import ai.never.trust.model.EvalResult;
import ai.never.trust.model.MetricScore;

public class BiasMetric implements Metric {

    @Override
    public MetricScore calculate(List<EvalResult> results) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'calculate'");
    }

    @Override
    public String getName() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getName'");
    }

    @Override
    public String getDescription() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getDescription'");
    }
}
