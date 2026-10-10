package ai.never.trust.model;

import java.util.List;
import java.util.Map;

/**
 * Per-test-case summary report.
 * Contains aggregated results across all runs.
 */
public class TestCaseReport {
    private String testName;
    private int totalRuns;
    private int passedRuns;
    private double averageScore;
    private double minScore;
    private double maxScore;
    private double scoreVariance;
    private Map<String, Double> metricScores;
    private List<String> incidents;
    private boolean passed;

    public TestCaseReport() {}

    public String getTestName() { return testName; }
    public void setTestName(String testName) { this.testName = testName; }

    public int getTotalRuns() { return totalRuns; }
    public void setTotalRuns(int totalRuns) { this.totalRuns = totalRuns; }

    public int getPassedRuns() { return passedRuns; }
    public void setPassedRuns(int passedRuns) { this.passedRuns = passedRuns; }

    public double getAverageScore() { return averageScore; }
    public void setAverageScore(double averageScore) { this.averageScore = averageScore; }

    public double getMinScore() { return minScore; }
    public void setMinScore(double minScore) { this.minScore = minScore; }

    public double getMaxScore() { return maxScore; }
    public void setMaxScore(double maxScore) { this.maxScore = maxScore; }

    public double getScoreVariance() { return scoreVariance; }
    public void setScoreVariance(double scoreVariance) { this.scoreVariance = scoreVariance; }

    public Map<String, Double> getMetricScores() { return metricScores; }
    public void setMetricScores(Map<String, Double> metricScores) { this.metricScores = metricScores; }

    public List<String> getIncidents() { return incidents; }
    public void setIncidents(List<String> incidents) { this.incidents = incidents; }

    public boolean isPassed() { return passed; }
    public void setPassed(boolean passed) { this.passed = passed; }
}
