package dev.yakekusolsu.growthtools.listener;

import dev.yakekusolsu.growthtools.ability.AbilityExecution;
import dev.yakekusolsu.growthtools.ability.AbilityOriginContext;
import dev.yakekusolsu.growthtools.ability.AbilityService;
import dev.yakekusolsu.growthtools.ability.AbilityTriggerResolver;
import dev.yakekusolsu.growthtools.ability.BlockAbilitySelectionPolicy;
import dev.yakekusolsu.growthtools.ability.PaperAbilityExecutionContext;
import dev.yakekusolsu.growthtools.api.ability.AbilityResult;
import dev.yakekusolsu.growthtools.api.ability.AbilityTrigger;
import dev.yakekusolsu.growthtools.config.AbilitySettings;
import dev.yakekusolsu.growthtools.model.GrowthToolData;
import dev.yakekusolsu.growthtools.service.AbilityEventPublisher;
import dev.yakekusolsu.growthtools.service.AbilitySettingsService;
import dev.yakekusolsu.growthtools.service.GrowthToolItemService;
import dev.yakekusolsu.growthtools.storage.PlacedBlockTracker;
import java.util.Map;
import java.util.Optional;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

/** Thin gameplay adapter: resolve trigger, delegate, and publish successful results. */
public final class BlockAbilityListener implements Listener {
    private final AbilityService abilities;
    private final AbilitySettingsService settings;
    private final GrowthToolItemService items;
    private final PlacedBlockTracker placedBlocks;
    private final AbilityEventPublisher events;
    private final AbilityTriggerResolver triggers = new AbilityTriggerResolver();
    private final BlockAbilitySelectionPolicy selections = new BlockAbilitySelectionPolicy();

    public BlockAbilityListener(AbilityService abilities, AbilitySettingsService settings,
            GrowthToolItemService items, PlacedBlockTracker placedBlocks,
            AbilityEventPublisher events) {
        this.abilities = abilities;
        this.settings = settings;
        this.items = items;
        this.placedBlocks = placedBlocks;
        this.events = events;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        if (AbilityOriginContext.isAbilityOrigin() || !placedBlocks.isReady()
                || placedBlocks.isPlayerPlaced(event.getBlock())) return;
        Player player = event.getPlayer();
        AbilitySettings snapshot = settings.current();
        if (player.getGameMode() == GameMode.CREATIVE && !snapshot.creativeMode()) return;
        ItemStack tool = player.getInventory().getItemInMainHand();
        Optional<GrowthToolData> data = items.read(tool);
        if (data.isEmpty()) return;
        GrowthToolData toolData = data.get();
        PaperAbilityExecutionContext context = new PaperAbilityExecutionContext(
                triggers.resolve(toolData, AbilityTrigger.BLOCK_BREAK,
                        Map.of("sneaking", Boolean.toString(player.isSneaking()))),
                player, tool, event.getBlock());
        for (AbilityExecution execution : abilities.executeTrigger(
                context, selections.excludedAbilities(
                        toolData, event.getBlock().getType(), snapshot))) {
            if (tool.getType().isAir()) break;
            publish(context, execution, toolData);
        }
    }

    private void publish(PaperAbilityExecutionContext context, AbilityExecution execution,
            GrowthToolData data) {
        if (execution.definition() != null
                && execution.result().status() == AbilityResult.Status.SUCCESS) {
            events.activated(context.player(), context.tool(), data,
                    execution.definition(), execution.result(), false);
        }
    }
}
