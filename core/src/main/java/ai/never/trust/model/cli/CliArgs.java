package ai.never.trust.model.cli;

/**
 * Represents parsed CLI arguments.
 * Only --config is required for MVP.
 *
 * @param config  Path to the EvalConfig YAML file.
 *                Example: "app.yaml"
 */
public record CliArgs(
    String config

    // Post MVP:
    // List<String> judges,
    // List<String> metrics,
    // String report,
    // Integer runs,
    // Boolean verbose,
    // Boolean dryRun
) {}
