package ai.never.trust.interfaces;

import ai.never.trust.model.EvalConfig;
import ai.never.trust.model.EvalReport;

public interface EvalEngine {
    EvalReport run(EvalConfig config);
}
