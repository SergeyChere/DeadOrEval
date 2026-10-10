package ai.never.trust.engine;

import ai.never.trust.model.Scenario;
import ai.never.trust.model.TestedAgent;
import ai.never.trust.model.TestCase;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;

/**
 * Sends requests to the tested agent and returns its text response.
 * Builds a chat-style JSON payload with scenario context + user query.
 */
public class AgentCaller {

    private final HttpClient httpClient;

    public AgentCaller() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(30))
                .build();
    }

    /**
     * Call the agent with the given scenario and test case.
     *
     * @return The agent's text response
     */
    public String call(TestedAgent agent, Scenario scenario, TestCase testCase) throws IOException, InterruptedException {
        String body = buildRequestBody(scenario, testCase);

        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(agent.getUrl()))
                .header("Content-Type", "application/json")
                .timeout(Duration.ofSeconds(60));

        // Add custom headers
        if (agent.getHeaders() != null) {
            for (Map.Entry<String, String> header : agent.getHeaders().entrySet()) {
                builder.header(header.getKey(), header.getValue());
            }
        }

        String method = agent.getMethod() != null ? agent.getMethod().toUpperCase() : "POST";
        HttpRequest request = switch (method) {
            case "GET" -> builder.GET().build();
            case "POST" -> builder.POST(HttpRequest.BodyPublishers.ofString(body)).build();
            default -> throw new IllegalArgumentException("Unsupported HTTP method: " + method);
        };

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IOException("Agent returned HTTP " + response.statusCode() + ": " + response.body());
        }

        return extractText(response.body());
    }

    /**
     * Build a simple JSON payload compatible with OpenAI-style chat APIs.
     * Format: {"messages": [{"role": "system", "content": "..."}, {"role": "user", "content": "..."}]}
     */
    private String buildRequestBody(Scenario scenario, TestCase testCase) {
        StringBuilder messages = new StringBuilder("[");

        if (scenario != null && scenario.getDescription() != null) {
            messages.append("""
                    {"role":"system","content":"%s"}
                    """.formatted(escape(scenario.getDescription())).strip());
            messages.append(",");
        }

        messages.append("""
                {"role":"user","content":"%s"}
                """.formatted(escape(testCase.getUserQuery())).strip());

        messages.append("]");

        return "{\"messages\":" + messages + "}";
    }

    /**
     * Try to extract the text content from common API response formats.
     * Supports: OpenAI format, plain text, {"response":"..."}, {"text":"..."}, {"content":"..."}
     */
    private String extractText(String body) {
        body = body.trim();

        // OpenAI format: {"choices":[{"message":{"content":"..."}}]}
        if (body.contains("\"choices\"")) {
            int idx = body.indexOf("\"content\":");
            if (idx != -1) {
                return extractStringValue(body, idx + 10);
            }
        }

        // Simple formats
        for (String key : new String[]{"\"response\":", "\"text\":", "\"content\":", "\"answer\":"}) {
            int idx = body.indexOf(key);
            if (idx != -1) {
                return extractStringValue(body, idx + key.length());
            }
        }

        // Return as-is (plain text response)
        return body;
    }

    private String extractStringValue(String json, int startSearchFrom) {
        int start = json.indexOf('"', startSearchFrom);
        if (start == -1) return json;
        start++;
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
        return json.substring(start, end)
                .replace("\\n", "\n")
                .replace("\\\"", "\"")
                .replace("\\\\", "\\");
    }

    private String escape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
