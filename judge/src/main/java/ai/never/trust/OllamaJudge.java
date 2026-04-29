package ai.never.trust;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ai.never.trust.interfaces.judge.Judge;
import ai.never.trust.model.config.JudgeConfig;
import ai.never.trust.model.result.JudgeOutput;
import ai.never.trust.model.test.TestCase;

public class OllamaJudge implements Judge {

    private static final Pattern SCORE_PATTERN = Pattern.compile("\\d+\\.\\d+|\\d+");
 
    private final JudgeConfig config;
    private final HttpClient httpClient;
 
    public OllamaJudge(JudgeConfig config) {
        this.config = config;
        this.httpClient = HttpClient.newHttpClient();
    }
 
    @Override
    public JudgeOutput judge(TestCase testCase) {
        String prompt = buildPrompt(testCase);
        String rawResponse = sendToOllama(prompt);
        double score = parseScore(rawResponse);
        return new JudgeOutput(score, List.of());
    }
 
    @Override
    public String getName() {
        return "ollama/" + config.model();
    }
 
    @Override
    public boolean isAvailable() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(config.url() + "/api/tags"))
                    .GET()
                    .build();
            HttpResponse<String> response = httpClient.send(
                    request, HttpResponse.BodyHandlers.ofString());
            return response.statusCode() == 200;
        } catch (Exception e) {
            return false;
        }
    }
 
    private String buildPrompt(TestCase testCase) {
        return "You are a strict factual judge evaluating an AI agent response. " +
               "Your task is to evaluate if the agent response is correct based on the user query and expected output. " +
               "Respond ONLY with a single decimal number between 0.0 and 1.0. No words. No explanation. Just the number. " +
               "1.0 means the agent response is 100% correct. " +
               "0.0 means the agent response is 100% wrong. " +
               "0.5 means the agent response is partially correct.\\n" +
               "User query: " + testCase.userQuery() + "\\n" +
               "Agent response: " + (testCase.agentOutput() != null ? testCase.agentOutput().toString() : "") + "\\n" +
               "Expected output: " + testCase.expectedOutput();
    }
 
    private String sendToOllama(String prompt) {
        try {
            String body = "{\"model\":\"" + config.model() + "\"," +
                         "\"prompt\":\"" + prompt + "\"," +
                         "\"stream\":false}";
 
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(config.url() + "/api/generate"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();
 
            HttpResponse<String> response = httpClient.send(
                    request, HttpResponse.BodyHandlers.ofString());
 
            return extractResponse(response.body());
        } catch (Exception e) {
            throw new RuntimeException("Failed to send request to Ollama: " + e.getMessage(), e);
        }
    }
 
    private String extractResponse(String rawBody) {
        int start = rawBody.indexOf("\"response\":\"") + 12;
        int end = rawBody.indexOf("\"", start);
        if (start < 12 || end < 0) return "";
        return rawBody.substring(start, end);
    }
 
    private double parseScore(String text) {
        Matcher matcher = SCORE_PATTERN.matcher(text);
        if (matcher.find()) {
            double score = Double.parseDouble(matcher.group());
            if (score > 1.0) score = score / 10.0;
            return Math.min(1.0, Math.max(0.0, score));
        }
        return 0.0;
    }
}
