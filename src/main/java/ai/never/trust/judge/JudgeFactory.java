package ai.never.trust.judge;

import ai.never.trust.model.JudgeConfig;

/**
 * Creates the appropriate Judge implementation based on JudgeConfig.
 * Detects judge type by URL pattern.
 */
public class JudgeFactory {

    public static Judge create(JudgeConfig config) {
        String url   = config.getUrl()   != null ? config.getUrl().toLowerCase()   : "";
        String model = config.getModel() != null ? config.getModel().toLowerCase() : "";

        // Jev-compatible models: openjev, jev-9b, jev-latest, jev-1.x, typesafe
        if (isJevModel(model) || isJevUrl(url)) {
            return new JevJudge(config);
        }

        // Ollama: local, no API key, default port 11434
        if (config.isLocal() || url.contains("11434") || url.contains("ollama")) {
            return new OllamaJudge(config);
        }

        throw new UnsupportedOperationException(
                "Unknown judge type for model='" + config.getModel() + "' url='" + config.getUrl() + "'.\n" +
                "Supported: jev (openjev / jev-9b / jev-latest) or ollama (llama3.2:3b etc.)"
        );
    }

    private static boolean isJevModel(String model) {
        return model.startsWith("jev") || model.startsWith("openjev") || model.contains("typesafe");
    }

    private static boolean isJevUrl(String url) {
        return url.contains("typesafe.ai") || url.contains("openjev") || url.contains("systemone");
    }
}
