package dev.yakekusolsu.growthtools.ability;

import dev.yakekusolsu.growthtools.api.ability.AbilityId;
import dev.yakekusolsu.growthtools.config.AbilitySettings;
import dev.yakekusolsu.growthtools.model.GrowthToolData;
import dev.yakekusolsu.growthtools.model.GrowthToolType;
import java.util.HashSet;
import java.util.Set;
import org.bukkit.Material;

/** Selects one built-in multi-block mining ability for a player-originated block break. */
public final class BlockAbilitySelectionPolicy {
    private final OreGroupResolver oreGroups = new OreGroupResolver();

    public Set<AbilityId> excludedAbilities(
            GrowthToolData tool, Material origin, AbilitySettings settings) {
        Set<AbilityId> excluded = new HashSet<>();
        excluded.add(BuiltinAbilities.AUTO_SMELT);
        if (veinMinerHasPriority(tool, origin, settings.veinMiner())) {
            excluded.add(BuiltinAbilities.AREA_BREAK);
        } else {
            excluded.add(BuiltinAbilities.VEIN_MINER);
        }
        return Set.copyOf(excluded);
    }

    private boolean veinMinerHasPriority(
            GrowthToolData tool, Material origin, AbilitySettings.VeinMiner settings) {
        return tool.type() == GrowthToolType.PICKAXE
                && settings.enabled()
                && tool.level() >= settings.unlockLevel()
                && oreGroups.groupFor(origin, settings.oreGroups()).isPresent();
    }
}
