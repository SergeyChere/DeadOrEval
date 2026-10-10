package ai.never.trust.reporter;

import ai.never.trust.model.EvalReport;
import ai.never.trust.model.TestCaseReport;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * Prints a formatted evaluation report to stdout.
 */
public class ConsoleReporter implements Reporter {

    private static final String DIVIDER = "══════════════════════════════════════════════════════";
    private static final DateTimeFormatter FMT = DateTimeFormatter
            .ofPattern("yyyy-MM-dd HH:mm:ss")
            .withZone(ZoneId.systemDefault());

    @Override
    public void report(EvalReport report) {
        System.out.println();
        System.out.println("╔" + DIVIDER + "╗");
        System.out.println("║              DeadOrEval — Final Report              ║");
        System.out.println("╚" + DIVIDER + "╝");
        System.out.println();
        System.out.println("  Eval:       " + report.getEvalName());
        System.out.println("  Timestamp:  " + FMT.format(report.getTimestamp()));
        System.out.println();

        System.out.println("  ┌─────────────────────────────────────────┐");
        System.out.println("  │               SUMMARY                   │");
        System.out.println("  ├─────────────────────────────────────────┤");
        System.out.printf("  │  Total test cases:    %-18d │%n", report.getTotalTests());
        System.out.printf("  │  Passed:              %-18d │%n", report.getPassedTests());
        System.out.printf("  │  Failed:              %-18d │%n", report.getFailedTests());
        System.out.printf("  │  Average score:       %-18s │%n", format(report.getAverageScore()));
        System.out.printf("  │  Min score:           %-18s │%n", format(report.getMinScore()));
        System.out.printf("  │  Max score:           %-18s │%n", format(report.getMaxScore()));
        System.out.printf("  │  Total incidents:     %-18d │%n",
                report.getIncidents() != null ? report.getIncidents().size() : 0);
        System.out.println("  └─────────────────────────────────────────┘");
        System.out.println();

        // Metrics
        if (report.getOverallMetrics() != null && !report.getOverallMetrics().isEmpty()) {
            System.out.println("  METRICS:");
            for (Map.Entry<String, Double> entry : report.getOverallMetrics().entrySet()) {
                String bar = progressBar(entry.getValue(), 20);
                System.out.printf("    %-20s %s %s%n", entry.getKey(), bar, format(entry.getValue()));
            }
            System.out.println();
        }

        // Per test case
        System.out.println("  TEST CASES:");
        List<TestCaseReport> tcReports = report.getTestCaseReports();
        if (tcReports != null) {
            for (TestCaseReport tc : tcReports) {
                String status = tc.isPassed() ? "✓" : "✗";
                System.out.printf("    %s %-35s  avg=%.3f  runs=%d  incidents=%d%n",
                        status,
                        tc.getTestName(),
                        tc.getAverageScore(),
                        tc.getTotalRuns(),
                        tc.getIncidents() != null ? tc.getIncidents().size() : 0);

                if (tc.getMetricScores() != null && !tc.getMetricScores().isEmpty()) {
                    for (Map.Entry<String, Double> m : tc.getMetricScores().entrySet()) {
                        System.out.printf("      %-20s %.3f%n", m.getKey(), m.getValue());
                    }
                }
            }
        }

        // Incidents
        if (report.getIncidents() != null && !report.getIncidents().isEmpty()) {
            System.out.println();
            System.out.println("  INCIDENTS:");
            for (String incident : report.getIncidents()) {
                System.out.println("    ⚠ " + incident);
            }
        }

        System.out.println();
        String overall = report.getFailedTests() == 0 ? "✓ ALL PASSED" : "✗ " + report.getFailedTests() + " FAILED";
        System.out.println("  " + overall);
        System.out.println("═".repeat(DIVIDER.length() + 2));
        System.out.println();
    }

    private String format(double v) {
        return String.format("%.4f", v);
    }

    private String progressBar(double value, int width) {
        int filled = (int) Math.round(value * width);
        filled = Math.max(0, Math.min(width, filled));
        return "[" + "█".repeat(filled) + "░".repeat(width - filled) + "]";
    }

    @Override
    public String getName() { return "console"; }
}
