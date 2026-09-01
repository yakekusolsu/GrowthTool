package dev.yakekusolsu.growthtools.listener;

import dev.yakekusolsu.growthtools.config.ExperienceSettings;
import dev.yakekusolsu.growthtools.model.ExperienceSource;
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
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.inventory.ItemStack;

/** Attributes one successful projectile damage event to the UUID-bearing bow that fired it. */
public final class BowExperienceListener implements Listener {
    private final ExperienceSettingsService settingsService;
    private final GrowthToolItemService itemService;
    private final GrowthToolInventoryService inventoryService;
    private final GameplayEntityTracker entityTracker;
    private final GrowthToolUpdateService updateService;

    public BowExperienceListener(
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
    public void onShoot(EntityShootBowEvent event) {
        if (!(event.getEntity() instanceof Player player)
                || !(event.getProjectile() instanceof Projectile projectile)) {
            return;
        }
        ExperienceSettings settings = settingsService.current();
        if (!settings.bowEnabled() || settings.bowHitExperience() <= 0
                || (player.getGameMode() == GameMode.CREATIVE && !settings.creativeMode())) {
            return;
        }
        ItemStack bow = event.getBow();
        if (bow == null) {
            return;
        }
        itemService.read(bow)
                .filter(data -> data.type() == GrowthToolType.BOW)
                .ifPresent(data -> entityTracker.trackBow(projectile, data.toolId()));
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onProjectileDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Projectile projectile)
                || !(projectile.getShooter() instanceof Player player)
                || event.getFinalDamage() <= 0) {
            return;
        }
        ExperienceSettings settings = settingsService.current();
        if (!settings.bowEnabled() || settings.bowHitExperience() <= 0
                || (player.getGameMode() == GameMode.CREATIVE && !settings.creativeMode())) {
            return;
        }

        Optional<UUID> toolId = entityTracker.claimBow(projectile);
        if (toolId.isEmpty()) {
            return;
        }
        inventoryService.find(player.getInventory(), toolId.get(), GrowthToolType.BOW)
                .ifPresent(item -> updateService.addExperience(
                        player, item, ExperienceSource.BOW_DAMAGE, settings.bowHitExperience()));
    }
}
