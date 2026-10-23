package ai.never.trust.config;

import ai.never.trust.model.*;
import org.yaml.snakeyaml.Yaml;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Parses YAML configuration files into EvalConfig domain objects.
 * Uses SnakeYAML for parsing. No reflection — manual mapping for clarity.
 */
public class YamlConfigParser {

    public EvalConfig parse(String path) throws IOException {
        Yaml yaml = new Yaml();
        try (InputStream in = new FileInputStream(path)) {
            Map<String, Object> raw = yaml.load(in);
            return parseEvalConfig(raw);
        }
    }

    @SuppressWarnings("unchecked")
    private EvalConfig parseEvalConfig(Map<String, Object> raw) {
        EvalConfig config = new EvalConfig();

        config.setName((String) raw.get("name"));

        if (raw.containsKey("testedAgent")) {
            config.setTestedAgent(parseTestedAgent((Map<String, Object>) raw.get("testedAgent")));
        }

        if (raw.containsKey("judges")) {
            List<Map<String, Object>> judgesRaw = (List<Map<String, Object>>) raw.get("judges");
            List<JudgeConfig> judges = new ArrayList<>();
            for (Map<String, Object> j : judgesRaw) {
                judges.add(parseJudgeConfig(j));
            }
            config.setJudges(judges);
        }

        if (raw.containsKey("metrics")) {
            config.setMetrics((List<String>) raw.get("metrics"));
        }

        if (raw.containsKey("report")) {
            config.setReport(parseReportConfig((Map<String, Object>) raw.get("report")));
        }

        if (raw.containsKey("scenarios")) {
            List<Map<String, Object>> scenariosRaw = (List<Map<String, Object>>) raw.get("scenarios");
            List<Scenario> scenarios = new ArrayList<>();
            for (Map<String, Object> s : scenariosRaw) {
                scenarios.add(parseScenario(s));
            }
            config.setScenarios(scenarios);
        }

        if (raw.containsKey("tests")) {
            List<Map<String, Object>> testsRaw = (List<Map<String, Object>>) raw.get("tests");
            List<TestCase> tests = new ArrayList<>();
            for (Map<String, Object> t : testsRaw) {
                tests.add(parseTestCase(t));
            }
            config.setTests(tests);
        }

        return config;
    }

    @SuppressWarnings("unchecked")
    private TestedAgent parseTestedAgent(Map<String, Object> raw) {
        TestedAgent agent = new TestedAgent();
        agent.setUrl((String) raw.get("url"));
        if (raw.containsKey("method")) agent.setMethod((String) raw.get("method"));
        if (raw.containsKey("headers")) agent.setHeaders((Map<String, String>) raw.get("headers"));
        return agent;
    }

    private JudgeConfig parseJudgeConfig(Map<String, Object> raw) {
        JudgeConfig judge = new JudgeConfig();
        judge.setModel((String) raw.get("model"));
        judge.setUrl((String) raw.get("url"));
        if (raw.containsKey("apiKey")) judge.setApiKey((String) raw.get("apiKey"));
        return judge;
    }

    private ReportConfig parseReportConfig(Map<String, Object> raw) {
        ReportConfig report = new ReportConfig();
        if (raw.containsKey("type")) report.setType((String) raw.get("type"));
        return report;
    }

    private Scenario parseScenario(Map<String, Object> raw) {
        Scenario scenario = new Scenario();
        scenario.setName((String) raw.get("name"));
        scenario.setDescription((String) raw.get("description"));
        return scenario;
    }

    @SuppressWarnings("unchecked")
    private TestCase parseTestCase(Map<String, Object> raw) {
        TestCase tc = new TestCase();
        tc.setName((String) raw.get("name"));
        if (raw.containsKey("description")) tc.setDescription((String) raw.get("description"));
        if (raw.containsKey("scenarioRef")) tc.setScenarioRef((String) raw.get("scenarioRef"));
        tc.setUserQuery((String) raw.get("userQuery"));
        if (raw.containsKey("expectedOutput")) tc.setExpectedOutput((String) raw.get("expectedOutput"));
        if (raw.containsKey("runs")) tc.setRuns((Integer) raw.get("runs"));
        if (raw.containsKey("judgeRuns")) tc.setJudgeRuns((Integer) raw.get("judgeRuns"));
        if (raw.containsKey("thresholds")) {
            Map<String, Object> raw_thresholds = (Map<String, Object>) raw.get("thresholds");
            Map<String, Double> thresholds = new java.util.LinkedHashMap<>();
            for (Map.Entry<String, Object> e : raw_thresholds.entrySet()) {
                thresholds.put(e.getKey(), ((Number) e.getValue()).doubleValue());
            }
            tc.setThresholds(thresholds);
        }
        return tc;
    }
}
