package ai.never.trust.model;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Final report produced after all test cases are evaluated.
 */
public class EvalReport {
    private String evalName;
    private Instant timestamp;
    private int totalTests;
    private int passedTests;
    private int failedTests;
    private double averageScore;
    private double minScore;
    private double maxScore;
    private List<TestCaseReport> testCaseReports;
    private List<String> incidents;
    private Map<String, Double> overallMetrics;

    public EvalReport() {
        this.timestamp = Instant.now();
    }

    public String getEvalName() { return evalName; }
    public void setEvalName(String evalName) { this.evalName = evalName; }

    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }

    public int getTotalTests() { return totalTests; }
    public void setTotalTests(int totalTests) { this.totalTests = totalTests; }

    public int getPassedTests() { return passedTests; }
    public void setPassedTests(int passedTests) { this.passedTests = passedTests; }

    public int getFailedTests() { return failedTests; }
    public void setFailedTests(int failedTests) { this.failedTests = failedTests; }

    public double getAverageScore() { return averageScore; }
    public void setAverageScore(double averageScore) { this.averageScore = averageScore; }

    public double getMinScore() { return minScore; }
    public void setMinScore(double minScore) { this.minScore = minScore; }

    public double getMaxScore() { return maxScore; }
    public void setMaxScore(double maxScore) { this.maxScore = maxScore; }

    public List<TestCaseReport> getTestCaseReports() { return testCaseReports; }
    public void setTestCaseReports(List<TestCaseReport> testCaseReports) { this.testCaseReports = testCaseReports; }

    public List<String> getIncidents() { return incidents; }
    public void setIncidents(List<String> incidents) { this.incidents = incidents; }

    public Map<String, Double> getOverallMetrics() { return overallMetrics; }
    public void setOverallMetrics(Map<String, Double> overallMetrics) { this.overallMetrics = overallMetrics; }
}
