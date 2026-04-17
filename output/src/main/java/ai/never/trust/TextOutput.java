package ai.never.trust;

import ai.never.trust.interfaces.output.AgentOutput;

/**
 * Represents a plain text response from a chatbot agent.
 * The most common output type for conversational agents.
 *
 * @param content  The text response produced by the agent.
 *                 Example: "I will book you for Tuesday."
 */
public record TextOutput(
    String content
) implements AgentOutput {

    @Override
    public String getType() {
        return "text";
    }
}
