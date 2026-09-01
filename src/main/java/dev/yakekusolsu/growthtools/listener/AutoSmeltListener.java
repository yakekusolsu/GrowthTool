package dev.yakekusolsu.growthtools.listener;

import dev.yakekusolsu.growthtools.ability.AbilityExecution;
import dev.yakekusolsu.growthtools.ability.AbilityService;
import dev.yakekusolsu.growthtools.ability.AbilityTriggerResolver;
import dev.yakekusolsu.growthtools.ability.BuiltinAbilities;
import dev.yakekusolsu.growthtools.ability.PaperAbilityExecutionContext;
import dev.yakekusolsu.growthtools.api.ability.AbilityResult;
import dev.yakekusolsu.growthtools.api.ability.AbilityTrigger;
import dev.yakekusolsu.growthtools.model.GrowthToolData;
import dev.yakekusolsu.growthtools.service.AbilityEventPublisher;
import dev.yakekusolsu.growthtools.service.AbilitySettingsService;
import dev.yakekusolsu.growthtools.service.GrowthToolItemService;
import java.util.Map;
import java.util.Optional;
import org.bukkit.GameMode;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.inventory.ItemStack;

public final class AutoSmeltListener implements Listener {
    private final AbilityService abilities;
    private final AbilitySettingsService settings;
    private final GrowthToolItemService items;
    private final AbilityEventPublisher events;
    private final AbilityTriggerResolver triggers = new AbilityTriggerResolver();

    public AutoSmeltListener(AbilityService abilities, AbilitySettingsService settings,
            GrowthToolItemService items, AbilityEventPublisher events) {
        this.abilities = abilities;
        this.settings = settings;
        this.items = items;
        this.events = events;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDrops(BlockDropItemEvent event) {
        if (event.getPlayer().getGameMode() == GameMode.CREATIVE
                && !settings.current().creativeMode()) return;
        ItemStack tool = event.getPlayer().getInventory().getItemInMainHand();
        Optional<GrowthToolData> data = items.read(tool);
        if (data.isEmpty()) return;
        PaperAbilityExecutionContext context = new PaperAbilityExecutionContext(
                triggers.resolve(data.get(), AbilityTrigger.BLOCK_BREAK, Map.of()),
                event.getPlayer(), tool, event.getBlockState().getBlock(), event.getItems());
        AbilityExecution execution = abilities.execute(BuiltinAbilities.AUTO_SMELT, context);
        if (execution.definition() != null
                && execution.result().status() == AbilityResult.Status.SUCCESS) {
            events.activated(event.getPlayer(), tool, data.get(), execution.definition(),
                    execution.result(), false);
        }
    }
}
