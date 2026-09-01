package dev.yakekusolsu.growthtools.ability;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

class AbilitySafetyLimitsTest {
    @Test void clampsBlockCountsRadiusAndMultiplier() {
        assertEquals(1, AbilitySafetyLimits.veinBlocks(-20));
        assertEquals(128, AbilitySafetyLimits.veinBlocks(500));
        assertEquals(3, AbilitySafetyLimits.areaRadius(50));
        assertEquals(1.25, AbilitySafetyLimits.experienceMultiplier(Double.NaN, 1.25));
        assertEquals(100, AbilitySafetyLimits.experienceMultiplier(500, 1.0));
    }
}
