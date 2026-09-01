package dev.yakekusolsu.growthtools.model;

import java.util.Objects;
import java.util.UUID;

/** Immutable, Paper-independent state stored on an individual GrowthTool. */
public record GrowthToolData(
        UUID toolId,
        GrowthToolType type,
        int level,
        long experience,
        long createdAt,
        int dataVersion) {

    public GrowthToolData {
        Objects.requireNonNull(toolId, "toolId");
        Objects.requireNonNull(type, "type");
        if (level < 1) {
            throw new IllegalArgumentException("level must be at least 1");
        }
        if (experience < 0) {
            throw new IllegalArgumentException("experience must not be negative");
        }
        if (createdAt < 0) {
            throw new IllegalArgumentException("createdAt must not be negative");
        }
        if (dataVersion < 1) {
            throw new IllegalArgumentException("dataVersion must be at least 1");
        }
    }

    public GrowthToolData withExperience(long newExperience, int newLevel) {
        return new GrowthToolData(
                toolId, type, newLevel, newExperience, createdAt, dataVersion);
    }
}
