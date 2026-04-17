package ai.never.trust.interfaces.output;

/**
 * Represents the output produced by an agent under evaluation.
 * Can be a text response, an action, a tool call, or any other agent output type.
 *
 * <p>Implementations:</p>
 * <ul>
 *   <li>{@link TextOutput} - Simple text response from a chatbot</li>
 *   <li>{@link ActionOutput} - Action performed by the agent (booking, email, command)</li>
 * </ul>
 *
 * <p>To add a new output type, simply implement this interface.</p>
 */
public interface AgentOutput {
    String getType();
}
