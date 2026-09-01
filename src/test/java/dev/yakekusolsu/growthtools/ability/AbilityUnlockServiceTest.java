package dev.yakekusolsu.growthtools.ability;

import static org.junit.jupiter.api.Assertions.assertEquals;

import dev.yakekusolsu.growthtools.model.GrowthToolType;
import org.junit.jupiter.api.Test;

class AbilityUnlockServiceTest {
    @Test void findsEveryThresholdCrossedByMultiLevelGain() {
        AbilityRegistry registry = new AbilityRegistry();
        registry.register(AbilityRegistryTest.definition("first", 25));
        registry.register(AbilityRegistryTest.definition("second", 50));
        registry.register(AbilityRegistryTest.definition("later", 75));
        assertEquals(2, new AbilityUnlockService(registry)
                .unlockedBetween(GrowthToolType.PICKAXE, 20, 60).size());
    }
}
