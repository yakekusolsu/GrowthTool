package dev.yakekusolsu.growthtools.ability;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.yakekusolsu.growthtools.api.ability.AbilityContext;
import dev.yakekusolsu.growthtools.api.ability.AbilityDefinition;
import dev.yakekusolsu.growthtools.api.ability.AbilityTrigger;
import dev.yakekusolsu.growthtools.config.AbilitySettings;
import dev.yakekusolsu.growthtools.model.GrowthToolType;
import java.time.Duration;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class VeinMinerActivationPolicyTest {
    @Test
    void sneakingBlockBreakIsEligibleWhenSneakingIsRequired() {
        AbilityDefinition veinMiner = definition(true);

        assertTrue(veinMiner.canActivate(context(true)));
    }

    @Test
    void ordinaryBlockBreakIsRejectedWhenSneakingIsRequired() {
        AbilityDefinition veinMiner = definition(true);

        assertFalse(veinMiner.canActivate(context(false)));
    }

    @Test
    void ordinaryBlockBreakIsEligibleWhenSneakingIsNotRequired() {
        AbilityDefinition veinMiner = definition(false);

        assertTrue(veinMiner.canActivate(context(false)));
    }

    private static AbilityDefinition definition(boolean requireSneak) {
        AbilityRegistry registry = new AbilityRegistry();
        AbilitySettings.VeinMiner veinMiner = new AbilitySettings.VeinMiner(
                true,
                25,
                Duration.ZERO,
                requireSneak,
                16,
                true,
                0.25D,
                Map.of());
        BuiltinAbilities.register(registry, new AbilitySettings(
                veinMiner,
                new AbilitySettings.AreaBreak(true, 75, Duration.ZERO, true, 1, 0.25D),
                new AbilitySettings.AutoSmelt(true, 50, Duration.ZERO, true),
                new AbilitySettings.ExperienceBoost(true, 100, Duration.ZERO, 1.25D),
                false,
                true,
                8));
        return registry.get(BuiltinAbilities.VEIN_MINER).orElseThrow();
    }

    private static AbilityContext context(boolean sneaking) {
        return new AbilityContext(
                UUID.randomUUID(),
                GrowthToolType.PICKAXE,
                25,
                AbilityTrigger.BLOCK_BREAK,
                Map.of("sneaking", Boolean.toString(sneaking)));
    }
}
