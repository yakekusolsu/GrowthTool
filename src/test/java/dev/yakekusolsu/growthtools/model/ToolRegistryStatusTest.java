package dev.yakekusolsu.growthtools.model;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class ToolRegistryStatusTest {
    @Test
    void allowsOperationalStatusTransitions() {
        assertTrue(ToolRegistryStatus.UNKNOWN.canTransitionTo(ToolRegistryStatus.ACTIVE));
        assertTrue(ToolRegistryStatus.ACTIVE.canTransitionTo(ToolRegistryStatus.DUPLICATE));
        assertTrue(ToolRegistryStatus.DUPLICATE.canTransitionTo(ToolRegistryStatus.ACTIVE));
        assertTrue(ToolRegistryStatus.ACTIVE.canTransitionTo(ToolRegistryStatus.DESTROYED));
        assertTrue(ToolRegistryStatus.DESTROYED.canTransitionTo(ToolRegistryStatus.ACTIVE));
        assertTrue(ToolRegistryStatus.ACTIVE.canTransitionTo(ToolRegistryStatus.REPLACED));
    }

    @Test
    void replacedIdsOnlyTransitionToDuplicateWhenObservedAgain() {
        assertFalse(ToolRegistryStatus.REPLACED.canTransitionTo(ToolRegistryStatus.ACTIVE));
        assertTrue(ToolRegistryStatus.REPLACED.canTransitionTo(ToolRegistryStatus.DUPLICATE));
    }
}
