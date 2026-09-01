package dev.yakekusolsu.growthtools.api.ability;

import java.util.Objects;

public record AbilityResult(Status status, int affectedBlocks, long extraExperience, String detail) {
    public AbilityResult {
        Objects.requireNonNull(status, "status");
        Objects.requireNonNull(detail, "detail");
        if (affectedBlocks < 0 || extraExperience < 0) {
            throw new IllegalArgumentException("Result counters cannot be negative");
        }
    }

    public static AbilityResult success(int blocks, long experience) {
        return new AbilityResult(Status.SUCCESS, blocks, experience, "");
    }

    public static AbilityResult skipped(Status status, String detail) {
        return new AbilityResult(status, 0, 0, detail);
    }

    public enum Status {
        SUCCESS,
        NOT_TRIGGERED,
        DISABLED,
        LOCKED,
        INCOMPATIBLE,
        ON_COOLDOWN,
        COOLDOWN,
        CONDITION_FAILED,
        CANCELLED,
        NO_EFFECT,
        NO_VALID_TARGET,
        FAILED,
        ERROR
    }
}
