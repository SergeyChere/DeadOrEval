package ai.never.trust.interfaces.metric;

import ai.never.trust.model.config.EvalConfig;
import ai.never.trust.model.result.EvalResult;
import ai.never.trust.model.result.MetricScore;

import java.util.List;

/**
 * Core interface for all metric implementations.
 * A Metric analyzes all evaluation results and produces a score.
 *
 * <p>Implementations:</p>
 * <ul>
 *   <li>{@code AccuracyMetric}      - Measures correctness of agent responses</li>
 *   <li>{@code ConsistencyMetric}   - Measures stability across multiple runs</li>
 *   <li>{@code HallucinationMetric} - Detects fabricated information</li>
 *   <li>{@code IncidentTracker}     - Tracks responses below threshold</li>
 * </ul>
 *
 * <p>To add a new metric, simply implement this interface.</p>
 */
public interface Metric {
    
    /**
     * Calculates the metric score based on all evaluation results.
     *
     * @param results  All results produced by judges during this test run.
     * @return {@link MetricScore} containing the calculated score and details.
     */
    MetricScore calculate(List<EvalResult> results);

    /**
     * Returns the unique name of this metric.
     * Must match the name used in {@link EvalConfig}.
     * Example: "accuracy", "consistency", "hallucination"
     */
    String getName();

    /**
     * Returns a human-readable description of what this metric measures.
     * Used in reports and documentation.
     * Example: "Measures the correctness of agent responses against expected output."
     */
    String getDescription();
}
