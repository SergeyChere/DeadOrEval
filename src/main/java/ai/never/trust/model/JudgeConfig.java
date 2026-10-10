package ai.never.trust.model;

/**
 * Defines which judge model to use and how to connect to it.
 * Supports Ollama (local), OpenAI, Gemini, Claude.
 */
public class JudgeConfig {
    private String model;
    private String url;
    private String apiKey;

    public JudgeConfig() {}

    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public String getApiKey() { return apiKey; }
    public void setApiKey(String apiKey) { this.apiKey = apiKey; }

    public boolean isLocal() {
        return apiKey == null || apiKey.isBlank();
    }

    @Override
    public String toString() {
        return "JudgeConfig{model='" + model + "', url='" + url + "', local=" + isLocal() + "}";
    }
}
