package dev.yakekusolsu.growthtools.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class RepairPolicyTest {
    private final RepairPolicy policy = new RepairPolicy();

    @Test
    void permitsOnlySafeRepairs() {
        assertTrue(policy.isRepairable(RepairPolicy.Problem.LEVEL_CACHE_MISMATCH));
        assertTrue(policy.isRepairable(RepairPolicy.Problem.LORE_MISMATCH));
        assertTrue(policy.isRepairable(RepairPolicy.Problem.REGISTRY_MISSING));
        assertFalse(policy.isRepairable(RepairPolicy.Problem.UNKNOWN_DATA_VERSION));
        assertFalse(policy.isRepairable(RepairPolicy.Problem.INVALID_UUID));
        assertFalse(policy.isRepairable(RepairPolicy.Problem.NEGATIVE_EXPERIENCE));
        assertFalse(policy.isRepairable(RepairPolicy.Problem.MATERIAL_TYPE_MISMATCH));
    }
}
