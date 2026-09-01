package dev.yakekusolsu.growthtools.model;

import java.util.Objects;

/** Immutable outcome of one centralized experience operation. */
public record ExperienceResult(
        ExperienceGain gain,
        GrowthToolData oldData,
        GrowthToolData newData,
        long experienceAdded,
        boolean reachedMaximumLevel) {

    public ExperienceResult {
        Objects.requireNonNull(gain, "gain");
        Objects.requireNonNull(oldData, "oldData");
        Objects.requireNonNull(newData, "newData");
        if (!oldData.toolId().equals(newData.toolId())
                || !oldData.toolId().equals(gain.toolId())) {
            throw new IllegalArgumentException("toolId must remain unchanged");
        }
        if (oldData.type() != newData.type() || oldData.type() != gain.toolType()) {
            throw new IllegalArgumentException("toolType must remain unchanged");
        }
        if (experienceAdded < 0
                || newData.experience() - oldData.experience() != experienceAdded) {
            throw new IllegalArgumentException("experienceAdded does not match the data change");
        }
        if (newData.level() < oldData.level()) {
            throw new IllegalArgumentException("level must not decrease during an experience gain");
        }
    }

    public int oldLevel() {
        return oldData.level();
    }

    public int newLevel() {
        return newData.level();
    }

    public int levelsGained() {
        return newLevel() - oldLevel();
    }

    public boolean leveledUp() {
        return levelsGained() > 0;
    }
}
