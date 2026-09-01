package dev.yakekusolsu.growthtools.config;

import java.util.Objects;

public record ConfigValidationIssue(Severity severity, String path, String message) {
    public ConfigValidationIssue {
        Objects.requireNonNull(severity, "severity");
        Objects.requireNonNull(path, "path");
        Objects.requireNonNull(message, "message");
    }

    public enum Severity {
        WARNING,
        ERROR
    }
}
