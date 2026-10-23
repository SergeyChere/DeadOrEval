package ai.never.trust.engine;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Lightweight text similarity — no external dependencies, pure Java.
 *
 * Uses token-level Jaccard similarity (word overlap).
 * Good enough for detecting whether two chatbot responses say the same thing.
 *
 * Returns a value in [0.0, 1.0] where:
 *   1.0 = identical token sets
 *   0.0 = no common tokens
 */
public class TextSimilarity {

    /**
     * Jaccard similarity between two texts.
     * Tokenises by whitespace, lowercases, ignores punctuation.
     */
    public static double jaccard(String a, String b) {
        if (a == null || b == null) return 0.0;
        Set<String> tokensA = tokenize(a);
        Set<String> tokensB = tokenize(b);
        if (tokensA.isEmpty() && tokensB.isEmpty()) return 1.0;
        if (tokensA.isEmpty() || tokensB.isEmpty()) return 0.0;

        Set<String> intersection = new HashSet<>(tokensA);
        intersection.retainAll(tokensB);

        Set<String> union = new HashSet<>(tokensA);
        union.addAll(tokensB);

        return (double) intersection.size() / union.size();
    }

    /**
     * Average pairwise similarity of a list of texts.
     * Used to measure how consistent the agent responses are across runs.
     * Samples up to MAX_PAIRS pairs for large sets to stay fast.
     */
    public static double averagePairwise(java.util.List<String> texts) {
        if (texts == null || texts.size() < 2) return 1.0;

        int n = texts.size();
        double total = 0.0;
        int pairs = 0;

        // For large n, sample rather than compute all n*(n-1)/2 pairs
        int maxPairs = 200;
        if ((long) n * (n - 1) / 2 <= maxPairs) {
            // Compute all pairs
            for (int i = 0; i < n; i++) {
                for (int j = i + 1; j < n; j++) {
                    total += jaccard(texts.get(i), texts.get(j));
                    pairs++;
                }
            }
        } else {
            // Sample random pairs
            java.util.Random rnd = new java.util.Random(42);
            while (pairs < maxPairs) {
                int i = rnd.nextInt(n);
                int j = rnd.nextInt(n);
                if (i != j) {
                    total += jaccard(texts.get(i), texts.get(j));
                    pairs++;
                }
            }
        }

        return pairs > 0 ? total / pairs : 1.0;
    }

    /**
     * Similarity of each text against a reference text.
     * Used to score non-judged runs against the judge-evaluated reference.
     */
    public static double[] againstReference(java.util.List<String> texts, String reference) {
        double[] scores = new double[texts.size()];
        for (int i = 0; i < texts.size(); i++) {
            scores[i] = jaccard(texts.get(i), reference);
        }
        return scores;
    }

    private static Set<String> tokenize(String text) {
        String clean = text.toLowerCase().replaceAll("[^a-z0-9\\s]", " ");
        return new HashSet<>(Arrays.asList(clean.trim().split("\\s+")));
    }
}
