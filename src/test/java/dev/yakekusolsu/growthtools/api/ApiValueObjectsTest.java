package dev.yakekusolsu.growthtools.api;

import static org.junit.jupiter.api.Assertions.*;

import dev.yakekusolsu.growthtools.api.ability.AbilityId;
import dev.yakekusolsu.growthtools.api.experience.ExperienceSourceId;
import org.junit.jupiter.api.Test;

class ApiValueObjectsTest {
    @Test void apiVersionOrdersMajorBeforeMinor() {
        assertEquals("1.0", ApiVersion.CURRENT.toString());
        assertTrue(new ApiVersion(2, 0).compareTo(new ApiVersion(1, 99)) > 0);
    }

    @Test void negativeApiVersionIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> new ApiVersion(-1, 0));
    }

    @Test void namespacedIdentifiersNormalizeAndValidate() {
        assertEquals("example:quest_reward",
                new ExperienceSourceId("Example", "Quest_Reward").toString());
        assertEquals(new AbilityId("growthtools", "vein_miner"),
                AbilityId.parse("vein_miner"));
        assertThrows(IllegalArgumentException.class,
                () -> new ExperienceSourceId("bad namespace", "reward"));
        assertThrows(IllegalArgumentException.class,
                () -> new AbilityId("a".repeat(65), "reward"));
    }
}
