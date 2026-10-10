package ai.never.trust.model;

/**
 * Defines how evaluation results are reported.
 * Supported types: console, html
 */
public class ReportConfig {
    private String type = "console";

    public ReportConfig() {}

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    @Override
    public String toString() {
        return "ReportConfig{type='" + type + "'}";
    }
}
