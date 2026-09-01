package dev.yakekusolsu.growthtools.service;

import dev.yakekusolsu.growthtools.ability.AbilityOriginContext;
import dev.yakekusolsu.growthtools.ability.PaperAbilityExecutionContext;
import dev.yakekusolsu.growthtools.storage.PlacedBlockTracker;
import java.util.Collection;
import java.util.Objects;
import java.util.function.Consumer;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.GameMode;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import dev.yakekusolsu.growthtools.integration.BlockProtectionGate;
import dev.yakekusolsu.growthtools.integration.IntegrationManager;

/** Executes one protection-compatible ability break without loading chunks. */
public final class AdditionalBlockBreakService {
    private final Plugin plugin;
    private final PlacedBlockTracker placedBlocks;
    private final BlockToolCompatibilityService compatibility;
    private final BlockProtectionGate protection;
    private final Consumer<Block> extraBlockMarker;
    private final DropTransformationPipeline transformations;
    private final BlockDropResolver drops;

    public AdditionalBlockBreakService(
            Plugin plugin, PlacedBlockTracker placedBlocks,
            BlockToolCompatibilityService compatibility,
            IntegrationManager integrations,
            DropTransformationPipeline transformations) {
        this(plugin, placedBlocks, compatibility, integrations,
                integrations::markExtraBlock, transformations, Block::getDrops);
    }

    AdditionalBlockBreakService(
            Plugin plugin, PlacedBlockTracker placedBlocks,
            BlockToolCompatibilityService compatibility,
            BlockProtectionGate protection, Consumer<Block> extraBlockMarker,
            DropTransformationPipeline transformations, BlockDropResolver drops) {
        this.plugin = plugin;
        this.placedBlocks = placedBlocks;
        this.compatibility = compatibility;
        this.protection = protection;
        this.extraBlockMarker = Objects.requireNonNull(extraBlockMarker, "extraBlockMarker");
        this.transformations = transformations;
        this.drops = drops;
    }

    public boolean breakBlock(PaperAbilityExecutionContext context, Block block) {
        Player player = context.player();
        ItemStack tool = context.tool();
        Material originalMaterial = block.getType();
        if (tool.getType().isAir() || block.getType().isAir()
                || block.getType().getHardness() < 0.0F
                || !block.getWorld().isChunkLoaded(block.getX() >> 4, block.getZ() >> 4)
                || placedBlocks.isPlayerPlaced(block)
                || !protection.canBreak(player, block)
                || !compatibility.isCompatible(context.domain().toolType(), block.getType())) {
            return false;
        }
        BlockBreakEvent event = new BlockBreakEvent(block, player);
        extraBlockMarker.accept(block);
        AbilityOriginContext.Scope scope = AbilityOriginContext.enter();
        try {
            plugin.getServer().getPluginManager().callEvent(event);
            if (event.isCancelled() || block.getType() != originalMaterial) {
                return false;
            }
            if (event.isDropItems()) {
                Collection<ItemStack> naturalDrops = drops.resolve(block, tool, player);
                DropTransformationResult result = transformations.transform(
                        new DropTransformationContext(
                                context.domain().toolType(), context.domain().toolLevel(), tool),
                        naturalDrops);
                Location dropLocation = block.getLocation().add(0.5D, 0.5D, 0.5D);
                block.setType(Material.AIR, true);
                for (ItemStack drop : result.drops()) {
                    if (!drop.getType().isAir() && drop.getAmount() > 0) {
                        block.getWorld().dropItemNaturally(dropLocation, drop);
                    }
                }
            } else {
                block.setType(Material.AIR, true);
            }
            if (player.getGameMode() != GameMode.CREATIVE) {
                tool.damage(1, player);
            }
            return true;
        } finally {
            scope.close();
        }
    }
}
