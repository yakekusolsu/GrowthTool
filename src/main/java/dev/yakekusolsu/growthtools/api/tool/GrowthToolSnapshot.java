package dev.yakekusolsu.growthtools.api.tool;

import dev.yakekusolsu.growthtools.model.GrowthToolType;
import java.time.Instant;
import java.util.UUID;

/** Immutable view of validated PDC data. */
public record GrowthToolSnapshot(UUID toolId, GrowthToolType type, int level,
        long totalExperience, Instant createdAt, int dataVersion, int maximumLevel) { }
