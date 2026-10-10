package ai.never.trust.cli;

import ai.never.trust.config.YamlConfigParser;
import ai.never.trust.engine.EvalEngine;
import ai.never.trust.model.EvalConfig;
import ai.never.trust.model.EvalReport;
import ai.never.trust.reporter.Reporter;
import ai.never.trust.reporter.ReporterFactory;

/**
 * CLI entry point for DeadOrEval.
 *
 * Usage:
 *   java -jar doe.jar --config app.yaml
 *   java -jar doe.jar --version
 *   java -jar doe.jar --help
 */
public class Doe {

    private static final String VERSION = "0.1.0-SNAPSHOT";

    public static void main(String[] args) {
        CliArgs cliArgs = parseArgs(args);

        if (cliArgs.showHelp) {
            printHelp();
            return;
        }

        if (cliArgs.showVersion) {
            System.out.println("DeadOrEval " + VERSION);
            return;
        }

        if (cliArgs.configPath == null) {
            System.err.println("Error: --config is required.");
            System.err.println("Usage: doe --config app.yaml");
            System.exit(1);
        }

        try {
            run(cliArgs.configPath);
        } catch (Exception e) {
            System.err.println("\nError: " + e.getMessage());
            if (isVerbose(args)) {
                e.printStackTrace();
            }
            System.exit(1);
        }
    }

    private static void run(String configPath) throws Exception {
        System.out.println("DeadOrEval " + VERSION);
        System.out.println("Loading config: " + configPath);

        YamlConfigParser parser = new YamlConfigParser();
        EvalConfig config = parser.parse(configPath);

        printConfig(config);

        EvalEngine engine = new EvalEngine();
        EvalReport report = engine.run(config);

        Reporter reporter = ReporterFactory.create(config.getReport());
        reporter.report(report);

        // Exit with non-zero if any tests failed
        if (report.getFailedTests() > 0) {
            System.exit(1);
        }
    }

    private static void printConfig(EvalConfig config) {
        System.out.println();
        System.out.println("Config loaded:");
        System.out.println("  name:    " + config.getName());
        System.out.println("  agent:   " + (config.getTestedAgent() != null ? config.getTestedAgent().getUrl() : "not set"));
        System.out.println("  judges:  " + (config.getJudges() != null ? config.getJudges().size() : 0));
        System.out.println("  metrics: " + (config.getMetrics() != null ? String.join(", ", config.getMetrics()) : "none"));
        System.out.println("  report:  " + (config.getReport() != null ? config.getReport().getType() : "console"));
        System.out.println("  tests:   " + (config.getTests() != null ? config.getTests().size() : 0));
        System.out.println("  scenarios: " + (config.getScenarios() != null ? config.getScenarios().size() : 0));
    }

    private static CliArgs parseArgs(String[] args) {
        CliArgs result = new CliArgs();
        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "--config", "-c" -> {
                    if (i + 1 < args.length) result.configPath = args[++i];
                }
                case "--version", "-v" -> result.showVersion = true;
                case "--help", "-h" -> result.showHelp = true;
            }
        }
        return result;
    }

    private static boolean isVerbose(String[] args) {
        for (String a : args) {
            if ("--verbose".equals(a) || "-V".equals(a)) return true;
        }
        return false;
    }

    private static void printHelp() {
        System.out.println("""
                DeadOrEval — Is your chatbot dead or alive?

                Usage:
                  doe --config <path>     Run evaluation with the given config file
                  doe --version           Print version
                  doe --help              Show this help

                Options:
                  --config, -c <path>     Path to YAML config file (required)
                  --version, -v           Print version and exit
                  --help, -h              Show help
                  --verbose, -V           Print full stack traces on error

                Example:
                  doe --config app.yaml

                Config file format:
                  name: my-eval
                  testedAgent:
                    url: http://localhost:8080/api/chat
                    method: POST
                  judges:
                    - model: llama3.2:3b
                      url: http://localhost:11434
                  metrics:
                    - accuracy
                    - consistency
                  report:
                    type: html
                  scenarios:
                    - name: basic
                      description: "A user is asking a simple question."
                  tests:
                    - name: basic-test
                      scenarioRef: basic
                      userQuery: "What are your opening hours?"
                      expectedOutput: "We are open Monday to Friday, 9am to 5pm."
                      runs: 10
                      thresholds:
                        accuracy: 0.8

                Docs: https://github.com/SergeyChere/DeadOrEval
                """);
    }

    private static class CliArgs {
        String configPath;
        boolean showVersion;
        boolean showHelp;
    }
}
