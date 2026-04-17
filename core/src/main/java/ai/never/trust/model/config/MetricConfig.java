package ai.never.trust.model.config;

/**
 * Configuration for a metric used during evaluation.
 *
 * @param name  The name of the metric to calculate.
 *              Example: "accuracy", "consistency", "hallucination"
 */
public record MetricConfig(
    String name
) {

}
