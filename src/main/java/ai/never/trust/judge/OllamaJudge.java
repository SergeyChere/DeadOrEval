package ai.never.trust.judge;

import ai.never.trust.model.JudgeConfig;
import ai.never.trust.model.JudgeOutput;
import ai.never.trust.model.Scenario;
import ai.never.trust.model.TestCase;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * Judge implementation for local Ollama models.
 * Calls the Ollama /api/generate endpoint.
 *
 * The judge sends a structured prompt asking the model to score the agent response
 * from 0.0 to 1.0 and returns the parsed score.
 */
public class OllamaJudge implements Judge {

    private static final String JUDGE_PROMPT_TEMPLATE = """
            You are an evaluator assessing a chatbot response.

            CONTEXT:
            %s

            USER QUERY:
            %s

            EXPECTED OUTPUT:
            %s

            ACTUAL AGENT RESPONSE:
            %s

            Evaluate the agent response. Consider:
            1. Does the response correctly address the user query?
            2. Is the response consistent with the expected output?
            3. Is the response factually accurate given the context?

            Respond with ONLY a JSON object in this exact format:
            {"score": 0.85, "reasoning": "Brief explanation"}

            Score must be between 0.0 (completely wrong) and 1.0 (perfect).
            """;

    private final JudgeConfig config;
    private final HttpClient httpClient;
    private final String name;

    public OllamaJudge(JudgeConfig config) {
        this.config = config;
        this.name = "ollama/" + config.getModel();
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(30))
                .build();
    }

    @Override
    public JudgeOutput judge(Scenario scenario, TestCase testCase, String agentOutput) {
        String context = scenario != null ? scenario.getDescription() : "No context provided.";
        String expectedOutput = testCase.getExpectedOutput() != null ? testCase.getExpectedOutput() : "Not specified.";

        String prompt = JUDGE_PROMPT_TEMPLATE.formatted(
                context,
                testCase.getUserQuery(),
                expectedOutput,
                agentOutput
        );

        try {
            String response = callOllama(prompt);
            return parseResponse(response);
        } catch (Exception e) {
            System.err.println("[" + name + "] Error during evaluation: " + e.getMessage());
            return new JudgeOutput(name, 0.0, "Error: " + e.getMessage());
        }
    }

    private String callOllama(String prompt) throws IOException, InterruptedException {
        String url = config.getUrl() + "/api/generate";
        String body = buildRequestBody(prompt);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .timeout(Duration.ofSeconds(120))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new IOException("Ollama returned HTTP " + response.statusCode());
        }

        return extractTextFromOllamaResponse(response.body());
    }

    private String buildRequestBody(String prompt) {
        // Escape the prompt for JSON
        String escaped = prompt
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");

        return """
               {
                 "model": "%s",
                 "prompt": "%s",
                 "stream": false,
                 "options": {
                   "temperature": 0.1
                 }
               }
               """.formatted(config.getModel(), escaped);
    }

    /**
     * Ollama returns newline-delimited JSON chunks.
     * We look for the chunk with "done":true and extract its "response" field.
     * Or if stream:false, there's one JSON object with "response".
     */
    private String extractTextFromOllamaResponse(String rawResponse) {
        // Find "response":"..." in the JSON
        int idx = rawResponse.indexOf("\"response\":");
        if (idx == -1) return rawResponse;

        int start = rawResponse.indexOf('"', idx + 11) + 1;
        int end = start;
        boolean escaped = false;
        while (end < rawResponse.length()) {
            char c = rawResponse.charAt(end);
            if (escaped) {
                escaped = false;
            } else if (c == '\\') {
                escaped = true;
            } else if (c == '"') {
                break;
            }
            end++;
        }
        return rawResponse.substring(start, end)
                .replace("\\n", "\n")
                .replace("\\\"", "\"")
                .replace("\\\\", "\\");
    }

    private JudgeOutput parseResponse(String response) {
        // Try to extract JSON from response — model may wrap it in text
        String json = extractJson(response);
        if (json == null) {
            // Fallback: try to find a number in the response
            double score = extractScoreFallback(response);
            return new JudgeOutput(name, score, "Parsed from raw response");
        }

        double score = parseJsonDouble(json, "score");
        String reasoning = parseJsonString(json, "reasoning");

        // Clamp to [0.0, 1.0]
        score = Math.max(0.0, Math.min(1.0, score));
        return new JudgeOutput(name, score, reasoning);
    }

    private String extractJson(String text) {
        int start = text.indexOf('{');
        int end = text.lastIndexOf('}');
        if (start == -1 || end == -1 || end <= start) return null;
        return text.substring(start, end + 1);
    }

    private double parseJsonDouble(String json, String key) {
        String search = "\"" + key + "\":";
        int idx = json.indexOf(search);
        if (idx == -1) return 0.5;
        int start = idx + search.length();
        while (start < json.length() && (json.charAt(start) == ' ' || json.charAt(start) == '\t')) start++;
        int end = start;
        while (end < json.length() && (Character.isDigit(json.charAt(end)) || json.charAt(end) == '.' || json.charAt(end) == '-')) end++;
        try {
            return Double.parseDouble(json.substring(start, end));
        } catch (NumberFormatException e) {
            return 0.5;
        }
    }

    private String parseJsonString(String json, String key) {
        String search = "\"" + key + "\":";
        int idx = json.indexOf(search);
        if (idx == -1) return "";
        int start = json.indexOf('"', idx + search.length()) + 1;
        int end = start;
        boolean escaped = false;
        while (end < json.length()) {
            char c = json.charAt(end);
            if (escaped) {
                escaped = false;
            } else if (c == '\\') {
                escaped = true;
            } else if (c == '"') {
                break;
            }
            end++;
        }
        return json.substring(start, end);
    }

    private double extractScoreFallback(String text) {
        // Look for a number between 0 and 1 or 0 and 10 (normalize)
        java.util.regex.Pattern p = java.util.regex.Pattern.compile("\\b(\\d+(?:\\.\\d+)?)\\b");
        java.util.regex.Matcher m = p.matcher(text);
        while (m.find()) {
            try {
                double v = Double.parseDouble(m.group(1));
                if (v >= 0 && v <= 1) return v;
                if (v > 1 && v <= 10) return v / 10.0;
            } catch (NumberFormatException ignored) {}
        }
        return 0.5;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean isAvailable() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(config.getUrl() + "/api/tags"))
                    .GET()
                    .timeout(Duration.ofSeconds(5))
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return response.statusCode() == 200;
        } catch (Exception e) {
            return false;
        }
    }
}
