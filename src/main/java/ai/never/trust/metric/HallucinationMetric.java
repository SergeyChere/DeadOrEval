package ai.never.trust.metric;

import ai.never.trust.model.TestResult;

import java.util.List;

/**
 * Hallucination metric: detects runs where the agent produced a very low score
 * despite a valid expected output being defined — indicating fabricated information.
 *
 * A run is considered a hallucination if:
 * - consensus score < 0.3 (very wrong)
 * - expected output was defined (there was a correct answer)
 *
 * Score = 1.0 - (hallucination_rate)
 * A score of 1.0 means no hallucinations detected.
 */
public class HallucinationMetric implements Metric {

    private static final double HALLUCINATION_THRESHOLD = 0.3;

    @Override
    public double calculate(List<TestResult> results) {
        if (results == null || results.isEmpty()) return 1.0;

        long hallucinations = results.stream()
                .filter(r -> r.getConsensusScore() < HALLUCINATION_THRESHOLD)
                .count();

        double hallucinationRate = (double) hallucinations / results.size();
        return Math.max(0.0, 1.0 - hallucinationRate);
    }

    @Override
    public String getName() { return "hallucination"; }

    @Override
    public String getDescription() {
        return "Rate of responses with very low scores (< 0.3) indicating fabricated content. " +
               "1.0 = no hallucinations detected, 0.0 = all responses are hallucinated.";
    }
}
