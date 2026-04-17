package ai.never.trust.model.config;

import java.util.List;

/**
 * Represents a batch of evaluation configurations to run together.
 * Designed for overnight or scheduled test runs.
 *
 * @param name     Unique identifier for this test suite.
 *                 Example: "dental-bot-full-suite"
 *
 * @param configs  List of paths to individual {@link EvalConfig} YAML files.
 *                 Example: ["configs/indecisive-client.yaml", "configs/angry-client.yaml"]
 */
public record EvalSuite(
    String name,
    List<String> configs
) {}
