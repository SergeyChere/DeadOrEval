# Core Module

The `core` module is the foundation of DeadOrEval.
It contains all domain models and interfaces used across all other modules.
No business logic. No implementations. Just contracts.

---

## Config 

### EvalSuite
Groups multiple evaluation configs into a single batch run, perfect for overnight testing.
Fields defined in the suite act as defaults for all configs and can be overridden individually.

### EvalConfig
The main configuration for a single DeadOrEval evaluation run.
Defines the agent under test, judges, metrics, scenarios and test cases.

### TestedAgent
Defines how DeadOrEval connects to the agent being tested.

### JudgeConfig
Defines which judge to use and how to connect to it.
For reliable consensus evaluation, use an odd number of judges.

### MetricConfig
Defines which metrics to calculate during evaluation.

### ReportConfig
Defines how evaluation results are reported.

## Result

//TODO

## Test

### TestCase
Represents a single test case for evaluating a chatbot response.
The referenced Scenario is sent once before all runs begin.
Then `userQuery` is executed `runs` times.

### Scenario
Represents a reusable evaluation scenario.
Loaded once and referenced by multiple test cases.

## Warmup

//TODO

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
| `AgentOutput`   | Represents output produced by the agent — text, action, or any custom type                                      |

## Dependency Rule

- `core` has no dependencies
- everything else depends on `core`

This ensures core can never be broken by other modules
and is ready to be extracted into a standalone microservice.