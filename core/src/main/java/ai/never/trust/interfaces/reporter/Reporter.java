package ai.never.trust.interfaces.reporter;

import ai.never.trust.model.result.EvalReport;

public interface Reporter {
    void report(EvalReport report);
    String getType();
}
