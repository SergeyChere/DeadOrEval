package ai.never.trust;

import ai.never.trust.config.YamlConfigParser;
import ai.never.trust.model.EvalConfig;
import org.junit.jupiter.api.Test;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class YamlConfigParserTest {

    @Test
    void parsesMinimalConfig() throws IOException {
        String yaml = """
                name: test-eval
                testedAgent:
                  url: http://localhost:8080/api/chat
                  method: POST
                judges:
                  - model: llama3.2:3b
                    url: http://localhost:11434
                metrics:
                  - accuracy
                  - consistency
                report:
                  type: console
                scenarios:
                  - name: basic
                    description: A user asks a question.
                tests:
                  - name: basic-test
                    scenarioRef: basic
                    userQuery: What are your hours?
                    expectedOutput: We are open 9am to 5pm.
                    runs: 5
                    thresholds:
                      accuracy: 0.8
                      consistency: 0.75
                """;

        Path tmp = Files.createTempFile("doe-test-", ".yaml");
        Files.writeString(tmp, yaml);

        YamlConfigParser parser = new YamlConfigParser();
        EvalConfig config = parser.parse(tmp.toString());

        assertEquals("test-eval", config.getName());
        assertNotNull(config.getTestedAgent());
        assertEquals("http://localhost:8080/api/chat", config.getTestedAgent().getUrl());
        assertEquals(1, config.getJudges().size());
        assertEquals("llama3.2:3b", config.getJudges().get(0).getModel());
        assertEquals(2, config.getMetrics().size());
        assertEquals(1, config.getScenarios().size());
        assertEquals("basic", config.getScenarios().get(0).getName());
        assertEquals(1, config.getTests().size());
        assertEquals(5, config.getTests().get(0).getRuns());
        assertEquals(0.8, config.getTests().get(0).getThreshold("accuracy"), 0.001);

        Files.deleteIfExists(tmp);
    }

    @Test
    void findScenarioByName() throws IOException {
        String yaml = """
                name: test
                testedAgent:
                  url: http://localhost:8080
                judges:
                  - model: llama3.2:3b
                    url: http://localhost:11434
                scenarios:
                  - name: scenario-a
                    description: First scenario
                  - name: scenario-b
                    description: Second scenario
                tests:
                  - name: t1
                    scenarioRef: scenario-a
                    userQuery: hello
                    runs: 1
                """;

        Path tmp = Files.createTempFile("doe-test-", ".yaml");
        Files.writeString(tmp, yaml);

        YamlConfigParser parser = new YamlConfigParser();
        EvalConfig config = parser.parse(tmp.toString());

        assertNotNull(config.findScenario("scenario-a"));
        assertNotNull(config.findScenario("scenario-b"));
        assertNull(config.findScenario("non-existent"));

        Files.deleteIfExists(tmp);
    }
}
