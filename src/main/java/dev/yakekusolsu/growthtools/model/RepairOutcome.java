package dev.yakekusolsu.growthtools.model;

import java.util.Optional;

/** Result of an explicit safe item repair attempt. */
public record RepairOutcome(Status status, Optional<GrowthToolData> data, String detail) {
    public enum Status {
        REPAIRED,
        NOT_A_GROWTH_TOOL,
        UNREPAIRABLE
    }
}
