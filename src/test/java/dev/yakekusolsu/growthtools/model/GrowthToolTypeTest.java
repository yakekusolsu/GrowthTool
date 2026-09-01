package dev.yakekusolsu.growthtools.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import org.junit.jupiter.api.Test;

class GrowthToolTypeTest {
    @Test
    void containsEveryPhaseTwoType() {
        assertEquals(
                Arrays.asList("pickaxe", "axe", "shovel", "hoe", "fishing_rod", "bow"),
                Arrays.stream(GrowthToolType.values()).map(GrowthToolType::id).toList());
    }

    @Test
    void parsesPersistentAndCommandRepresentations() {
        assertEquals(GrowthToolType.PICKAXE, GrowthToolType.parse("PICKAXE").orElseThrow());
        assertEquals(GrowthToolType.FISHING_ROD,
                GrowthToolType.parse("fishing_rod").orElseThrow());
        assertTrue(GrowthToolType.parse("sword").isEmpty());
        assertTrue(GrowthToolType.parse(null).isEmpty());
    }
}
