package ai.never.trust;

import ai.never.trust.model.cli.CliArgs;
import ai.never.trust.model.config.EvalConfig;

/**
 * Entry point for DeadOrEval CLI.
 * Usage: doe --config app.yaml
 */
public class Doe {

    public static void main(String[] args) {
        CliArgs cliArgs = parseArgs(args);

        if (cliArgs == null) {
            printUsage();
            System.exit(1);
        }

        System.out.println("DeadOrEval starting...");
        System.out.println("Config: " + cliArgs.config());

        YamlConfigParser parser = new YamlConfigParser();
        EvalConfig evalConfig = parser.parse(cliArgs.config());

        System.out.println("\n=== Parsed EvalConfig ===");
        System.out.println("Name:        " + evalConfig.name());
        System.out.println("TestedAgent: " + evalConfig.testedAgent().url());
        System.out.println("Judges:      " + evalConfig.judges().size());
        evalConfig.judges().forEach(j ->
            System.out.println("  - " + j.model() + " @ " + j.url()));
        System.out.println("Metrics:     " + evalConfig.metrics().size());
        evalConfig.metrics().forEach(m ->
            System.out.println("  - " + m.name()));
        System.out.println("Report:      " + evalConfig.report().type());
        System.out.println("Scenarios:   " + evalConfig.scenarios().size());
        evalConfig.scenarios().forEach(s ->
            System.out.println("  - " + s.name()));
        System.out.println("Tests:       " + evalConfig.tests().size());
        evalConfig.tests().forEach(t ->
            System.out.println("  - " + t.name() + " (runs: " + t.runs() + ")"));
    }

    private static CliArgs parseArgs(String[] args) {
        String config = null;

        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "--config", "-c" -> {
                    if (i + 1 < args.length) config = args[++i];
                }
                case "--help", "-h" -> {
                    printUsage();
                    System.exit(0);
                }
                case "--version" -> {
                    System.out.println("DeadOrEval 0.1.0");
                    System.exit(0);
                }
            }
        }

        if (config == null) {
            System.err.println("Error: --config is required");
            return null;
        }

        return new CliArgs(config);
    }

    private static void printUsage() {
        System.out.println("""
            DeadOrEval — Local-first LLM evaluation framework
            
            Usage:
              doe --config <path>
            
            Arguments:
              --config, -c   Path to EvalConfig YAML file (required)
              --version      Print version
              --help, -h     Print this help
            
            Example:
              doe --config app.yaml
            """);
    }
}