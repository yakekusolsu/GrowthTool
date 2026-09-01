package dev.yakekusolsu.growthtools.listener;

import dev.yakekusolsu.growthtools.config.ExperienceSettings;
import dev.yakekusolsu.growthtools.model.ExperienceSource;
import dev.yakekusolsu.growthtools.model.GrowthToolData;
import dev.yakekusolsu.growthtools.model.GrowthToolType;
import dev.yakekusolsu.growthtools.service.ExperienceSettingsService;
import dev.yakekusolsu.growthtools.service.GameplayEntityTracker;
import dev.yakekusolsu.growthtools.service.GrowthToolInventoryService;
import dev.yakekusolsu.growthtools.service.GrowthToolItemService;
import dev.yakekusolsu.growthtools.service.GrowthToolUpdateService;
import java.util.Optional;
import java.util.UUID;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.ItemStack;

/** Attributes actual fishing catches to the UUID-bearing rod used to cast. */
public final class FishingExperienceListener implements Listener {
    private final ExperienceSettingsService settingsService;
    private final GrowthToolItemService itemService;
    private final GrowthToolInventoryService inventoryService;
    private final GameplayEntityTracker entityTracker;
    private final GrowthToolUpdateService updateService;

    public FishingExperienceListener(
            ExperienceSettingsService settingsService,
            GrowthToolItemService itemService,
            GrowthToolInventoryService inventoryService,
            GameplayEntityTracker entityTracker,
            GrowthToolUpdateService updateService) {
        this.settingsService = settingsService;
        this.itemService = itemService;
        this.inventoryService = inventoryService;
        this.entityTracker = entityTracker;
        this.updateService = updateService;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onFish(PlayerFishEvent event) {
        ExperienceSettings settings = settingsService.current();
        if (!settings.fishingEnabled() || settings.fishingExperience() <= 0
                || (event.getPlayer().getGameMode() == GameMode.CREATIVE
                && !settings.creativeMode())) {
            return;
        }

        if (event.getState() == PlayerFishEvent.State.FISHING) {
            findHeldRod(event.getPlayer()).ifPresent(data ->
                    entityTracker.trackFishing(event.getHook(), data.toolId()));
            return;
        }
        if ((event.getState() != PlayerFishEvent.State.CAUGHT_FISH
                && event.getState() != PlayerFishEvent.State.CAUGHT_ENTITY)
                || event.getCaught() == null) {
            return;
        }

        Optional<UUID> toolId = entityTracker.claimFishing(event.getHook());
        if (toolId.isEmpty()) {
            return;
        }
        inventoryService.find(
                        event.getPlayer().getInventory(), toolId.get(), GrowthToolType.FISHING_ROD)
                .ifPresent(item -> updateService.addExperience(
                        event.getPlayer(), item, ExperienceSource.FISHING,
                        settings.fishingExperience()));
    }

    private Optional<GrowthToolData> findHeldRod(Player player) {
        ItemStack mainHand = player.getInventory().getItemInMainHand();
        Optional<GrowthToolData> mainData = itemService.read(mainHand)
                .filter(data -> data.type() == GrowthToolType.FISHING_ROD);
        if (mainData.isPresent()) {
            return mainData;
        }
        return itemService.read(player.getInventory().getItemInOffHand())
                .filter(data -> data.type() == GrowthToolType.FISHING_ROD);
    }
}
