# CLI Module

The `cli` module is the entry point of DeadOrEval.
It parses command line arguments and orchestrates the evaluation run.

---

## Usage

```bash
java -jar doe.jar --config app.yaml
```

---

## Arguments

| Argument   | Description                        | Required |
|------------|------------------------------------|----------|
| `--config` | Path to the EvalConfig YAML file   | ✅ Yes   |

---

## Examples

```bash
# Run evaluation with a config file
java -jar doe.jar --config app.yaml

# Run with a config in a subdirectory
java -jar doe.jar --config configs/dental-bot.yaml
```

---

## Flow

```
doe --config app.yaml
        │
        ▼
    CliArgs
        │
        ▼
  YamlConfigParser
        │
        ▼
    EvalConfig
        │
        ▼
    EvalEngine
        │
        ▼
    EvalReport
        │
        ▼
     Reporter
```

---

## Post MVP Arguments

```bash
doe --config app.yaml --report html
doe --config app.yaml --judges ollama,openai
doe --config app.yaml --metrics accuracy,consistency
doe --config app.yaml --runs 1000
doe --config app.yaml --verbose
doe --config app.yaml --dry-run
```

---

## Dependency Rule

```
cli → engine
cli → config
cli → core
```

For configuration details see [config/README.md](../config/README.md).
For engine details see [engine/README.md](../engine/README.md).