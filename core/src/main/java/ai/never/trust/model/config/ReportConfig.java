package ai.never.trust.model.config;

/**
 * Configuration for the evaluation report output.
 * Defines which reporter to use for evaluation results.
 *
 * @param type    The type of report to generate.
 *                Example: "console", "html", "json", "pdf"
 */
public record ReportConfig(
    String type
) {}
