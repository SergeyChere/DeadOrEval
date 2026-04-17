package ai.never.trust.model.test;

import java.util.Map;

import ai.never.trust.interfaces.output.AgentOutput;

/**
 * Represents a single test case for evaluating a chatbot response.
 * The referenced {@link Scenario} is sent once before all {@code runs} begin.
 * Then {@code userQuery} is executed {@code runs} times.
 *
 * @param name           Unique identifier for this test case.
 *                       Example: "indecisive-client-tuesday"
 *
 * @param description    Human-readable description of what this test case validates.
 *                       Example: "Verifies agent books correct day based on client availability"
 *
 * @param scenarioRef    Reference to a {@link Scenario} by its unique name.
 *                       The scenario is loaded once and reused across multiple test cases.
 *                       Example: "indecisive-client"
 *
 * @param userQuery      The message executed {@code runs} times against the agent.
 *                       Example: "I am not sure about Friday, Tuesday seems okay but Wednesday I am busy."
 *
 * @param agentOutput    The actual output produced by the agent being evaluated.
 *                       Can be a text response, an action, a tool call, or any other output type.
 *                       See {@link AgentOutput} implementations for supported types.
 *
 * @param expectedOutput The correct expected output based on scenario and user query.
 *                       Must be the same type as {@code agentOutput}.
 *                       See {@link AgentOutput} implementations for supported types.
 *
 * @param runs           Number of times userQuery is executed after scenario is sent.
 *                       Higher runs = more reliable consistency score.
 *                       Example: 1000
 *
 * @param thresholds     Minimum acceptable score per metric.
 *                       Example: {"accuracy": 0.9, "consistency": 0.85, "hallucination": 0.95}
 */
public record TestCase(
    String name,
    String description,
    String scenarioRef,
    String userQuery,
    AgentOutput agentOutput,
    AgentOutput expectedOutput,
    int runs,
    Map<String, Double> thresholds
) {}