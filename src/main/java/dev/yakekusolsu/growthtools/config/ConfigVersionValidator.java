package dev.yakekusolsu.growthtools.config;

/** Paper-independent config version classification used before migration. */
public final class ConfigVersionValidator {
    private final int currentVersion;

    public ConfigVersionValidator(int currentVersion) {
        if (currentVersion < 1) {
            throw new IllegalArgumentException("currentVersion must be positive");
        }
        this.currentVersion = currentVersion;
    }

    public Result validate(int configuredVersion) {
        if (configuredVersion < 0) {
            return Result.INVALID;
        }
        if (configuredVersion == 0) {
            return Result.MISSING;
        }
        if (configuredVersion < currentVersion) {
            return Result.OLDER;
        }
        if (configuredVersion == currentVersion) {
            return Result.CURRENT;
        }
        return Result.NEWER;
    }

    public enum Result {
        MISSING,
        INVALID,
        OLDER,
        CURRENT,
        NEWER
    }
}
