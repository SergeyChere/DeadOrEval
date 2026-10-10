package ai.never.trust.model;

import java.util.List;
import java.util.Map;

/**
 * Result of a single test case run.
 * Contains all judge outputs, consensus score, and metric scores.
 */
public class TestResult {
    private String testName;
    private String agentOutput;
    private List<JudgeOutput> judgeOutputs;
    private double consensusScore;
    private Map<String, Double> metricScores;
    private boolean passed;
    private List<String> incidents;

    public TestResult() {}

    public String getTestName() { return testName; }
    public void setTestName(String testName) { this.testName = testName; }

    public String getAgentOutput() { return agentOutput; }
    public void setAgentOutput(String agentOutput) { this.agentOutput = agentOutput; }

    public List<JudgeOutput> getJudgeOutputs() { return judgeOutputs; }
    public void setJudgeOutputs(List<JudgeOutput> judgeOutputs) { this.judgeOutputs = judgeOutputs; }

    public double getConsensusScore() { return consensusScore; }
    public void setConsensusScore(double consensusScore) { this.consensusScore = consensusScore; }

    public Map<String, Double> getMetricScores() { return metricScores; }
    public void setMetricScores(Map<String, Double> metricScores) { this.metricScores = metricScores; }

    public boolean isPassed() { return passed; }
    public void setPassed(boolean passed) { this.passed = passed; }

    public List<String> getIncidents() { return incidents; }
    public void setIncidents(List<String> incidents) { this.incidents = incidents; }
}
