package ai.never.trust.metric;

import ai.never.trust.model.TestResult;

import java.util.List;

/**
 * Accuracy metric: measures the fraction of runs where the agent scored above threshold.
 * Score = (runs above 0.5) / total runs
 */
public class AccuracyMetric implements Metric {

    @Override
    public double calculate(List<TestResult> results) {
        if (results == null || results.isEmpty()) return 0.0;

        long passed = results.stream()
                .filter(r -> r.getConsensusScore() >= 0.5)
                .count();

        return (double) passed / results.size();
    }

    @Override
    public String getName() { return "accuracy"; }

    @Override
    public String getDescription() {
        return "Fraction of runs where the agent response scored above 0.5";
    }
}
