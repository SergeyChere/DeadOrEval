package ai.never.trust.model.config;

/**
 * Configuration for a judge used to evaluate agent responses.
 * Defines which model to use and how to connect to it.
 *
 * @param model   The model name to use for evaluation.
 *                Example: "llama3.2:3b", "gpt-4o", "gemini-pro"
 *
 * @param url     The endpoint URL of the judge service.
 *                Example: "http://localhost:11434", "https://api.openai.com"
 *
 * @param apiKey  The API key required to authenticate with the judge service.
 *                Example: "sk-...", "AI..."
 */
public record JudgeConfig(
    String model,
    String url,
    String apiKey
) {}
