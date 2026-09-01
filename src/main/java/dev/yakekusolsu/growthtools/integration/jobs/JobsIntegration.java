package dev.yakekusolsu.growthtools.integration.jobs;

import com.gamingmesh.jobs.api.JobsPrePaymentEvent;
import dev.yakekusolsu.growthtools.ability.AbilityOriginContext;
import dev.yakekusolsu.growthtools.integration.IntegrationAdapter;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

/** Prevents Jobs payment for synthetic ability breaks without cancelling the block event. */
public final class JobsIntegration implements IntegrationAdapter, Listener {
    private final Plugin plugin;
    private final boolean rewardExtraBlocks;

    public JobsIntegration(Plugin plugin, boolean rewardExtraBlocks) {
        this.plugin = plugin;
        this.rewardExtraBlocks = rewardExtraBlocks;
    }

    @Override public String id() { return "jobs"; }
    @Override public String pluginName() { return "Jobs"; }
    @Override public void initialize() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPrePayment(JobsPrePaymentEvent event) {
        if (!rewardExtraBlocks && AbilityOriginContext.isAbilityOrigin()
                && event.getBlock() != null) {
            event.setCancelled(true);
        }
    }

    @Override public void close() { HandlerList.unregisterAll(this); }
}
