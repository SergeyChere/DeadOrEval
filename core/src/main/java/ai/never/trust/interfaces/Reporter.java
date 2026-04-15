package ai.never.trust.interfaces;

import ai.never.trust.model.EvalReport;

public interface Reporter {
    void report(EvalReport report);
    String getType();
}
