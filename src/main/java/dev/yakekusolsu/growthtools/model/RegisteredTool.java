package dev.yakekusolsu.growthtools.model;

import java.util.Objects;
import java.util.UUID;

/** Registry/audit projection; PDC remains the actual portable item state. */
public record RegisteredTool(
        UUID toolId,
        GrowthToolType toolType,
        int lastKnownLevel,
        long lastKnownExperience,
        int dataVersion,
        long firstSeenAt,
        long lastSeenAt,
        UUID lastOwnerUuid,
        ToolRegistryStatus status) {

    public RegisteredTool {
        Objects.requireNonNull(toolId, "toolId");
        Objects.requireNonNull(toolType, "toolType");
        Objects.requireNonNull(status, "status");
        if (lastKnownLevel < 1 || lastKnownExperience < 0 || dataVersion < 1
                || firstSeenAt < 0 || lastSeenAt < firstSeenAt) {
            throw new IllegalArgumentException("Invalid registered tool values");
        }
    }
}
