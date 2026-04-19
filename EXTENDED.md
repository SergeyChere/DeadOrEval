# Extended Testing

For serious production evaluation, a single local machine is not enough.
This guide covers how to scale DeadOrEval for reliable results.

---

## Why Extended Testing?

A single local judge on a laptop has limitations:
- Limited RAM — only small models fit
- Single judge — no consensus, no calibration
- Slow — 1000 runs takes a long time locally
- Unreliable — laptop throttles under load

---

## Option 1 — Cloud VM (Recommended)

Run DeadOrEval on a dedicated cloud machine with more RAM and CPU.

//TODO

---

## Option 2 — Multiple Cloud Judges

Instead of one local judge, use multiple cloud models for better consensus.

```yaml
judges:
  - model: "llama3.2:3b"
    url: "http://localhost:11434"      # local judge

  - model: "gpt-4o"
    url: "https://api.openai.com"      # cloud judge
    apiKey: "sk-..."

  - model: "claude-3-5-sonnet"
    url: "https://api.anthropic.com"   # cloud judge
    apiKey: "sk-ant-..."
```

**Why this works better:**
- Different models have different biases
- Consensus of 3 judges is more reliable than 1
- Cloud models are more powerful than local 3b models

---

## Option 3 — Distributed Judges

Run each judge on a separate machine and connect via URL.

```yaml
judges:
  - model: "llama3.3:70b"
    url: "http://judge-1.myserver.com:11434"

  - model: "llama3.3:70b"
    url: "http://judge-2.myserver.com:11434"

  - model: "llama3.3:70b"
    url: "http://judge-3.myserver.com:11434"
```

Each machine runs Ollama independently.
DeadOrEval connects to all three and runs consensus.

---

## Recommended Configurations

| Use Case | Judges | RAM | Runs | Cost |
|----------|--------|-----|------|------|
| Local dev | 1 × llama3.2:3b | 8GB | 100 | Free |
| Basic eval | 3 × llama3.2:3b | 16GB | 1000 | Free |
| Production | 3 × llama3.3:70b | 64GB | 1000 | ~$50/month |
| Enterprise | 3 × GPT-4o + Claude | Cloud | 10000 | API costs |

---

## Overnight Testing with EvalSuite

For large test suites, run overnight on a cloud VM:

//TODO

---

## Cost Estimation

**OpenAI GPT-4o:**
- ~$0.005 per 1000 tokens
- 1000 runs × average 200 tokens = ~$1 per TestCase

**Anthropic Claude:**
- ~$0.003 per 1000 tokens
- 1000 runs × average 200 tokens = ~$0.60 per TestCase

**Local Ollama:**
- Free — only electricity cost