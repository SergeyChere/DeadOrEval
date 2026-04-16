package ai.never.trust.interfaces.engine;

import ai.never.trust.model.config.EvalConfig;
import ai.never.trust.model.result.EvalReport;

public interface EvalEngine {
    EvalReport run(EvalConfig config);
}
