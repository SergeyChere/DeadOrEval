package ai.never.trust.model.test;

import java.util.Map;

/**
 * Represents a single test case for evaluating a chatbot response.
 * Contains all the information needed to evaluate one interaction.
 *
 * @param context  The background information or knowledge base the chatbot should use.
 *                 Example: "Dental clinic working hours are Mon-Fri 9am-5pm"
 * @param question The question asked to the chatbot.
 *                 Example: "Are you open on Saturday?"
 * @param answer   The actual response given by the chatbot being evaluated.
 *                 Example: "Yes, we are open on Saturday"
 * @param expected The correct expected answer to compare against.
 *                 Example: "No, we are closed on Saturday"
 */
public record TestCase(
    String name,
    String description,
    String context,
    String userQuery,
    String agentOutput,
    String expectedOutput,
    int runs,
    Map<String, Double> thresholds
) {}
