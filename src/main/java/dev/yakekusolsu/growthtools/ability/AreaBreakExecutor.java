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
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.util.Vector;
import org.bukkit.block.BlockFace;

public final class AreaBreakExecutor implements AbilityExecutor {
    private final AbilitySettingsService abilitySettings;
    private final ExperienceSettingsService experienceSettings;
    private final AdditionalBlockBreakService breaker;
    private final GrowthToolUpdateService updates;

    public AreaBreakExecutor(AbilitySettingsService abilitySettings,
            ExperienceSettingsService experienceSettings, AdditionalBlockBreakService breaker,
            GrowthToolUpdateService updates) {
        this.abilitySettings = abilitySettings;
        this.experienceSettings = experienceSettings;
        this.breaker = breaker;
        this.updates = updates;
    }

    @Override
    public AbilityResult execute(PaperAbilityExecutionContext context, AbilityDefinition definition) {
        AbilitySettings.AreaBreak settings = abilitySettings.current().areaBreak();
        Block origin = context.origin();
        ExperienceSettings exp = experienceSettings.current();
        long extra = 0;
        int affected = 0;
        for (BlockPosition position : plane(context.player().getTargetBlockFace(6),
                context.player().getEyeLocation().getDirection())
                .positions(position(origin), settings.radius())) {
            if (context.tool().getType().isAir()) break;
            if (!origin.getWorld().isChunkLoaded(position.x() >> 4, position.z() >> 4)) continue;
            Block target = origin.getWorld().getBlockAt(position.x(), position.y(), position.z());
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
                ? AbilityResult.skipped(AbilityResult.Status.NO_VALID_TARGET, "no compatible blocks")
                : AbilityResult.success(affected, extra);
    }

    private static BlockPosition position(Block block) {
        return new BlockPosition(block.getX(), block.getY(), block.getZ());
    }

    private static AreaPlane plane(BlockFace face, Vector direction) {
        if (face != null) {
            return switch (face) {
                case UP, DOWN -> AreaPlane.XZ;
                case EAST, WEST -> AreaPlane.YZ;
                case NORTH, SOUTH -> AreaPlane.XY;
                default -> plane(direction);
            };
        }
        return plane(direction);
    }

    private static AreaPlane plane(Vector direction) {
        double x = Math.abs(direction.getX());
        double y = Math.abs(direction.getY());
        double z = Math.abs(direction.getZ());
        if (y >= x && y >= z) return AreaPlane.XZ;
        if (x >= z) return AreaPlane.YZ;
        return AreaPlane.XY;
    }
}
