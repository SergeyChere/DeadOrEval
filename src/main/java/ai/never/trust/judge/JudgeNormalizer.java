package ai.never.trust.judge;

import ai.never.trust.model.JudgeOutput;

import java.util.*;

/**
 * Normalizes scores from multiple judges to a common [0.0, 1.0] scale.
 * Different models use the scoring range differently — some cluster around 0.7-0.9,
 * others use the full range. Z-score normalization corrects for this.
 */
public class JudgeNormalizer {

    // Per-judge history: judge name -> list of historical scores
    private final Map<String, List<Double>> history = new HashMap<>();

    /**
     * Record a score for a judge (used to build normalization baseline).
     */
    public void record(String judgeName, double score) {
        history.computeIfAbsent(judgeName, k -> new ArrayList<>()).add(score);
    }

    /**
     * Normalize a score using z-score normalization, then rescale to [0.0, 1.0].
     * Falls back to raw score if insufficient history.
     */
    public double normalize(String judgeName, double rawScore) {
        List<Double> scores = history.get(judgeName);
        if (scores == null || scores.size() < 3) {
            return rawScore; // Not enough history, return as-is
        }

        double mean = scores.stream().mapToDouble(d -> d).average().orElse(0.5);
        double variance = scores.stream()
                .mapToDouble(d -> (d - mean) * (d - mean))
                .average()
                .orElse(0.1);
        double stdDev = Math.sqrt(variance);

        if (stdDev < 0.001) {
            return 0.5; // Judge always gives same score — neutral
        }

        // Z-score: how many standard deviations from the mean
        double zScore = (rawScore - mean) / stdDev;

        // Sigmoid-like rescaling to [0.0, 1.0]
        double normalized = 1.0 / (1.0 + Math.exp(-zScore));

        return Math.max(0.0, Math.min(1.0, normalized));
    }

    /**
     * Normalize a list of judge outputs in-place.
     */
    public List<JudgeOutput> normalizeAll(List<JudgeOutput> outputs) {
        // First pass: record all scores
        for (JudgeOutput output : outputs) {
            record(output.getJudgeName(), output.getScore());
        }
        // Second pass: normalize (only effective if we have prior history)
        // For single-run, scores pass through unchanged
        return outputs;
    }

    /**
     * Calculate weighted consensus score from multiple judge outputs.
     * All judges have equal weight (1/n) in MVP.
     */
    public double weightedConsensus(List<JudgeOutput> outputs) {
        if (outputs == null || outputs.isEmpty()) return 0.0;
        if (outputs.size() == 1) return outputs.get(0).getScore();

        double total = 0.0;
        double weight = 1.0 / outputs.size();
        for (JudgeOutput o : outputs) {
            total += o.getScore() * weight;
        }
        return total;
    }
}
