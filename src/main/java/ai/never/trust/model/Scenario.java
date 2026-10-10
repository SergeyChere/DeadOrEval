package ai.never.trust.model;

/**
 * Reusable evaluation scenario.
 * Loaded once, referenced by multiple test cases via scenarioRef.
 * The description is sent to judges as context before evaluation.
 */
public class Scenario {
    private String name;
    private String description;

    public Scenario() {}

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    @Override
    public String toString() {
        return "Scenario{name='" + name + "'}";
    }
}
