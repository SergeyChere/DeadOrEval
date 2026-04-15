package ai.never.trust.interfaces;

import ai.never.trust.model.EvalConfig;

public interface ConfigParser {
    EvalConfig parse(String path);
}
