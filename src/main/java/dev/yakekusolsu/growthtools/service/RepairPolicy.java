package dev.yakekusolsu.growthtools.service;

/** Classifies which item problems are safe for an explicit administrator repair. */
public final class RepairPolicy {
    public boolean isRepairable(Problem problem) {
        return switch (problem) {
            case LEVEL_CACHE_MISMATCH, LORE_MISMATCH, REGISTRY_MISSING -> true;
            case UNKNOWN_DATA_VERSION, INVALID_UUID, NEGATIVE_EXPERIENCE,
                    MATERIAL_TYPE_MISMATCH, INCOMPLETE_PDC -> false;
        };
    }

    public enum Problem {
        LEVEL_CACHE_MISMATCH,
        LORE_MISMATCH,
        REGISTRY_MISSING,
        UNKNOWN_DATA_VERSION,
        INVALID_UUID,
        NEGATIVE_EXPERIENCE,
        MATERIAL_TYPE_MISMATCH,
        INCOMPLETE_PDC
    }
}
