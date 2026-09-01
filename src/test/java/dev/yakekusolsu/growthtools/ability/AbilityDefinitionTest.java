package dev.yakekusolsu.growthtools.ability;

import static org.junit.jupiter.api.Assertions.*;

import dev.yakekusolsu.growthtools.api.ability.*;
import dev.yakekusolsu.growthtools.model.GrowthToolType;
import java.util.Map;
import java.util.UUID;
import java.time.Duration;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

class AbilityDefinitionTest {
    @Test void enforcesEnabledTriggerTypeLevelAndConditions() {
        AbilityDefinition definition = AbilityRegistryTest.definition("test", 25);
        AbilityContext valid = new AbilityContext(UUID.randomUUID(), GrowthToolType.PICKAXE,
                25, AbilityTrigger.BLOCK_BREAK, Map.of());
        assertTrue(definition.canActivate(valid));
        assertFalse(definition.canActivate(new AbilityContext(valid.toolId(), GrowthToolType.AXE,
                25, AbilityTrigger.BLOCK_BREAK, Map.of())));
        assertFalse(definition.canActivate(new AbilityContext(valid.toolId(), GrowthToolType.PICKAXE,
                24, AbilityTrigger.BLOCK_BREAK, Map.of())));
    }

    @Test void rejectsInvalidDefinitionValues() {
        assertThrows(IllegalArgumentException.class, () -> new AbilityDefinition(
                AbilityId.parse("test:invalid"), "Invalid", "Invalid",
                AbilityTrigger.MANUAL, true, 0, Duration.ZERO,
                Set.of(GrowthToolType.PICKAXE), List.of(), Map.of()));
        assertThrows(IllegalArgumentException.class, () -> new AbilityDefinition(
                AbilityId.parse("test:invalid"), "Invalid", "Invalid",
                AbilityTrigger.MANUAL, true, 1, Duration.ofSeconds(-1),
                Set.of(GrowthToolType.PICKAXE), List.of(), Map.of()));
        assertThrows(IllegalArgumentException.class, () -> new AbilityDefinition(
                AbilityId.parse("test:oversized"), "x".repeat(129), "Invalid",
                AbilityTrigger.MANUAL, true, 1, Duration.ZERO,
                Set.of(GrowthToolType.PICKAXE), List.of(), Map.of()));
    }
}
