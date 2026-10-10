package ai.never.trust.judge;

import ai.never.trust.model.JudgeConfig;

/**
 * Creates the appropriate Judge implementation based on JudgeConfig.
 * Detects judge type by URL pattern.
 */
public class JudgeFactory {

    public static Judge create(JudgeConfig config) {
        String url = config.getUrl() != null ? config.getUrl().toLowerCase() : "";

        // Ollama: local, no API key, default port 11434
        if (config.isLocal() || url.contains("localhost") || url.contains("11434") || url.contains("ollama")) {
            return new OllamaJudge(config);
        }

        // For now, all cloud judges fall back to Ollama-compatible (OpenAI-compatible)
        // TODO: add OpenAI, Gemini, Claude judges in next iteration
        throw new UnsupportedOperationException(
                "Cloud judge not yet implemented for URL: " + config.getUrl() +
                "\nFor MVP, use Ollama (local) judges only."
        );
    }
}
