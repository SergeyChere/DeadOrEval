package ai.never.trust.metric;

import java.util.ArrayList;
import java.util.List;

/**
 * Creates metric instances by name from YAML config.
 */
public class MetricRegistry {

    public static List<Metric> build(List<String> metricNames) {
        List<Metric> metrics = new ArrayList<>();
        if (metricNames == null) return metrics;

        for (String name : metricNames) {
            metrics.add(switch (name.trim().toLowerCase()) {
                case "accuracy" -> new AccuracyMetric();
                case "consistency" -> new ConsistencyMetric();
                case "hallucination" -> new HallucinationMetric();
                case "incident_tracking", "incident" -> new IncidentMetric();
                default -> throw new IllegalArgumentException("Unknown metric: " + name +
                        "\nSupported: accuracy, consistency, hallucination, incident_tracking");
            });
        }
        return metrics;
    }
}
