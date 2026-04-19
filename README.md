# DeadOrEval

> Is your chatbot dead or alive? Find out with one command.

DeadOrEval is a local-first LLM evaluation framework for testing chatbots and AI agents.
No API keys. No cloud. No cost. Just point it at your chatbot and get a reliability report.

## Why DeadOrEval?

Most eval tools measure **capability** — can your model pass a benchmark?
DeadOrEval measures **reliability** — can you trust it in production on week 12?

---

## Quick Start

```bash
# Install Ollama — https://ollama.com
ollama pull llama3.2:3b

# Clone and build
git clone https://github.com/SergeyChere/DeadOrEval.git
cd DeadOrEval
mvn clean install

# Run
java -jar cli/target/doe.jar --config app.yaml
```

## Configuration

```yaml
testedAgent:
  url: "https://my-chatbot.com/api/chat"
  method: "POST"
judges:
  - model: "llama3.2:3b"
    url: "http://localhost:11434"
scenarios:
  - name: "basic-scenario"
    description: "A client is asking a simple question."
tests:
  - name: "basic-test"
    scenarioRef: "basic-scenario"
    userQuery: "What are your opening hours?"
    expectedOutput: "We are open Monday to Friday, 9am to 5pm."
    runs: 100
    thresholds:
      accuracy: 0.9
      consistency: 0.85
```

For full configuration details see [config/README.md](config/README.md).
For CLI usage see [cli/README.md](cli/README.md).

## Minimum Requirements

DeadOrEval is designed to run on any machine with:
- Java 21+
- 8GB RAM
- [Ollama](https://ollama.com) with `llama3.2:3b`

This covers a single local judge and basic test cases.
For extended testing see [EXTENDED.md](EXTENDED.md).

## Roadmap

### MVP
- [x] Multi-module Maven architecture
- [ ] YAML config support
- [ ] Ollama judge implementation
- [ ] Accuracy, Consistency, Hallucination, Incident metrics
- [ ] Console reporter
- [ ] HTML report generation
- [ ] Single judge evaluation flow
- [ ] Warmup — model heatup before calibration
- [ ] Warmup — judge calibration with known questions
- [ ] Warmup — peer review between judges
- [ ] Warmup — judge weight calculation
- [ ] Warmup — skip if single judge
- [ ] Multiple judges consensus

### Post MVP
- [ ] EvalSuite — batch overnight testing
- [ ] Warmup — custom warmup questions via config
- [ ] Warmup — configurable heatup runs and timeouts
- [ ] GraalVM native binary
- [ ] Distributed judges on remote machines
- [ ] CI/CD integration
- [ ] PDF report generation
- [ ] Prometheus metrics export
- [ ] Schedule testing

## Contributing

Please read [BRANCHING.md](BRANCHING.md) for contribution rules.

## License

MIT © SergeyChere