# 🔫 DeadOrEval

> Is your chatbot **dead or alive**? Find out with one command.

DeadOrEval is a local-first LLM evaluation framework for testing chatbots and AI agents.
No API keys. No cloud. No cost. Just point it at your chatbot and get a reliability report.

## Why DeadOrEval?

Most eval tools measure **capability** — can your model pass a benchmark?
DeadOrEval measures **reliability** — can you trust it in production on week 12?

## How it works

1. You provide a **context**, a **question**, and a **chatbot answer**
2. DeadOrEval sends it to a local judge model thousands of times
3. You get a report showing consistency, accuracy and failure rate

## Quick Start

```bash
# Install Ollama
# https://ollama.com

# Pull judge model
ollama pull llama3.2:3b

# Clone 
git clone https://github.com/SergeyChere/DeadOrEval.git
cd DeadOrEval

# Build
mvn install

# Run
java -jar cli/target/doe.jar --config app.yaml

## Example Output

=== DeadOrEval Results ===
Total evaluated:  1000
Failed to parse:  0
Average score:    0.873
Min score:        0.6
Max score:        1.0
Perfect (>=0.9):  743
Wrong   (<=0.1):  0

## Supported Judges

| Judge | Type | Cost |
|-------|------|------|
| llama3.2:3b | Local (Ollama) | Free |
| tinyllama | Local (Ollama) | Free |
| GPT-4o | Cloud (OpenAI) | Paid |
| Gemini | Cloud (Google) | Paid |

## CLI Usage

```bash
doe --config app.yaml
doe --config app.yaml --runs 1000
doe --config app.yaml --judges ollama,openai
doe --config app.yaml --metrics accuracy,consistency,hallucination
doe --config app.yaml --report html
doe --config app.yaml --threshold 0.8
doe --config app.yaml --verbose
doe --version
```

## Roadmap

- [ ] YAML config support - in progress
- [ ] Multiple judges consensus - in progress
- [ ] HTML report generation - in progress
- [ ] CLI tool (doe)
- [ ] CI/CD integration - in progress

## License

MIT © SergeyChere