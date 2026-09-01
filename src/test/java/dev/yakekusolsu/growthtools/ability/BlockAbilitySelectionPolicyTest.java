package dev.yakekusolsu.growthtools.ability;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.yakekusolsu.growthtools.api.ability.AbilityId;
import dev.yakekusolsu.growthtools.config.AbilitySettings;
import dev.yakekusolsu.growthtools.model.GrowthToolData;
import dev.yakekusolsu.growthtools.model.GrowthToolType;
import java.time.Duration;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.bukkit.Material;
import org.junit.jupiter.api.Test;

class BlockAbilitySelectionPolicyTest {
    private static final AbilityId ADDON_ABILITY = new AbilityId("example", "block_break");
    private final BlockAbilitySelectionPolicy policy = new BlockAbilitySelectionPolicy();

    @Test
    void configuredOreSelectsOnlyVeinMinerWhenBothAbilitiesAreUnlocked() {
        Set<AbilityId> excluded = policy.excludedAbilities(
                tool(GrowthToolType.PICKAXE, 75), Material.IRON_ORE, settings());

        assertFalse(excluded.contains(BuiltinAbilities.VEIN_MINER));
        assertTrue(excluded.contains(BuiltinAbilities.AREA_BREAK));
        assertFalse(excluded.contains(ADDON_ABILITY));
    }

    @Test
    void ordinaryStoneSelectsOnlyAreaBreakWhenBothAbilitiesAreUnlocked() {
        Set<AbilityId> excluded = policy.excludedAbilities(
                tool(GrowthToolType.PICKAXE, 75), Material.STONE, settings());

        assertTrue(excluded.contains(BuiltinAbilities.VEIN_MINER));
        assertFalse(excluded.contains(BuiltinAbilities.AREA_BREAK));
        assertFalse(excluded.contains(ADDON_ABILITY));
    }

    private static AbilitySettings settings() {
        return new AbilitySettings(
                new AbilitySettings.VeinMiner(
                        true,
                        25,
                        Duration.ZERO,
                        true,
                        16,
                        true,
                        0.25D,
                        Map.of("iron", Set.of(Material.IRON_ORE, Material.DEEPSLATE_IRON_ORE))),
                new AbilitySettings.AreaBreak(true, 75, Duration.ZERO, true, 1, 0.25D),
                new AbilitySettings.AutoSmelt(true, 50, Duration.ZERO, true),
                new AbilitySettings.ExperienceBoost(true, 100, Duration.ZERO, 1.25D),
                false,
                true,
                8);
    }

    private static GrowthToolData tool(GrowthToolType type, int level) {
        return new GrowthToolData(UUID.randomUUID(), type, level, 0L, 1L, 1);
    }
}
