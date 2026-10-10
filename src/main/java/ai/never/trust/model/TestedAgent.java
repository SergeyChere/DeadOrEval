package ai.never.trust.model;

import java.util.Map;

/**
 * Defines how DeadOrEval connects to the agent being tested.
 */
public class TestedAgent {
    private String url;
    private String method = "POST";
    private Map<String, String> headers;

    public TestedAgent() {}

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public String getMethod() { return method; }
    public void setMethod(String method) { this.method = method; }

    public Map<String, String> getHeaders() { return headers; }
    public void setHeaders(Map<String, String> headers) { this.headers = headers; }

    @Override
    public String toString() {
        return "TestedAgent{url='" + url + "', method='" + method + "'}";
    }
}
