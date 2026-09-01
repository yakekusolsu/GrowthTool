package dev.yakekusolsu.growthtools.listener;

import dev.yakekusolsu.growthtools.config.ExperienceSettings;
import dev.yakekusolsu.growthtools.ability.AbilityOriginContext;
import dev.yakekusolsu.growthtools.model.ExperienceSource;
import dev.yakekusolsu.growthtools.model.GrowthToolType;
import dev.yakekusolsu.growthtools.service.BlockToolCompatibilityService;
import dev.yakekusolsu.growthtools.service.ExperienceSettingsService;
import dev.yakekusolsu.growthtools.service.GrowthToolUpdateService;
import dev.yakekusolsu.growthtools.service.MaterialToolTypeMapper;
import dev.yakekusolsu.growthtools.storage.PlacedBlockTracker;
import java.util.Optional;
import java.util.function.BooleanSupplier;
import java.util.logging.Logger;
import org.bukkit.GameMode;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

/** Awards configured block EXP after all cancellation decisions have completed. */
public final class BlockExperienceListener implements Listener {
    private final ExperienceSettingsService settingsService;
    private final MaterialToolTypeMapper typeMapper;
    private final BlockToolCompatibilityService compatibilityService;
    private final GrowthToolUpdateService updateService;
    private final PlacedBlockTracker placedBlockTracker;
    private final BooleanSupplier debugEnabled;
    private final Logger logger;

    public BlockExperienceListener(
            ExperienceSettingsService settingsService,
            MaterialToolTypeMapper typeMapper,
            BlockToolCompatibilityService compatibilityService,
            GrowthToolUpdateService updateService,
            PlacedBlockTracker placedBlockTracker,
            BooleanSupplier debugEnabled,
            Logger logger) {
        this.settingsService = settingsService;
        this.typeMapper = typeMapper;
        this.compatibilityService = compatibilityService;
        this.updateService = updateService;
        this.placedBlockTracker = placedBlockTracker;
        this.debugEnabled = debugEnabled;
        this.logger = logger;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        if (AbilityOriginContext.isAbilityOrigin()) {
            return;
        }
        Block block = event.getBlock();
        ExperienceSettings settings = settingsService.current();
        long amount = settings.blockExperience(block.getType());
        ItemStack item = event.getPlayer().getInventory().getItemInMainHand();
        Optional<GrowthToolType> toolType = typeMapper.fromMaterial(item.getType());
        boolean compatible = toolType
                .map(type -> compatibilityService.isCompatible(type, block.getType()))
                .orElse(false);
        boolean mature = toolType
                .map(type -> compatibilityService.isMatureForExperience(type, block))
                .orElse(true);
        boolean creative = event.getPlayer().getGameMode() == GameMode.CREATIVE;

        if (!placedBlockTracker.isReady()) {
            debugRejected("placed-block-tracker-not-ready", block, amount,
                    toolType, compatible, false, creative);
            return;
        }
        boolean placed = placedBlockTracker.isPlayerPlaced(block);
        if (placed) {
            placedBlockTracker.unmark(block);
            boolean placedCropAllowed = toolType
                    .map(type -> compatibilityService.permitsPlacedBlockExperience(type, block))
                    .orElse(false);
            if (!placedCropAllowed) {
                debugRejected(mature ? "player-placed" : "immature-crop", block, amount,
                        toolType, compatible, true, creative);
                return;
            }
        }

        if (creative && !settings.creativeMode()) {
            debugRejected("creative-disabled", block, amount,
                    toolType, compatible, placed, true);
            return;
        }
        if (amount <= 0) {
            debugRejected("not-configured", block, amount,
                    toolType, compatible, placed, creative);
            return;
        }

        if (toolType.isEmpty()) {
            debugRejected("unsupported-main-hand", block, amount,
                    toolType, false, placed, creative);
            return;
        }
        if (!compatible) {
            debugRejected("incompatible-tool", block, amount,
                    toolType, false, placed, creative);
            return;
        }
        if (!mature) {
            debugRejected("immature-crop", block, amount,
                    toolType, true, placed, creative);
            return;
        }
        updateService.addExperience(event.getPlayer(), item, ExperienceSource.BLOCK_BREAK, amount);
    }

    private void debugRejected(String reason, Block block, long configuredExperience,
            Optional<GrowthToolType> toolType, boolean compatible,
            boolean placed, boolean creative) {
        if (!debugEnabled.getAsBoolean()) {
            return;
        }
        logger.info("Block EXP rejected: reason=" + reason
                + " material=" + block.getType()
                + " configuredExp=" + configuredExperience
                + " toolType=" + toolType.map(Enum::name).orElse("NONE")
                + " compatibility=" + compatible
                + " placed=" + placed
                + " creative=" + creative);
    }
}
