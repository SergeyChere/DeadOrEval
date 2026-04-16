package ai.never.trust.interfaces.config;

import ai.never.trust.model.config.EvalConfig;

public interface ConfigParser {
    EvalConfig parse(String path);
}
