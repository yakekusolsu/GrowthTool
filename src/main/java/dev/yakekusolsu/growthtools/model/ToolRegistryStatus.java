package dev.yakekusolsu.growthtools.model;

/** Audit status for a portable PDC-backed GrowthTool. */
public enum ToolRegistryStatus {
    ACTIVE,
    DUPLICATE,
    DESTROYED,
    REPLACED,
    UNKNOWN;

    public boolean canTransitionTo(ToolRegistryStatus next) {
        if (this == next) {
            return true;
        }
        return switch (this) {
            case UNKNOWN -> true;
            case ACTIVE, DUPLICATE -> next != UNKNOWN;
            case DESTROYED -> next == ACTIVE || next == DUPLICATE || next == REPLACED;
            case REPLACED -> next == DUPLICATE;
        };
    }
}
