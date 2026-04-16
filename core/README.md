# Core Module

The `core` module is the foundation of DeadOrEval.
It contains all domain models and interfaces used across all other modules.
No business logic. No implementations. Just contracts.

---

## Domain Models

### Scenario
Represents a reusable evaluation scenario.
Loaded once and referenced by multiple test cases.

```yaml
scenarios:
  - name: "indecisive-client"
    description: "A difficult client called who cannot decide
                  which day to schedule their appointment."

  - name: "angry-client"  
    description: "An angry client called demanding immediate appointment."
```

### TestCase
Represents a single test case for evaluating a chatbot response.
The referenced Scenario is sent once before all runs begin.
Then `userQuery` is executed `runs` times.

```yaml
tests:
  - name: "indecisive-client-tuesday"
    description: "Verifies agent books correct day based on client availability"
    scenarioRef: "indecisive-client"
    userQuery: "I am not sure about Friday, Tuesday seems okay but Wednesday I am busy."
    agentOutput: "I will book you for Wednesday."
    expectedOutput: "I will book you for Tuesday."
    runs: 1000
    thresholds:
      accuracy: 0.9
      consistency: 0.85
      hallucination: 0.95
```

## How Scenario and TestCase work together

## How Scenario and TestCase work together

**Scenarios are parsed once from config:**
- `indecisive-client` — A difficult client who cannot decide which day to schedule
- `angry-client` — An angry client demanding immediate appointment
- `confused-client` — A confused elderly client who keeps forgetting what was said

**Each test references a scenario and runs userQuery N times:**

| Test   | scenarioRef       | userQuery                          | runs |
|--------|-------------------|------------------------------------|------|
| test-1 | indecisive-client | "I am not sure about Friday..."    | 1000 |
| test-2 | angry-client      | "I need appointment NOW"           | 500  |
| test-3 | indecisive-client | "Maybe Monday? Tuesday was fine."  | 1000 |

---

## Interfaces

| Interface       | Responsibility                              |
|-----------------|---------------------------------------------|
| `Judge`         | Evaluates agent response, returns score     |
| `Metric`        | Calculates a specific metric score          |
| `Reporter`      | Generates evaluation report                 |
| `EvalEngine`    | Orchestrates the full evaluation run        |
| `ConfigParser`  | Parses YAML config into domain models       |
| `WarmupStrategy`| Calibrates judges before evaluation begins  |
| `TagMerger`     | TBD                                         |

## Dependency Rule

- `core` has no dependencies
- everything else depends on `core`

This ensures core can never be broken by other modules
and is ready to be extracted into a standalone microservice.

## Module Structure

```
core/
└── src/main/java/ai/never/trust/
    ├── model/
    │   ├── config/
    │   ├── result/
    │   ├── test/
    │   └── warmup/
    └── interfaces/
        ├── judge/
        ├── metric/
        ├── reporter/
        ├── engine/
        └── config/
```

