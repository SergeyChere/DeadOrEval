package ai.never.trust.model.warmup;

/**
 * Represents the calibration result of a single judge after warmup.
 * Used by the engine to apply weighted consensus scoring during evaluation.
 * Judges with higher scores on warmup questions receive higher weights,
 * meaning their evaluation scores have more influence on the final consensus.
 *
 * @param judgeName  The unique name of the judge that was calibrated.
 *                   Example: "ollama/llama3.2:3b"
 *
 * @param score      The average accuracy score of this judge on warmup questions.
 *                   Measured from 0.0 (completely inaccurate) to 1.0 (perfectly accurate).
 *                   Example: 0.87
 *
 * @param weight     The final weight assigned to this judge for consensus scoring.
 *                   Calculated from warmup accuracy and peer review scores.
 *                   Higher weight means more influence on the final consensus result.
 *                   Example: 0.92
 */
public record WarmupResult(
    String judgeName,
    double score,
    double weight
) {}
