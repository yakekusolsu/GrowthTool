package dev.yakekusolsu.growthtools.listener;

import dev.yakekusolsu.growthtools.api.internal.AbilityManagerImpl;
import dev.yakekusolsu.growthtools.integration.IntegrationManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;

public final class AddonLifecycleListener implements Listener {
    private final AbilityManagerImpl abilities;
    private final IntegrationManager integrations;
    public AddonLifecycleListener(AbilityManagerImpl abilities, IntegrationManager integrations) {
        this.abilities = abilities; this.integrations = integrations;
    }
    @EventHandler public void onDisable(PluginDisableEvent event) {
        abilities.unregisterOwnedBy(event.getPlugin());
        integrations.refresh(event.getPlugin().getName());
    }
    @EventHandler public void onEnable(PluginEnableEvent event) {
        integrations.refresh(event.getPlugin().getName());
    }
}
