package dev.yakekusolsu.growthtools.ability;

import static org.junit.jupiter.api.Assertions.*;

import dev.yakekusolsu.growthtools.api.ability.*;
import dev.yakekusolsu.growthtools.model.GrowthToolType;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

class AbilityRegistryTest {
    @Test void registerLookupEnumerateAndUnregister() {
        AbilityRegistry registry = new AbilityRegistry();
        AbilityDefinition definition = definition("one", 5);
        registry.register(definition);
        assertTrue(registry.contains(definition.id()));
        assertEquals(definition, registry.get(definition.id()).orElseThrow());
        assertEquals(List.of(definition), registry.getAll());
        assertEquals(definition, registry.unregister(definition.id()).orElseThrow());
        assertFalse(registry.contains(definition.id()));
    }

    @Test void rejectsDuplicateIds() {
        AbilityRegistry registry = new AbilityRegistry();
        registry.register(definition("one", 5));
        assertThrows(IllegalArgumentException.class, () -> registry.register(definition("one", 6)));
    }

    static AbilityDefinition definition(String key, int level) {
        return new AbilityDefinition(new AbilityId("test", key), key, "description",
                AbilityTrigger.BLOCK_BREAK, true, level, Duration.ZERO,
                Set.of(GrowthToolType.PICKAXE), List.of(), Map.of());
    }
}
