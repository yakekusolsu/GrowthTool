package dev.yakekusolsu.growthtools.ability;

import dev.yakekusolsu.growthtools.api.ability.AbilityDefinition;
import dev.yakekusolsu.growthtools.api.ability.AbilityResult;
import dev.yakekusolsu.growthtools.config.AbilitySettings;
import dev.yakekusolsu.growthtools.config.ExperienceSettings;
import dev.yakekusolsu.growthtools.model.ExperienceSource;
import dev.yakekusolsu.growthtools.service.AbilitySettingsService;
import dev.yakekusolsu.growthtools.service.AdditionalBlockBreakService;
import dev.yakekusolsu.growthtools.service.ExperienceSettingsService;
import dev.yakekusolsu.growthtools.service.GrowthToolUpdateService;
import dev.yakekusolsu.growthtools.storage.PlacedBlockTracker;
import java.util.Optional;
import java.util.Set;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;

public final class VeinMinerExecutor implements AbilityExecutor {
    private final AbilitySettingsService abilitySettings;
    private final ExperienceSettingsService experienceSettings;
    private final PlacedBlockTracker placedBlocks;
    private final AdditionalBlockBreakService breaker;
    private final GrowthToolUpdateService updates;
    private final VeinTraversal traversal = new VeinTraversal();
    private final OreGroupResolver groups = new OreGroupResolver();

    public VeinMinerExecutor(AbilitySettingsService abilitySettings,
            ExperienceSettingsService experienceSettings, PlacedBlockTracker placedBlocks,
            AdditionalBlockBreakService breaker, GrowthToolUpdateService updates) {
        this.abilitySettings = abilitySettings;
        this.experienceSettings = experienceSettings;
        this.placedBlocks = placedBlocks;
        this.breaker = breaker;
        this.updates = updates;
    }

    @Override
    public AbilityResult execute(PaperAbilityExecutionContext context, AbilityDefinition definition) {
        AbilitySettings.VeinMiner settings = abilitySettings.current().veinMiner();
        Block origin = context.origin();
        Optional<Set<Material>> oreGroup = groups.groupFor(origin.getType(), settings.oreGroups());
        if (oreGroup.isEmpty()) {
            return AbilityResult.skipped(AbilityResult.Status.NO_VALID_TARGET, "not configured ore");
        }
        World world = origin.getWorld();
        BlockPosition start = position(origin);
        var positions = traversal.find(start, position -> {
            if (!world.isChunkLoaded(position.x() >> 4, position.z() >> 4)) return false;
            Block candidate = world.getBlockAt(position.x(), position.y(), position.z());
            return oreGroup.get().contains(candidate.getType())
                    && !placedBlocks.isPlayerPlaced(candidate);
        }, settings.diagonal(), settings.maximumBlocks());
        ExperienceSettings exp = experienceSettings.current();
        long extra = 0;
        int affected = 0;
        for (BlockPosition position : positions) {
            if (position.equals(start) || context.tool().getType().isAir()) continue;
            Block target = world.getBlockAt(position.x(), position.y(), position.z());
            Material material = target.getType();
            if (breaker.breakBlock(context, target)) {
                affected++;
                extra = AbilityMath.saturatingAdd(extra,
                        AbilityMath.scaled(exp.blockExperience(material),
                                settings.extraExperienceMultiplier()));
            }
        }
        if (extra > 0 && !context.tool().getType().isAir()) {
            updates.addExperience(context.player(), context.tool(), ExperienceSource.BLOCK_BREAK, extra);
        }
        return affected == 0
                ? AbilityResult.skipped(AbilityResult.Status.NO_VALID_TARGET, "no extra blocks")
                : AbilityResult.success(affected, extra);
    }

    private static BlockPosition position(Block block) {
        return new BlockPosition(block.getX(), block.getY(), block.getZ());
    }
}
