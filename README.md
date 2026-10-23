# DeadOrEval

> Is your chatbot dead or alive? Find out with one command.

DeadOrEval is a **self-hosted LLM evaluation framework** for testing chatbots and AI agents.  
No SaaS. No vendor lock-in. Point it at your chatbot and get a reliability report.

---

## Why DeadOrEval?

Most eval tools measure **capability** — can your model pass a benchmark?  
DeadOrEval measures **reliability** — can you trust it in production on week 12?

| | DeadOrEval | DeepEval | Ragas | Langsmith |
|---|---|---|---|---|
| Jev judge (typed, no hallucination) | ✓ | ✗ | ✗ | ✗ |
| Self-hosted judge (free) | ✓ | ✗ | ✗ | ✗ |
| No code required | ✓ | ✗ | ✗ | ✗ |
| Multi-run consistency | ✓ | partial | ✗ | partial |
| Self-contained JAR | ✓ | ✗ | ✗ | ✗ |
| Open source | ✓ | ✓ | ✓ | ✗ |

---

## Quick Start

### Option A — Jev (recommended)

Jev is a System One model: one forward pass, typed output, no hallucinations, no text parsing.
Self-host with vLLM or use the TypeSafe cloud API.

```bash
# 1. Self-host openjev with vLLM (needs ~24 GB VRAM, or use the GGUF build for less)
#    See: https://huggingface.co/openjev/openjev
#    Or use TypeSafe cloud: https://console.typesafe.ai

# 2. Clone and build
git clone https://github.com/SergeyChere/DeadOrEval.git
cd DeadOrEval
mvn clean package

# 3. Create config
cat > app.yaml << 'EOF'
name: my-bot-eval
testedAgent:
  url: http://localhost:8080/api/chat
  method: POST
judges:
  - model: openjev          # or: jev-latest (TypeSafe cloud), jev-9b (lighter model)
    url: http://localhost:8080   # your vLLM server
    # apiKey: ts-...         # only needed for TypeSafe cloud
metrics:
  - accuracy
  - consistency
  - hallucination
report:
  type: html
scenarios:
  - name: basic
    description: "A user is asking about opening hours."
tests:
  - name: opening-hours
    scenarioRef: basic
    userQuery: "What are your opening hours?"
    expectedOutput: "We are open Monday to Friday, 9am to 5pm."
    runs: 100
    thresholds:
      accuracy: 0.8
      consistency: 0.85
EOF

# 4. Run
java -jar target/doe.jar --config app.yaml
```

### Option B — Ollama (slow, not recommended for large runs)

```bash
ollama pull llama3.2:3b
```

```yaml
judges:
  - model: llama3.2:3b
    url: http://localhost:11434
```

> ⚠️ Ollama is autoregressive — 1000 runs can take hours. Use Jev for any serious evaluation.

---

## How it works

```
1. doe --config app.yaml
        │
2. Parse YAML → EvalConfig
        │
3. For each TestCase:
   ├── Resolve Scenario (context for judge)
   ├── Call agent N times → N responses
   └── For each response → Judge evaluates → score [0.0–1.0]
        │
4. Aggregate: consensus score, metrics, incidents
        │
5. Report: Console or HTML
```

**Key design decisions:**
- **Jev-first**: One forward pass, typed output — no text parsing, no hallucinations, fast enough for 1000+ runs
- **Multi-run**: Run the same query 100–1000 times to measure consistency
- **Consensus**: Multiple judges → weighted average score
- **Incident tracking**: Any run below your threshold is logged as an incident

---

## Configuration Reference

### Full example

```yaml
name: dental-bot-eval-v1

testedAgent:
  url: https://my-dental-bot.com/api/chat
  method: POST
  headers:
    Authorization: Bearer token
    Content-Type: application/json

judges:
  # Jev (recommended) — self-hosted via vLLM:
  - model: openjev
    url: http://localhost:8080
  # Jev via TypeSafe cloud:
  # - model: jev-latest
  #   url: https://api.typesafe.ai
  #   apiKey: ts-...
  # Ollama (slow, for quick local tests only):
  # - model: llama3.2:3b
  #   url: http://localhost:11434

metrics:
  - accuracy
  - consistency
  - hallucination
  - incident_tracking

report:
  type: html        # or: console

scenarios:
  - name: indecisive-client
    description: "A difficult client who cannot decide which day to schedule."
  - name: angry-client
    description: "An angry client demanding an immediate appointment."

tests:
  - name: indecisive-client-tuesday
    scenarioRef: indecisive-client
    userQuery: "I am not sure about Friday, Tuesday seems okay but Wednesday I am busy."
    expectedOutput: "I will book you for Tuesday."
    runs: 1000
    thresholds:
      accuracy: 0.9
      consistency: 0.85
      hallucination: 0.95

  - name: angry-client-now
    scenarioRef: angry-client
    userQuery: "I need an appointment right now, this is urgent."
    expectedOutput: "I understand, the earliest available slot is Tuesday at 10:00."
    runs: 500
    thresholds:
      accuracy: 0.85
```

### Fields

| Field | Required | Description |
|---|---|---|
| `name` | yes | Identifier for this evaluation run |
| `testedAgent.url` | yes | HTTP endpoint of the chatbot |
| `testedAgent.method` | no | HTTP method, default `POST` |
| `testedAgent.headers` | no | Custom HTTP headers (auth tokens, etc.) |
| `judges` | yes | At least 1 judge |
| `judges[].model` | yes | Model name (e.g. `llama3.2:3b`) |
| `judges[].url` | yes | Judge service URL |
| `judges[].apiKey` | no | API key for cloud judges |
| `metrics` | no | Which metrics to calculate |
| `report.type` | no | `console` (default) or `html` |
| `scenarios` | no | Reusable context definitions |
| `tests` | yes | List of test cases |
| `tests[].runs` | no | How many times to call the agent, default `1` |
| `tests[].thresholds` | no | Per-metric pass/fail thresholds |

---

## Metrics

| Metric | Description |
|---|---|
| `accuracy` | Fraction of runs where agent scored ≥ 0.5 |
| `consistency` | How stable scores are across runs (1.0 = perfectly stable) |
| `hallucination` | Fraction of runs without hallucination indicators (score < 0.3) |
| `incident_tracking` | Fraction of runs that did not fall below threshold |

All metrics return a score in `[0.0, 1.0]` where **1.0 is best**.

---

## Output

### Console
```
╔══════════════════════════════════════════╗
║          DeadOrEval — Starting           ║
╚══════════════════════════════════════════╝
  Eval:    dental-bot-eval-v1
  Agent:   https://my-dental-bot.com/api/chat
  Judges:  1
  Tests:   2

[1/2] Running: indecisive-client-tuesday (1000 runs)
    ✓ PASS  avg=0.876  min=0.612  max=1.000  variance=0.0142  incidents=47
[2/2] Running: angry-client-now (500 runs)
    ✓ PASS  avg=0.921  min=0.754  max=1.000  variance=0.0089  incidents=0

  METRICS:
    accuracy             [████████████████░░░░] 0.8830
    consistency          [██████████████████░░] 0.9124
    hallucination        [████████████████████] 0.9980

  ✓ ALL PASSED
```

### HTML
A self-contained `eval-report.html` with dark theme, progress bars, and per-test-case breakdown.

---

## Agent API Format

DeadOrEval sends a standard **OpenAI-compatible chat format**:

```json
{
  "messages": [
    {"role": "system", "content": "scenario description"},
    {"role": "user", "content": "user query"}
  ]
}
```

Your agent must return any of these formats:
```json
{"choices": [{"message": {"content": "response text"}}]}   // OpenAI format
{"response": "response text"}                               // Ollama format  
{"text": "response text"}                                   // Simple format
{"content": "response text"}                                // Generic format
"plain text response"                                       // Plain text
```

---

## CLI Reference

```bash
java -jar doe.jar --config app.yaml    # Run evaluation
java -jar doe.jar --version            # Print version
java -jar doe.jar --help               # Show help
java -jar doe.jar --config app.yaml --verbose   # Full stack traces on error
```

---

## Architecture

Single-module monolith. Easy to understand, easy to fork.

```
src/main/java/ai/never/trust/
├── cli/
│   └── Doe.java                  ← Entry point, arg parsing
├── config/
│   └── YamlConfigParser.java     ← YAML → EvalConfig
├── model/
│   ├── EvalConfig.java           ← Root config object
│   ├── TestCase.java             ← Single test definition
│   ├── Scenario.java             ← Reusable context
│   ├── JudgeConfig.java          ← Judge connection config
│   ├── TestedAgent.java          ← Agent connection config
│   ├── ReportConfig.java         ← Report type config
│   ├── JudgeOutput.java          ← Single judge verdict
│   ├── TestResult.java           ← Single run result
│   ├── TestCaseReport.java       ← Aggregated test case result
│   └── EvalReport.java           ← Final full report
├── judge/
│   ├── Judge.java                ← Interface
│   ├── OllamaJudge.java          ← Ollama implementation
│   ├── JudgeFactory.java         ← Creates judges from config
│   └── JudgeNormalizer.java      ← Normalizes scores + consensus
├── metric/
│   ├── Metric.java               ← Interface
│   ├── AccuracyMetric.java       ← Correctness
│   ├── ConsistencyMetric.java    ← Stability
│   ├── HallucinationMetric.java  ← Fabrication detection
│   ├── IncidentMetric.java       ← Threshold tracking
│   └── MetricRegistry.java       ← Creates metrics from config
├── engine/
│   ├── EvalEngine.java           ← Orchestrates full run
│   └── AgentCaller.java          ← HTTP calls to tested agent
└── reporter/
    ├── Reporter.java             ← Interface
    ├── ConsoleReporter.java      ← Terminal output
    ├── HtmlReporter.java         ← HTML file output
    └── ReporterFactory.java      ← Creates reporters from config
```

---

## Building from source

**Requirements:** Java 21, Maven 3.8+

```bash
# Build fat JAR
mvn clean package

# The JAR is at:
target/doe.jar

# Run tests
mvn test
```

---

## Roadmap

### v0.1 (current)
- [x] YAML config parser
- [x] Jev judge (openjev, JEV-9B, TypeSafe API) — typed output, calibrated confidence
- [x] Ollama judge (legacy, slow — for quick local tests)
- [x] 4 metrics: accuracy, consistency, hallucination, incident_tracking
- [x] Console + HTML reporter
- [x] Multi-judge consensus with normalization
- [x] Agent HTTP caller (OpenAI-compatible format)
- [x] Incident tracking

### v0.2
- [ ] OpenAI judge
- [ ] Gemini judge
- [ ] Claude judge
- [ ] Warmup calibration for judge weight calculation
- [ ] JSON reporter

### v0.3
- [ ] EvalSuite (batch multiple configs overnight)
- [ ] CLI overrides (`--runs`, `--judges`, `--threshold`)
- [ ] Prometheus metrics export
- [ ] GraalVM native binary (`doe` instead of `java -jar`)

### Pro (cloud)
- [ ] Cloud GPU judges (faster than local Ollama)
- [ ] Official AI Security Audit PDF report
- [ ] Team dashboard

---

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md).

Branch strategy: `dev` → `review` → `main`.  
`main` is protected. All merges via squash PR.

---

## License

MIT © [SergeyChere](https://github.com/SergeyChere)
