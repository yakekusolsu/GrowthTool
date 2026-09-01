package dev.yakekusolsu.growthtools.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.yakekusolsu.growthtools.model.ExperienceGain;
import dev.yakekusolsu.growthtools.model.ExperienceResult;
import dev.yakekusolsu.growthtools.model.ExperienceSource;
import dev.yakekusolsu.growthtools.model.GrowthToolData;
import dev.yakekusolsu.growthtools.model.GrowthToolType;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class ExperienceServiceTest {
    private static final UUID TOOL_ID = UUID.fromString("5ae315ac-7a42-4925-aec4-b454b606b84f");
    private static final long MAXIMUM_EXPERIENCE = 1_000L;

    private final ExperienceService service = new ExperienceService(new LevelingService(100, 5));

    @Test
    void addsExperienceWithoutLevelingUp() {
        ExperienceResult result = add(data(1, 10), 20);

        assertEquals(20, result.experienceAdded());
        assertEquals(30, result.newData().experience());
        assertEquals(1, result.newLevel());
        assertFalse(result.leveledUp());
    }

    @Test
    void gainsOneLevel() {
        ExperienceResult result = add(data(1, 90), 20);

        assertEquals(2, result.newLevel());
        assertEquals(1, result.levelsGained());
        assertTrue(result.leveledUp());
        assertFalse(result.reachedMaximumLevel());
    }

    @Test
    void gainsMultipleLevelsInOneOperation() {
        ExperienceResult result = add(data(1, 0), 650);

        assertEquals(4, result.newLevel());
        assertEquals(3, result.levelsGained());
    }

    @Test
    void capsExperienceAtMaximumLevel() {
        ExperienceResult result = add(data(4, 900), 200);

        assertEquals(MAXIMUM_EXPERIENCE, result.newData().experience());
        assertEquals(100, result.experienceAdded());
        assertEquals(5, result.newLevel());
        assertTrue(result.reachedMaximumLevel());
    }

    @Test
    void doesNotIncreaseAnAlreadyMaximumLevelTool() {
        ExperienceResult result = add(data(5, MAXIMUM_EXPERIENCE), 100);

        assertEquals(0, result.experienceAdded());
        assertEquals(MAXIMUM_EXPERIENCE, result.newData().experience());
        assertFalse(result.reachedMaximumLevel());
    }

    @Test
    void rejectsZeroAndNegativeAmounts() {
        assertThrows(IllegalArgumentException.class, () -> gain(0));
        assertThrows(IllegalArgumentException.class, () -> gain(-1));
    }

    @Test
    void preventsLongOverflowByCappingBeforeAddition() {
        ExperienceResult result = add(data(4, 900), Long.MAX_VALUE);

        assertEquals(MAXIMUM_EXPERIENCE, result.newData().experience());
        assertEquals(100, result.experienceAdded());
    }

    @Test
    void rejectsExperienceAlreadyAboveTheMaximumCap() {
        assertThrows(IllegalArgumentException.class,
                () -> add(data(5, MAXIMUM_EXPERIENCE + 1), 1));
    }

    @Test
    void normalizesCachedLevelFromExperienceSourceOfTruth() {
        ExperienceResult result = add(data(4, 100), 1);

        assertEquals(2, result.oldLevel());
        assertEquals(2, result.newLevel());
        assertEquals(101, result.newData().experience());
    }

    @Test
    void rejectsAGainForAnotherTool() {
        GrowthToolData data = data(1, 0);
        ExperienceGain other = new ExperienceGain(
                UUID.randomUUID(), GrowthToolType.PICKAXE, ExperienceSource.BLOCK_BREAK, 1);

        assertThrows(IllegalArgumentException.class,
                () -> service.addExperience(data, other));
    }

    private ExperienceResult add(GrowthToolData data, long amount) {
        return service.addExperience(data, gain(amount));
    }

    private ExperienceGain gain(long amount) {
        return new ExperienceGain(
                TOOL_ID, GrowthToolType.PICKAXE, ExperienceSource.BLOCK_BREAK, amount);
    }

    private GrowthToolData data(int level, long experience) {
        return new GrowthToolData(
                TOOL_ID, GrowthToolType.PICKAXE, level, experience, 1_700_000_000_000L, 1);
    }
}
