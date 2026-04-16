package ai.never.trust.interfaces.judge;

import ai.never.trust.model.result.JudgeOutput;
import ai.never.trust.model.test.TestCase;

public interface Judge {
    JudgeOutput judge(TestCase testCase);
    String getName();
    boolean isAvailable();
}
