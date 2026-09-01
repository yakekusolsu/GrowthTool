package dev.yakekusolsu.growthtools.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class LevelingServiceTest {
    private final LevelingService service = new LevelingService(100, 500);

    @Test
    void calculatesRequiredExperienceForNextLevel() {
        assertEquals(100, service.getRequiredExperienceForLevel(1));
        assertEquals(200, service.getRequiredExperienceForLevel(2));
        assertEquals(0, service.getRequiredExperienceForLevel(500));
    }

    @Test
    void calculatesZeroAndMultipleLevelUpsFromTotalExperience() {
        assertEquals(1, service.calculateLevel(0));
        assertEquals(1, service.calculateLevel(99));
        assertEquals(2, service.calculateLevel(100));
        assertEquals(2, service.calculateLevel(299));
        assertEquals(3, service.calculateLevel(300));
        assertEquals(4, service.calculateLevel(600));
    }

    @Test
    void neverExceedsMaximumLevel() {
        assertEquals(500, service.calculateLevel(Long.MAX_VALUE));
    }

    @Test
    void rejectsNegativeExperienceAndOutOfRangeLevels() {
        assertThrows(IllegalArgumentException.class, () -> service.calculateLevel(-1));
        assertThrows(IllegalArgumentException.class,
                () -> service.getRequiredExperienceForLevel(0));
        assertThrows(IllegalArgumentException.class,
                () -> service.getRequiredExperienceForLevel(501));
    }

    @Test
    void saturatesResultsWithoutOverflowAndDoesNotGrantUnreachableLevels() {
        LevelingService extreme = new LevelingService(Long.MAX_VALUE, 3);

        assertEquals(Long.MAX_VALUE, extreme.getRequiredExperienceForLevel(2));
        assertEquals(Long.MAX_VALUE, extreme.getTotalExperienceForLevel(3));
        assertEquals(2, extreme.calculateLevel(Long.MAX_VALUE));
    }

    @Test
    void validatesConfiguration() {
        assertThrows(IllegalArgumentException.class, () -> new LevelingService(0, 10));
        assertThrows(IllegalArgumentException.class, () -> new LevelingService(100, 0));
    }
}
