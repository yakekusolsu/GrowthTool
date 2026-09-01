package dev.yakekusolsu.growthtools.model;

import java.util.Objects;
import java.util.UUID;

/** A validated request to add experience to one specific GrowthTool. */
public record ExperienceGain(
        UUID toolId,
        GrowthToolType toolType,
        ExperienceSource source,
        long amount) {

    public ExperienceGain {
        Objects.requireNonNull(toolId, "toolId");
        Objects.requireNonNull(toolType, "toolType");
        Objects.requireNonNull(source, "source");
        if (amount <= 0) {
            throw new IllegalArgumentException("amount must be positive");
        }
    }
}
