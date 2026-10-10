package ai.never.trust.reporter;

import ai.never.trust.model.ReportConfig;

/**
 * Creates the appropriate Reporter based on ReportConfig.
 */
public class ReporterFactory {

    public static Reporter create(ReportConfig config) {
        if (config == null) return new ConsoleReporter();

        return switch (config.getType().toLowerCase()) {
            case "console" -> new ConsoleReporter();
            case "html" -> new HtmlReporter();
            default -> {
                System.out.println("Unknown report type '" + config.getType() + "', falling back to console.");
                yield new ConsoleReporter();
            }
        };
    }
}
