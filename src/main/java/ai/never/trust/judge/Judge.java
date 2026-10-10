package ai.never.trust.judge;

import ai.never.trust.model.JudgeOutput;
import ai.never.trust.model.Scenario;
import ai.never.trust.model.TestCase;

/**
 * Contract for all judge implementations.
 * A judge evaluates an agent's response and returns a score between 0.0 and 1.0.
 */
public interface Judge {

    /**
     * Evaluate an agent response.
     *
     * @param scenario  The scenario context (may be null)
     * @param testCase  The test case being evaluated
     * @param agentOutput The actual output from the agent
     * @return JudgeOutput with score in [0.0, 1.0]
     */
    JudgeOutput judge(Scenario scenario, TestCase testCase, String agentOutput);

    /**
     * @return The unique name of this judge (e.g. "ollama/llama3.2:3b")
     */
    String getName();

    /**
     * @return true if the judge service is reachable
     */
    boolean isAvailable();
}
