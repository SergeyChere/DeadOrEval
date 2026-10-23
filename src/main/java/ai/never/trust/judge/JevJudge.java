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
 * Judge implementation for Jev-compatible models (OpenJev, JEV-9B, TypeSafe API).
 *
 * Uses the /v1/systemone endpoint which returns typed, calibrated decisions
 * directly — no text parsing, no hallucinations, one forward pass.
 *
 * Compatible with:
 *   - openjev/openjev  (27B, CC BY-NC, self-hosted via vLLM)
 *   - autotrust/JEV-9B (9B, Apache 2.0, self-hosted via vLLM or Transformers)
 *   - TypeSafe API     (cloud, api.typesafe.ai)
 *
 * Config example:
 *   judges:
 *     - model: openjev
 *       url: http://localhost:8080
 *     - model: jev-latest
 *       url: https://api.typesafe.ai
 *       apiKey: ts-...
 */
public class JevJudge implements Judge {

    private static final String ENDPOINT = "/v1/systemone";

    private final JudgeConfig config;
    private final HttpClient httpClient;
    private final String name;

    public JevJudge(JudgeConfig config) {
        this.config = config;
        this.name = "jev/" + config.getModel();
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    @Override
    public JudgeOutput judge(Scenario scenario, TestCase testCase, String agentOutput) {
        String context = scenario != null ? scenario.getDescription() : "";
        String expected = testCase.getExpectedOutput() != null ? testCase.getExpectedOutput() : "";

        String state = buildState(context, testCase.getUserQuery(), expected, agentOutput);
        String body = buildRequestBody(state);

        try {
            String url = config.getUrl() + ENDPOINT;
            HttpRequest.Builder req = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .timeout(Duration.ofSeconds(30));

            if (config.getApiKey() != null && !config.getApiKey().isBlank()) {
                req.header("Authorization", "Bearer " + config.getApiKey());
            }

            HttpResponse<String> response = httpClient.send(req.build(), HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new IOException("Jev returned HTTP " + response.statusCode() + ": " + response.body());
            }

            return parseResponse(response.body());

        } catch (Exception e) {
            System.err.println("[" + name + "] Error: " + e.getMessage());
            return new JudgeOutput(name, 0.5, "Error: " + e.getMessage());
        }
    }

    /**
     * Builds a concise state string for Jev.
     * Jev doesn't need a prompt template — the state is just the facts.
     */
    private String buildState(String context, String userQuery, String expected, String agentOutput) {
        StringBuilder sb = new StringBuilder();
        if (!context.isBlank()) {
            sb.append("Context: ").append(context).append("\n\n");
        }
        sb.append("User query: ").append(userQuery).append("\n\n");
        if (!expected.isBlank()) {
            sb.append("Expected answer: ").append(expected).append("\n\n");
        }
        sb.append("Agent response: ").append(agentOutput);
        return sb.toString();
    }

    /**
     * Builds the /v1/systemone request body.
     *
     * We ask two typed questions:
     *   - quality: score 0–4 (poor/weak/fair/good/excellent)
     *   - hallucination: noul (yes/no — did the agent fabricate something?)
     *
     * Jev returns calibrated probabilities for each level, so we can compute
     * an expected value as a [0.0–1.0] score.
     */
    private String buildRequestBody(String state) {
        String escapedState = escapeJson(state);
        return """
               {
                 "model": "%s",
                 "state": "%s",
                 "questions": {
                   "quality": {
                     "type": "score",
                     "instructions": "How well does the agent response answer the user query, given the expected answer?",
                     "criteria": ["poor", "weak", "fair", "good", "excellent"]
                   },
                   "hallucination": {
                     "type": "noul",
                     "instructions": "Does the agent response contain fabricated information not supported by the context or expected answer?"
                   }
                 }
               }
               """.formatted(config.getModel(), escapedState);
    }

    /**
     * Parses the /v1/systemone response.
     *
     * Response shape:
     * {
     *   "answers": {
     *     "quality": {
     *       "score": "good",
     *       "probabilities": {"poor":0.01,"weak":0.05,"fair":0.10,"good":0.60,"excellent":0.24},
     *       "confidence": 0.84
     *     },
     *     "hallucination": {
     *       "noul": 0.07,
     *       "confidence": 0.91
     *     }
     *   },
     *   "usage": { "input_tokens": 120, "output_tokens": 0 }
     * }
     *
     * We compute a [0.0–1.0] score as the weighted expected value of quality levels,
     * then apply a hallucination penalty.
     */
    private JudgeOutput parseResponse(String json) {
        // Extract quality probabilities
        double poor      = extractProb(json, "poor");
        double weak      = extractProb(json, "weak");
        double fair      = extractProb(json, "fair");
        double good      = extractProb(json, "good");
        double excellent = extractProb(json, "excellent");

        // Weighted expected value: poor=0, weak=0.25, fair=0.5, good=0.75, excellent=1.0
        double qualityScore = poor * 0.0 + weak * 0.25 + fair * 0.5 + good * 0.75 + excellent * 1.0;

        // Normalize in case probabilities don't sum to exactly 1.0
        double total = poor + weak + fair + good + excellent;
        if (total > 0) qualityScore /= total;

        // Hallucination: noul is probability of "yes, it hallucinated"
        double hallucinationProb = extractJsonDouble(json, "noul");

        // Penalty: if hallucination prob > 0.5, cap score at 0.3
        double finalScore;
        String reasoning;
        if (hallucinationProb > 0.5) {
            finalScore = Math.min(qualityScore, 0.3);
            reasoning = String.format("quality=%.2f hallucination=%.2f (penalty applied)", qualityScore, hallucinationProb);
        } else {
            finalScore = qualityScore;
            reasoning = String.format("quality=%.2f hallucination=%.2f", qualityScore, hallucinationProb);
        }

        double confidence = extractJsonDouble(json, "confidence");

        finalScore = Math.max(0.0, Math.min(1.0, finalScore));
        JudgeOutput out = new JudgeOutput(name, finalScore, reasoning);
        out.setConfidence(confidence);
        return out;
    }

    /** Extract a probability value for a specific label key from the JSON blob. */
    private double extractProb(String json, String label) {
        return extractJsonDouble(json, "\"" + label + "\"");
    }

    private double extractJsonDouble(String json, String key) {
        int idx = json.indexOf(key);
        if (idx == -1) return 0.0;
        // Find the colon or just the value after the key
        int colon = json.indexOf(':', idx + key.length());
        if (colon == -1) {
            // key might be a value itself (like "noul": 0.07)
            // try treating the key as a field name
            String search = "\"" + key.replace("\"", "") + "\":";
            idx = json.indexOf(search);
            if (idx == -1) return 0.0;
            colon = idx + search.length() - 1;
        }
        int start = colon + 1;
        while (start < json.length() && (json.charAt(start) == ' ' || json.charAt(start) == '\t')) start++;
        int end = start;
        while (end < json.length() && (Character.isDigit(json.charAt(end)) || json.charAt(end) == '.' || json.charAt(end) == '-')) end++;
        try {
            return Double.parseDouble(json.substring(start, end));
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    private String escapeJson(String text) {
        return text
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean isAvailable() {
        try {
            // Check the /health or /v1/models endpoint
            String healthUrl = config.getUrl() + "/health";
            HttpRequest.Builder req = HttpRequest.newBuilder()
                    .uri(URI.create(healthUrl))
                    .GET()
                    .timeout(Duration.ofSeconds(5));

            if (config.getApiKey() != null && !config.getApiKey().isBlank()) {
                req.header("Authorization", "Bearer " + config.getApiKey());
            }

            HttpResponse<String> response = httpClient.send(req.build(), HttpResponse.BodyHandlers.ofString());
            return response.statusCode() == 200;
        } catch (Exception e) {
            return false;
        }
    }
}
