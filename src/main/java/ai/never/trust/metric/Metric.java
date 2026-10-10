package ai.never.trust.metric;

import ai.never.trust.model.TestResult;

import java.util.List;

/**
 * Contract for all metric implementations.
 * A metric calculates a single quality dimension across all runs of a test case.
 */
public interface Metric {
    /**
     * Calculate a metric score from a list of run results.
     * @return Score in [0.0, 1.0]
     */
    double calculate(List<TestResult> results);

    String getName();
    String getDescription();
}
