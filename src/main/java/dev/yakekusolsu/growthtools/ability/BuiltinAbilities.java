package dev.yakekusolsu.growthtools.ability;

import dev.yakekusolsu.growthtools.api.ability.AbilityDefinition;
import dev.yakekusolsu.growthtools.api.ability.AbilityId;
import dev.yakekusolsu.growthtools.api.ability.AbilityCondition;
import dev.yakekusolsu.growthtools.api.ability.AbilityTrigger;
import dev.yakekusolsu.growthtools.config.AbilitySettings;
import dev.yakekusolsu.growthtools.model.GrowthToolType;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Registers only the four built-ins promised for Phase 5. */
public final class BuiltinAbilities {
    public static final AbilityId VEIN_MINER = new AbilityId("growthtools", "vein_miner");
    public static final AbilityId AREA_BREAK = new AbilityId("growthtools", "area_break");
    public static final AbilityId AUTO_SMELT = new AbilityId("growthtools", "auto_smelt");
    public static final AbilityId EXPERIENCE_BOOST = new AbilityId("growthtools", "experience_boost");
    static final Set<GrowthToolType> AUTO_SMELT_TOOL_TYPES = Set.of(GrowthToolType.PICKAXE);

    private BuiltinAbilities() {
    }

    public static void register(AbilityRegistry registry, AbilitySettings settings) {
        List.of(VEIN_MINER, AREA_BREAK, AUTO_SMELT, EXPERIENCE_BOOST)
                .forEach(registry::unregister);
        AbilitySettings.VeinMiner vein = settings.veinMiner();
        registry.register(new AbilityDefinition(VEIN_MINER, "Vein Miner",
                "Breaks a bounded vein of matching ore.", AbilityTrigger.BLOCK_BREAK,
                vein.enabled(), vein.unlockLevel(), vein.cooldown(), Set.of(GrowthToolType.PICKAXE),
                sneakConditions(vein.requireSneak()),
                Map.of("max-blocks", Integer.toString(vein.maximumBlocks()),
                        "diagonal", Boolean.toString(vein.diagonal()),
                        "require-sneak", Boolean.toString(vein.requireSneak()))));
        AbilitySettings.AutoSmelt smelt = settings.autoSmelt();
        registry.register(definition(AUTO_SMELT, "Auto Smelt",
                "Transforms eligible block drops using furnace recipes.", AbilityTrigger.BLOCK_BREAK,
                smelt.enabled(), smelt.unlockLevel(), smelt.cooldown(), AUTO_SMELT_TOOL_TYPES,
                Map.of("disable-with-silk-touch", Boolean.toString(smelt.disableWithSilkTouch()))));
        AbilitySettings.AreaBreak area = settings.areaBreak();
        registry.register(new AbilityDefinition(AREA_BREAK, "Area Break",
                "Breaks a bounded plane around the targeted block.", AbilityTrigger.BLOCK_BREAK,
                area.enabled(), area.unlockLevel(), area.cooldown(),
                Set.of(GrowthToolType.PICKAXE, GrowthToolType.SHOVEL),
                sneakConditions(area.requireSneak()),
                Map.of("radius", Integer.toString(area.radius()),
                        "require-sneak", Boolean.toString(area.requireSneak()))));
        AbilitySettings.ExperienceBoost boost = settings.experienceBoost();
        registry.register(definition(EXPERIENCE_BOOST, "Experience Boost",
                "Multiplies Growth EXP from supported sources.", AbilityTrigger.EXPERIENCE_GAIN,
                boost.enabled(), boost.unlockLevel(), boost.cooldown(), Set.of(GrowthToolType.values()),
                Map.of("multiplier", Double.toString(boost.multiplier()))));
    }

    private static AbilityDefinition definition(
            AbilityId id, String name, String description, AbilityTrigger trigger, boolean enabled,
            int level, java.time.Duration cooldown, Set<GrowthToolType> types,
            Map<String, String> settings) {
        return new AbilityDefinition(id, name, description, trigger, enabled, level, cooldown,
                types, List.of(), settings);
    }

    private static List<AbilityCondition> sneakConditions(boolean requireSneak) {
        if (!requireSneak) {
            return List.of();
        }
        return List.of(context -> Boolean.parseBoolean(
                context.attributes().getOrDefault("sneaking", "false")));
    }
}
