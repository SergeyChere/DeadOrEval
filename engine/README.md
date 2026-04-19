# Engine Module

The `engine` module is the orchestrator of DeadOrEval.
It coordinates judges, metrics, warmup and reporters to produce a final evaluation report.

---

## Responsibilities

- Orchestrates the full evaluation flow
- Manages judge warmup and calibration
- Executes test cases against the tested agent
- Sends agent responses to judges for evaluation
- Collects and aggregates evaluation results
- Calculates metrics from results
- Produces the final EvalReport
- Passes report to Reporter

---

## Full Evaluation Flow

```
1. Load EvalConfig
      │
      ▼
2. Warmup Judges
   └── if judges < 2 → skip, weight = 1.0
   └── if judges >= 2 → calibrate and calculate weights
      │
      ▼
3. For each TestCase:
   │
   ├── 3a. Resolve Scenario
   │       └── Find Scenario by scenarioRef
   │
   ├── 3b. Execute
   │       └── Send userQuery to TestedAgent × runs
   │       └── Collect List<AgentOutput>
   │
   ├── 3c. Evaluate
   │       └── For each AgentOutput:
   │           └── Send to each Judge
   │           └── Collect List<EvalResult>
   │
   ├── 3d. Consensus
   │       └── Apply judge weights from Warmup
   │       └── Calculate weighted consensus score
   │
   └── 3e. Aggregate
           └── Pass List<EvalResult> to each Metric
           └── Collect List<MetricScore>
      │
      ▼
4. Assemble EvalReport
   └── All TestCase results
   └── All MetricScores
   └── All Incidents
   └── ConsensusReport
      │
      ▼
5. Pass to Reporter
   └── ConsoleReporter
   └── HtmlReporter
```

---

## Warmup Flow

```
1. Check judge count
   └── if judges < 2 → skip warmup, weight = 1.0 for all

2. Heatup
   └── For each Judge:
       └── Send heatupRuns empty requests
       └── Results ignored — model loaded into memory

3. Calibration
   └── For each Judge:
       └── For each WarmupQuestion:
           └── Send question to Judge
           └── Compare answer to expectedAnswer
           └── Record score
       └── warmupAccuracy = average of all scores

4. Peer Review
   └── For each Judge A:
       └── For each other Judge B:
           └── Judge A evaluates Judge B answers
           └── Record peerScore

5. Weight Calculation
   └── finalWeight = (warmupAccuracy × 0.6) + (peerScore × 0.4)

6. Result
   └── Map<Judge, Double> weights
   └── Passed to Execution Flow for consensus scoring
```

---

## Execution Flow

```
For each TestCase:

1. Resolve Scenario
   └── Find Scenario by scenarioRef from config

2. Execute
   └── Send userQuery to TestedAgent × runs
   └── Collect List<AgentOutput>

3. Evaluate
   └── For each AgentOutput:
       └── Send to each Judge with Scenario as context
       └── Collect List<EvalResult> per Judge

4. Consensus
   └── For each EvalResult:
       └── score × judgeWeight
   └── finalScore = weighted average across all judges

5. Incident Detection
   └── if finalScore < threshold → mark as Incident
   └── Record in IncidentTracker

6. Aggregate
   └── Pass List<EvalResult> to each Metric
   └── Collect List<MetricScore>
```

---

## Consensus Scoring

```
Judge A (weight 0.9): score 0.8 → 0.8 × 0.9 = 0.72
Judge B (weight 0.7): score 0.6 → 0.6 × 0.7 = 0.42
Judge C (weight 0.5): score 0.4 → 0.4 × 0.5 = 0.20

Sum of weights: 0.9 + 0.7 + 0.5 = 2.1
Sum of scores:  0.72 + 0.42 + 0.20 = 1.34

ConsensusScore = 1.34 / 2.1 = 0.638
```

---

## Module Structure

```
engine/
└── src/main/java/ai/never/trust/engine/
    ├── EvalEngineImpl.java         — main orchestrator
    └── warmup/
        ├── WarmupExecutor.java     — coordinates warmup flow
        ├── HeatupRunner.java       — model heatup requests
        ├── CalibrationRunner.java  — judge accuracy calibration
        ├── PeerReviewRunner.java   — peer review between judges
        └── WeightCalculator.java   — calculates final judge weights
```

---

## Interfaces

| Interface        | Responsibility                             |
|------------------|--------------------------------------------|
| `EvalEngine`     | Orchestrates the full evaluation run       |
| `WarmupStrategy` | Calibrates judges before evaluation begins |

---

## Roadmap

- [ ] EvalEngineImpl — basic evaluation flow
- [ ] Execution — send userQuery to TestedAgent
- [ ] Execution — collect AgentOutput
- [ ] Evaluation — send AgentOutput to judges
- [ ] Consensus — weighted average scoring
- [ ] Incident Detection — flag results below threshold
- [ ] Warmup — model heatup runs
- [ ] Warmup — judge calibration with known questions
- [ ] Warmup — peer review between judges
- [ ] Warmup — judge weight calculation
- [ ] Warmup — configurable heatup runs count
- [ ] Warmup — configurable timeout per request
- [ ] Warmup — skip if single judge

---

## Dependencies

```
engine → core
engine → judge
engine → metrics
engine → reporter
engine → config
```

For domain models and interfaces see [core/README.md](../core/README.md).
For configuration details see [config/README.md](../config/README.md).