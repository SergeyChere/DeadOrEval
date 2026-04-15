package ai.never.trust;

import java.util.List;
import java.util.concurrent.Callable;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(
    name = "doe",
    description = "DeadOrEval - Local-first LLM evaluation framework",
    mixinStandardHelpOptions = true,
    version = "0.1.0"
)
public class Doe implements Callable<Integer> {
    @Option(
        names = {"--config", "-c"},
        description = "Path to eval config file",
        required = true
    )
    private String config;

    @Option(
        names = {"--runs", "-r"},
        description = "Number of evaluation runs"
    )
    private int runs = -1;

    @Option(
        names = {"--judges", "-j"},
        description = "Judges to use (ollama, openai, gemini)",
        split = ","
    )
    private List<String> judges;

    @Option(
        names = {"--metrics", "-m"},
        description = "Metrics to calculate",
        split = ","
    )
    private List<String> metrics;

    @Option(
        names = {"--report"},
        description = "Report type (console, html, json)"
    )
    private String report;

    @Option(
        names = {"--threshold", "-t"},
        description = "Incident threshold (default 0.5)"
    )
    private double threshold = -1;

    @Option(
        names = {"--verbose", "-v"},
        description = "Verbose output"
    )
    private boolean verbose;

    @Override
    public Integer call() throws Exception {
        System.out.println("DeadOrEval starting...");
        System.out.println("Config: " + config);
        System.out.println("Runs: " + runs);
        System.out.println("Judges: " + judges);
        System.out.println("Metrics: " + metrics);
        return 0;
    }

    public static void main(String[] args) {
        int exitCode = new CommandLine(new Doe()).execute(args);
        System.exit(exitCode);
    }
}