package ai.never.trust.model.test;

/**
 * Describes the scenario used to evaluate the agent.
 * Represents the situation the agent is placed in during testing.
 * Acts as the "imagine that..." instruction for the evaluation context.
 *
 * @param name      Short identifier for this scenario.
 *                  Example: "indecisive-client"
 *
 * @param scenario  The full situation description.
 *                  Example: "A difficult client called who cannot decide
 *                           which day to schedule their appointment.
 *                           They mention being busy on Wednesday and
 *                           preferring Tuesday over other days."
 */
public record AgentContext(
    String name,
    String scenario
) {}