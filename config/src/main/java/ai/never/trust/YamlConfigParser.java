package ai.never.trust;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.yaml.snakeyaml.Yaml;

import ai.never.trust.interfaces.config.ConfigParser;
import ai.never.trust.model.config.EvalConfig;
import ai.never.trust.model.config.JudgeConfig;
import ai.never.trust.model.config.MetricConfig;
import ai.never.trust.model.config.ReportConfig;
import ai.never.trust.model.config.TestedAgent;
import ai.never.trust.model.test.Scenario;
import ai.never.trust.model.test.TestCase;

public class YamlConfigParser implements ConfigParser {

    private final Yaml yaml = new Yaml();

    @Override
    public EvalConfig parse(String path) {
        try (FileInputStream fis = new FileInputStream(path)) {
            Map<String, Object> raw = yaml.load(fis);
            return parseEvalConfig(raw);
        } catch (IOException e) {
            throw new RuntimeException("Failed to parse config file: " + path, e);
        }
    }

    private EvalConfig parseEvalConfig(Map<String, Object> raw) {
        String name = (String) raw.get("name");
        TestedAgent testedAgent = parseTestedAgent(get(raw, "testedAgent"));
        List<JudgeConfig> judges = parseJudges(getList(raw, "judges"));
        List<MetricConfig> metrics = parseMetrics(getList(raw, "metrics"));
        ReportConfig report = parseReportConfig(get(raw, "report"));
        List<Scenario> scenarios = parseScenarios(getList(raw, "scenarios"));
        List<TestCase> tests = parseTests(getList(raw, "tests"));

        return new EvalConfig(name, testedAgent, judges, metrics, report, scenarios, tests);
    }

    private TestedAgent parseTestedAgent(Map<String, Object> raw) {
        if (raw == null) throw new RuntimeException("testedAgent is required");
        String url = (String) raw.get("url");
        String method = (String) raw.getOrDefault("method", "POST");
        Map<String, String> headers = getStringMap(raw, "headers");
        return new TestedAgent(url, method, headers);
    }

    private List<JudgeConfig> parseJudges(List<Map<String, Object>> rawList) {
        if (rawList == null) return List.of();
        return rawList.stream().map(raw -> {
            String model = (String) raw.get("model");
            String url = (String) raw.get("url");
            String apiKey = (String) raw.get("apiKey");
            return new JudgeConfig(model, url, apiKey);
        }).toList();
    }

    private List<MetricConfig> parseMetrics(List<Object> rawList) {
        if (rawList == null) return List.of();
        return rawList.stream()
                .map(item -> new MetricConfig((String) item))
                .toList();
    }

    private ReportConfig parseReportConfig(Map<String, Object> raw) {
        if (raw == null) return new ReportConfig("console");
        String type = (String) raw.getOrDefault("type", "console");
        return new ReportConfig(type);
    }

    private List<Scenario> parseScenarios(List<Map<String, Object>> rawList) {
        if (rawList == null) return List.of();
        return rawList.stream().map(raw -> {
            String name = (String) raw.get("name");
            String description = (String) raw.get("description");
            return new Scenario(name, description);
        }).toList();
    }

    private List<TestCase> parseTests(List<Map<String, Object>> rawList) {
        if (rawList == null) return List.of();
        return rawList.stream().map(raw -> {
            String name = (String) raw.get("name");
            String description = (String) raw.getOrDefault("description", "");
            String scenarioRef = (String) raw.get("scenarioRef");
            String userQuery = (String) raw.get("userQuery");
            String expectedOutput = (String) raw.get("expectedOutput");
            int runs = (int) raw.getOrDefault("runs", 100);
            Map<String, Double> thresholds = getDoubleMap(raw, "thresholds");
            return new TestCase(name, description, scenarioRef, userQuery, null, null, runs, thresholds);
        }).toList();
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> get(Map<String, Object> raw, String key) {
        return (Map<String, Object>) raw.get(key);
    }

    @SuppressWarnings("unchecked")
    private <T> List<T> getList(Map<String, Object> raw, String key) {
        return (List<T>) raw.get(key);
    }

    @SuppressWarnings("unchecked")
    private Map<String, String> getStringMap(Map<String, Object> raw, String key) {
        Object val = raw.get(key);
        if (val == null) return Map.of();
        return (Map<String, String>) val;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Double> getDoubleMap(Map<String, Object> raw, String key) {
        Object val = raw.get(key);
        if (val == null) return Map.of();
        Map<String, Object> map = (Map<String, Object>) val;
        Map<String, Double> result = new HashMap<>();
        map.forEach((k, v) -> result.put(k, ((Number) v).doubleValue()));
        return result;
    }

}
