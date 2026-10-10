package ai.never.trust.model;

import java.util.Map;

/**
 * A single test case for evaluating a chatbot response.
 * The referenced Scenario is sent once as context before all runs begin.
 * Then userQuery is executed `runs` times and each response is evaluated.
 */
public class TestCase {
    private String name;
    private String description;
    private String scenarioRef;
    private String userQuery;
    private String expectedOutput;
    private int runs = 1;
    private Map<String, Double> thresholds;

    public TestCase() {}

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getScenarioRef() { return scenarioRef; }
    public void setScenarioRef(String scenarioRef) { this.scenarioRef = scenarioRef; }

    public String getUserQuery() { return userQuery; }
    public void setUserQuery(String userQuery) { this.userQuery = userQuery; }

    public String getExpectedOutput() { return expectedOutput; }
    public void setExpectedOutput(String expectedOutput) { this.expectedOutput = expectedOutput; }

    public int getRuns() { return runs; }
    public void setRuns(int runs) { this.runs = runs; }

    public Map<String, Double> getThresholds() { return thresholds; }
    public void setThresholds(Map<String, Double> thresholds) { this.thresholds = thresholds; }

    public double getThreshold(String metric) {
        if (thresholds == null) return 0.8;
        return thresholds.getOrDefault(metric, 0.8);
    }

    @Override
    public String toString() {
        return "TestCase{name='" + name + "', runs=" + runs + "}";
    }
}
