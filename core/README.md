# Core Module

The `core` module is the foundation of DeadOrEval.
It contains all domain models and interfaces used across all other modules.
No business logic. No implementations. Just contracts.

---

## Configuration

### Test Suite
Group multiple evaluation configs into a single batch run.
Perfect for overnight testing.

```yaml
suite:
  name: "dental-bot-full-suite"
  configs:
    - "configs/indecisive-client.yaml"
    - "configs/angry-client.yaml"
    - "configs/confused-client.yaml"
    - "configs/booking-edge-cases.yaml"
    - "configs/cancellation-flow.yaml"
```

---

### EvalConfig
The main configuration for a DeadOrEval evaluation run.

```yaml
name: "dental-bot-eval-v1"
testedAgent:
  url: "https://my-dental-bot.com/api/chat"
  method: "POST"
  headers:
    Authorization: "Bearer token"
judges:
  - model: "llama3.2:3b"
    url: "http://localhost:11434"
  - model: "gpt-4o"
    url: "https://api.openai.com"
    apiKey: "sk-..."
  - model: "gemini-pro"
    url: "https://generativelanguage.googleapis.com"
    apiKey: "AI..."
metrics:
  - accuracy
  - consistency
  - hallucination
report:
  type: "html"
tests:
  - name: "indecisive-client-tuesday"
    scenarioRef: "indecisive-client"
    userQuery: "I am not sure about Friday, Tuesday seems okay but Wednesday I am busy."
    expectedOutput: "I will book you for Tuesday."
    runs: 1000
    thresholds:
      accuracy: 0.9
      consistency: 0.85

  - name: "indecisive-client-friday"
    scenarioRef: "indecisive-client"
    userQuery: "Actually Friday might work, I am just not sure."
    expectedOutput: "Would Tuesday still work for you?"
    runs: 1000
    thresholds:
      accuracy: 0.9
      consistency: 0.85

  - name: "angry-client-now"
    scenarioRef: "angry-client"
    userQuery: "I need an appointment right now, this is urgent."
    expectedOutput: "I understand, the earliest available slot is Tuesday at 10:00."
    runs: 500
    thresholds:
      accuracy: 0.85
      consistency: 0.80
```

| Field          | Description                                    |
|----------------|------------------------------------------------|
| `name`         | Unique identifier for this evaluation run      |
| `testedAgent`  | The agent under evaluation                     |
| `judges`       | Odd number of judges for consensus evaluation  |
| `metrics`      | List of metrics to calculate                   |
| `report`       | How to report evaluation results               |
| `tests`        | List of test cases to execute                  |

---

### TestedAgent
Defines how DeadOrEval connects to the agent being tested.

```yaml
testedAgent:
  url: "https://my-dental-bot.com/api/chat"
  method: "POST"
  headers:
    Authorization: "Bearer token"
    Content-Type: "application/json"
```

| Field     | Description                              |
|-----------|------------------------------------------|
| `url`     | Endpoint URL of the agent under test     |
| `method`  | HTTP method used to send requests        |
| `headers` | Additional HTTP headers if required      |

---

### JudgeConfig
Defines which judge to use and how to connect to it.
For reliable consensus evaluation, use an odd number of judges.

```yaml
judges:
  - model: "llama3.2:3b"
    url: "http://localhost:11434"
  - model: "gpt-4o"
    url: "https://api.openai.com"
    apiKey: "sk-..."
  - model: "gemini-pro"
    url: "https://generativelanguage.googleapis.com"
    apiKey: "AI..."
```

| Field    | Description                                    |
|----------|------------------------------------------------|
| `model`  | Model name to use for evaluation               |
| `url`    | Endpoint URL of the judge service              |
| `apiKey` | API key to authenticate with the judge service |

---

### MetricConfig
Defines which metrics to calculate during evaluation.

```yaml
metrics:
  - accuracy
  - consistency
  - hallucination
  - incident_tracking
```

| Value               | Description                                          |
|---------------------|------------------------------------------------------|
| `accuracy`          | Measures correctness of agent responses              |
| `consistency`       | Measures stability of responses across multiple runs |
| `hallucination`     | Detects fabricated information not in context        |
| `incident_tracking` | Tracks responses that fall below acceptable score    |

To add a custom metric, implement the `Metric` interface in the `metrics` module.

---

### ReportConfig
Defines how evaluation results are reported.

```yaml
report:
  type: "html"
```

| Field  | Description                                      |
|--------|--------------------------------------------------|
| `type` | Type of report: `console`, `html`, `json`, `pdf` |

---

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

---

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

---

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

<details>
<summary>Full configuration example</summary>

```yaml
# ===========================
# Test Suite
# ===========================
suite:
  name: "dental-bot-full-suite"
  configs:
    - "configs/booking-flow.yaml"
    - "configs/cancellation-flow.yaml"
    - "configs/edge-cases.yaml"

# ===========================
# Scenarios
# ===========================
scenarios:
  - name: "indecisive-client"
    description: "A difficult client called who cannot decide
                  which day to schedule their appointment.
                  They mention being busy on Wednesday and
                  preferring Tuesday over other days."

  - name: "angry-client"
    description: "An angry client called demanding
                  an immediate appointment."

  - name: "confused-client"
    description: "A confused elderly client who keeps
                  forgetting what was said earlier
                  in the conversation."

# ===========================
# Tested Agent
# ===========================
target:
  url: "https://my-dental-bot.com/api/chat"
  method: "POST"
  headers:
    Authorization: "Bearer token"
    Content-Type: "application/json"

# ===========================
# Judges (odd number)
# ===========================
judges:
  - model: "llama3.2:3b"
    url: "http://localhost:11434"

  - model: "gpt-4o"
    url: "https://api.openai.com"
    apiKey: "sk-..."

  - model: "gemini-pro"
    url: "https://generativelanguage.googleapis.com"
    apiKey: "AI..."

# ===========================
# Metrics
# ===========================
metrics:
  - accuracy
  - consistency
  - hallucination
  - incident_tracking

# ===========================
# Report
# ===========================
report:
  type: "html"

# ===========================
# Test Cases
# ===========================
tests:
  - name: "indecisive-client-tuesday"
    description: "Verifies agent books correct day based on client availability"
    scenarioRef: "indecisive-client"
    userQuery: "I am not sure about Friday, Tuesday seems okay but Wednesday I am busy."
    expectedOutput: "I will book you for Tuesday."
    runs: 1000
    thresholds:
      accuracy: 0.9
      consistency: 0.85
      hallucination: 0.95

  - name: "indecisive-client-friday"
    description: "Verifies agent handles client changing their mind about Friday"
    scenarioRef: "indecisive-client"
    userQuery: "Actually Friday might work, I am just not sure."
    expectedOutput: "Would Tuesday still work for you?"
    runs: 1000
    thresholds:
      accuracy: 0.9
      consistency: 0.85
      hallucination: 0.95

  - name: "angry-client-urgent"
    description: "Verifies agent handles urgent appointment request calmly"
    scenarioRef: "angry-client"
    userQuery: "I need an appointment right now, this is urgent."
    expectedOutput: "I understand, the earliest available slot is Tuesday at 10:00."
    runs: 500
    thresholds:
      accuracy: 0.85
      consistency: 0.80
      hallucination: 0.90

  - name: "confused-client-cancellation"
    description: "Verifies agent handles cancellation request from confused client"
    scenarioRef: "confused-client"
    userQuery: "I think I had an appointment? I want to cancel it, or maybe reschedule?"
    expectedOutput: "I found your appointment on Tuesday. Would you like to cancel or reschedule?"
    runs: 500
    thresholds:
      accuracy: 0.85
      consistency: 0.80
      hallucination: 0.90
```

</details>

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