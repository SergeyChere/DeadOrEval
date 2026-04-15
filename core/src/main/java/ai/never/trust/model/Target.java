package ai.never.trust.model;

import java.util.Map;

public record Target(
    String url,
    String method,
    Map<String, String> headers
) {}
