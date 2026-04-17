package ai.never.trust;

import java.util.Map;

import ai.never.trust.interfaces.output.AgentOutput;


/**
 * Represents an action performed by the agent instead of a text response.
 * Used when the agent executes a real-world operation such as
 * booking an appointment, sending an email, or issuing a drone command.
 *
 * @param actionType   The type of action performed by the agent.
 *                     Example: "booking", "email", "drone_command"
 */
public record ActionOutput(
    String content
) implements AgentOutput {

    @Override
    public String getType() {
        return "action";
    }
}
