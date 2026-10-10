package ai.never.trust.reporter;

import ai.never.trust.model.EvalReport;
import ai.never.trust.model.TestCaseReport;

import java.io.FileWriter;
import java.io.IOException;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * Generates a self-contained HTML report file.
 * Output: eval-report.html in the current directory.
 */
public class HtmlReporter implements Reporter {

    private static final DateTimeFormatter FMT = DateTimeFormatter
            .ofPattern("yyyy-MM-dd HH:mm:ss")
            .withZone(ZoneId.systemDefault());

    @Override
    public void report(EvalReport report) {
        String filename = "eval-report.html";
        try (FileWriter w = new FileWriter(filename)) {
            w.write(buildHtml(report));
            System.out.println("\n  HTML report saved to: " + filename);
        } catch (IOException e) {
            System.err.println("Failed to write HTML report: " + e.getMessage());
        }
        // Also print console summary
        new ConsoleReporter().report(report);
    }

    private String buildHtml(EvalReport report) {
        String passRate = report.getTotalTests() > 0
                ? String.format("%.1f%%", 100.0 * report.getPassedTests() / report.getTotalTests())
                : "N/A";
        String statusClass = report.getFailedTests() == 0 ? "pass" : "fail";

        StringBuilder sb = new StringBuilder();
        sb.append("""
                <!DOCTYPE html>
                <html lang="en">
                <head>
                  <meta charset="UTF-8">
                  <meta name="viewport" content="width=device-width, initial-scale=1.0">
                  <title>DeadOrEval — %s</title>
                  <style>
                    * { box-sizing: border-box; margin: 0; padding: 0; }
                    body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif;
                           background: #0f1117; color: #e1e4e8; padding: 32px; }
                    h1 { font-size: 24px; font-weight: 700; margin-bottom: 4px; }
                    h2 { font-size: 16px; font-weight: 600; margin: 24px 0 12px; color: #8b949e; text-transform: uppercase; letter-spacing: 1px; }
                    .header { margin-bottom: 32px; }
                    .meta { color: #8b949e; font-size: 13px; margin-top: 4px; }
                    .cards { display: grid; grid-template-columns: repeat(auto-fit, minmax(160px, 1fr)); gap: 16px; margin-bottom: 32px; }
                    .card { background: #161b22; border: 1px solid #30363d; border-radius: 8px; padding: 16px; }
                    .card .value { font-size: 28px; font-weight: 700; }
                    .card .label { font-size: 12px; color: #8b949e; margin-top: 4px; }
                    .pass .value { color: #3fb950; }
                    .fail .value { color: #f85149; }
                    .warn .value { color: #d29922; }
                    .neutral .value { color: #79c0ff; }
                    table { width: 100%%; border-collapse: collapse; background: #161b22; border-radius: 8px; overflow: hidden; border: 1px solid #30363d; }
                    th { background: #21262d; padding: 10px 16px; text-align: left; font-size: 12px; color: #8b949e; text-transform: uppercase; letter-spacing: 0.5px; }
                    td { padding: 10px 16px; border-top: 1px solid #21262d; font-size: 13px; }
                    .badge { display: inline-block; padding: 2px 8px; border-radius: 12px; font-size: 11px; font-weight: 600; }
                    .badge-pass { background: #1a3a2a; color: #3fb950; }
                    .badge-fail { background: #3a1a1a; color: #f85149; }
                    .bar-wrap { display: flex; align-items: center; gap: 8px; }
                    .bar { height: 6px; border-radius: 3px; background: #21262d; flex: 1; }
                    .bar-fill { height: 100%%; border-radius: 3px; background: #3fb950; }
                    .bar-fill.warn { background: #d29922; }
                    .bar-fill.bad { background: #f85149; }
                    .incident { background: #2d1a1a; border-left: 3px solid #f85149; padding: 8px 12px; margin: 4px 0; border-radius: 0 4px 4px 0; font-size: 12px; font-family: monospace; }
                    .section { margin-bottom: 32px; }
                  </style>
                </head>
                <body>
                """.formatted(report.getEvalName()));

        sb.append("""
                <div class="header">
                  <h1>DeadOrEval</h1>
                  <div class="meta">%s &nbsp;·&nbsp; %s</div>
                </div>
                """.formatted(report.getEvalName(), FMT.format(report.getTimestamp())));

        // Summary cards
        sb.append("<div class=\"cards\">\n");
        sb.append(card(passRate, "Pass Rate", statusClass));
        sb.append(card(String.format("%.3f", report.getAverageScore()), "Avg Score", scoreClass(report.getAverageScore())));
        sb.append(card(String.valueOf(report.getTotalTests()), "Test Cases", "neutral"));
        sb.append(card(String.valueOf(report.getFailedTests()), "Failed", report.getFailedTests() == 0 ? "pass" : "fail"));
        sb.append(card(String.valueOf(report.getIncidents() != null ? report.getIncidents().size() : 0), "Incidents", report.getIncidents() == null || report.getIncidents().isEmpty() ? "pass" : "warn"));
        sb.append("</div>\n");

        // Metrics
        if (report.getOverallMetrics() != null && !report.getOverallMetrics().isEmpty()) {
            sb.append("<div class=\"section\">\n<h2>Metrics</h2>\n<table>\n");
            sb.append("<tr><th>Metric</th><th>Score</th><th>Distribution</th></tr>\n");
            for (Map.Entry<String, Double> e : report.getOverallMetrics().entrySet()) {
                double v = e.getValue();
                String barClass = v >= 0.8 ? "" : v >= 0.5 ? " warn" : " bad";
                sb.append(String.format("""
                        <tr>
                          <td>%s</td>
                          <td>%.4f</td>
                          <td><div class="bar-wrap"><div class="bar"><div class="bar-fill%s" style="width:%.1f%%"></div></div><span>%.0f%%</span></div></td>
                        </tr>
                        """, e.getKey(), v, barClass, v * 100, v * 100));
            }
            sb.append("</table></div>\n");
        }

        // Test cases
        sb.append("<div class=\"section\">\n<h2>Test Cases</h2>\n<table>\n");
        sb.append("<tr><th>Test</th><th>Status</th><th>Avg Score</th><th>Runs</th><th>Incidents</th></tr>\n");
        List<TestCaseReport> tcReports = report.getTestCaseReports();
        if (tcReports != null) {
            for (TestCaseReport tc : tcReports) {
                String badge = tc.isPassed()
                        ? "<span class=\"badge badge-pass\">PASS</span>"
                        : "<span class=\"badge badge-fail\">FAIL</span>";
                int incCount = tc.getIncidents() != null ? tc.getIncidents().size() : 0;
                sb.append(String.format("""
                        <tr>
                          <td>%s</td>
                          <td>%s</td>
                          <td>%.3f</td>
                          <td>%d</td>
                          <td>%d</td>
                        </tr>
                        """, tc.getTestName(), badge, tc.getAverageScore(), tc.getTotalRuns(), incCount));
            }
        }
        sb.append("</table></div>\n");

        // Incidents
        if (report.getIncidents() != null && !report.getIncidents().isEmpty()) {
            sb.append("<div class=\"section\">\n<h2>Incidents</h2>\n");
            for (String incident : report.getIncidents()) {
                sb.append("<div class=\"incident\">⚠ " + escHtml(incident) + "</div>\n");
            }
            sb.append("</div>\n");
        }

        sb.append("</body></html>");
        return sb.toString();
    }

    private String card(String value, String label, String cssClass) {
        return String.format("""
                <div class="card %s">
                  <div class="value">%s</div>
                  <div class="label">%s</div>
                </div>
                """, cssClass, value, label);
    }

    private String scoreClass(double score) {
        if (score >= 0.8) return "pass";
        if (score >= 0.5) return "warn";
        return "fail";
    }

    private String escHtml(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    @Override
    public String getName() { return "html"; }
}
