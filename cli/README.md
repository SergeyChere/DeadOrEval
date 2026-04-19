

---

## CLI Usage

```bash
java -jar cli/target/doe.jar --config app.yaml
java -jar cli/target/doe.jar --config app.yaml --runs 1000
java -jar cli/target/doe.jar --config app.yaml --judges ollama,openai,gemini
java -jar cli/target/doe.jar --config app.yaml --metrics accuracy,consistency,hallucination
java -jar cli/target/doe.jar --config app.yaml --report html
java -jar cli/target/doe.jar --config app.yaml --threshold 0.8
java -jar cli/target/doe.jar --config app.yaml --verbose
java -jar cli/target/doe.jar --version
```

---

## Example Output

=== DeadOrEval Results ===
Total evaluated:  1000
Failed to parse:  0
Average score:    0.873
Min score:        0.6
Max score:        1.0
Perfect (>=0.9):  743
Wrong   (<=0.1):  0

---