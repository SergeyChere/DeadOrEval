package ai.never.trust.metric;

import ai.never.trust.model.TestResult;

import java.util.List;

/**
 * Consistency metric: measures how stable the agent's scores are across multiple runs.
 * High consistency = low variance. Score = 1.0 - normalized_variance.
 *
 * A score of 1.0 means the agent always produces the same quality response.
 * A score of 0.0 means the agent is completely unpredictable.
 */
public class ConsistencyMetric implements Metric {

    @Override
    public double calculate(List<TestResult> results) {
        if (results == null || results.size() < 2) return 1.0;

        double[] scores = results.stream()
                .mapToDouble(TestResult::getConsensusScore)
                .toArray();

        double mean = 0;
        for (double s : scores) mean += s;
        mean /= scores.length;

        double variance = 0;
        for (double s : scores) variance += (s - mean) * (s - mean);
        variance /= scores.length;

        // Normalize: max possible variance for [0,1] values is 0.25
        double normalizedVariance = variance / 0.25;

        return Math.max(0.0, 1.0 - normalizedVariance);
    }

    @Override
    public String getName() { return "consistency"; }

    @Override
    public String getDescription() {
        return "Stability of agent responses across multiple runs (1.0 = perfectly stable, 0.0 = completely random)";
    }
}
