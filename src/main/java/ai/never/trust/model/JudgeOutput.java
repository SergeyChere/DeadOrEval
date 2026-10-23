package ai.never.trust.model;

import java.util.List;

/**
 * Output from a single judge evaluation.
 * Score is normalized to [0.0, 1.0].
 */
public class JudgeOutput {
    private String judgeName;
    private double score;
    private String reasoning;
    private List<String> tags;
    private double confidence = -1.0; // -1 means not provided (Ollama); Jev always provides it

    public JudgeOutput() {}

    public JudgeOutput(String judgeName, double score, String reasoning) {
        this.judgeName = judgeName;
        this.score = score;
        this.reasoning = reasoning;
    }

    public String getJudgeName() { return judgeName; }
    public void setJudgeName(String judgeName) { this.judgeName = judgeName; }

    public double getScore() { return score; }
    public void setScore(double score) { this.score = score; }

    public String getReasoning() { return reasoning; }
    public void setReasoning(String reasoning) { this.reasoning = reasoning; }

    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags; }

    public double getConfidence() { return confidence; }
    public void setConfidence(double confidence) { this.confidence = confidence; }
    public boolean hasConfidence() { return confidence >= 0.0; }

    @Override
    public String toString() {
        return "JudgeOutput{judge='" + judgeName + "', score=" + score + "}";
    }
}
