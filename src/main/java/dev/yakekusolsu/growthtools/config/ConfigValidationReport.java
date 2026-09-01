package dev.yakekusolsu.growthtools.config;

import java.util.ArrayList;
import java.util.List;

/** Aggregated human-readable configuration validation result. */
public final class ConfigValidationReport {
    private final List<ConfigValidationIssue> issues = new ArrayList<>();

    public void warning(String path, String message) {
        issues.add(new ConfigValidationIssue(
                ConfigValidationIssue.Severity.WARNING, path, message));
    }

    public void error(String path, String message) {
        issues.add(new ConfigValidationIssue(
                ConfigValidationIssue.Severity.ERROR, path, message));
    }

    public List<ConfigValidationIssue> issues() {
        return List.copyOf(issues);
    }

    public long warningCount() {
        return count(ConfigValidationIssue.Severity.WARNING);
    }

    public long errorCount() {
        return count(ConfigValidationIssue.Severity.ERROR);
    }

    public boolean hasErrors() {
        return errorCount() > 0;
    }

    private long count(ConfigValidationIssue.Severity severity) {
        return issues.stream().filter(issue -> issue.severity() == severity).count();
    }
}
