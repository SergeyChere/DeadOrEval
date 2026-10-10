package ai.never.trust.metric;

import ai.never.trust.model.TestResult;

import java.util.List;

/**
 * Incident tracking metric: measures the fraction of runs that fell below
 * the configured threshold — these are "incidents" that require attention.
 *
 * Score = 1.0 - incident_rate
 * A score of 1.0 means no incidents.
 */
public class IncidentMetric implements Metric {

    private final double threshold;

    public IncidentMetric() {
        this.threshold = 0.7; // Default: flag anything below 70% quality
    }

    public IncidentMetric(double threshold) {
        this.threshold = threshold;
    }

    @Override
    public double calculate(List<TestResult> results) {
        if (results == null || results.isEmpty()) return 1.0;

        long incidents = results.stream()
                .filter(r -> r.getConsensusScore() < threshold)
                .count();

        return 1.0 - ((double) incidents / results.size());
    }

    @Override
    public String getName() { return "incident_tracking"; }

    @Override
    public String getDescription() {
        return "Fraction of runs that did NOT trigger an incident (score below " + threshold + "). " +
               "1.0 = no incidents, 0.0 = all runs are incidents.";
    }
}
