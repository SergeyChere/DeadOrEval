package ai.never.trust.reporter;

import ai.never.trust.model.EvalReport;

/**
 * Contract for all report output implementations.
 */
public interface Reporter {
    void report(EvalReport report);
    String getName();
}
