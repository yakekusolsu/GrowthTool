package dev.yakekusolsu.growthtools.ability;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import dev.yakekusolsu.growthtools.api.ability.AbilityId;
import org.junit.jupiter.api.Test;

class AbilityIdTest {
    @Test void parsesNamespacedAndDefaultIds() {
        assertEquals("other:test", AbilityId.parse("OTHER:TEST").toString());
        assertEquals("growthtools:vein_miner", AbilityId.parse("vein_miner").toString());
    }

    @Test void rejectsInvalidParts() {
        assertThrows(IllegalArgumentException.class, () -> AbilityId.parse("bad space:key"));
        assertThrows(IllegalArgumentException.class, () -> AbilityId.parse("a:b:c"));
        assertThrows(IllegalArgumentException.class, () -> AbilityId.parse(":"));
    }
}
