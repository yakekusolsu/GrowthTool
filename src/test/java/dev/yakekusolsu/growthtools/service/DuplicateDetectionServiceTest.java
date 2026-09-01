package dev.yakekusolsu.growthtools.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class DuplicateDetectionServiceTest {
    private final DuplicateDetectionService service = new DuplicateDetectionService();

    @Test
    void detectsSameInventoryAndCrossOwnerDuplicates() {
        assertFalse(service.isDuplicate(1, false));
        assertTrue(service.isDuplicate(2, false));
        assertTrue(service.isDuplicate(1, true));
    }

    @Test
    void rejectsInvalidObservationCounts() {
        assertThrows(IllegalArgumentException.class,
                () -> service.isDuplicate(-1, false));
    }
}
