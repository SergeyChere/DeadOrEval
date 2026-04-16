package ai.never.trust.model.config;

import java.util.Map;

/**
 * Represents the target chatbot endpoint under evaluation.
 * Defines how DeadOrEval connects to the agent being tested.
 *
 * @param url      The endpoint URL of the agent being evaluated.
 *                 Example: "https://my-dental-bot.com/api/chat"
 *
 * @param method   The HTTP method used to send requests to the agent.
 *                 Example: "POST"
 *
 * @param headers  Additional HTTP headers required by the agent.
 *                 Example: {"Authorization": "Bearer token", "Content-Type": "application/json"}
 */
public record Target(
    String url,
    String method,
    Map<String, String> headers
) {}
