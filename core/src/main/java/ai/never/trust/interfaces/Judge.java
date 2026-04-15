package ai.never.trust.interfaces;

import ai.never.trust.model.JudgeOutput;
import ai.never.trust.model.TestCase;

public interface Judge {
    JudgeOutput judge(TestCase testCase);
    String getName();
    boolean isAvailable();
}
