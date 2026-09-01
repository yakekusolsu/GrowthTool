package dev.yakekusolsu.growthtools.example;

import dev.yakekusolsu.growthtools.api.GrowthToolsAPI;
import dev.yakekusolsu.growthtools.api.GrowthToolsProvider;
import dev.yakekusolsu.growthtools.api.ability.AbilityDefinition;
import dev.yakekusolsu.growthtools.api.ability.AbilityId;
import dev.yakekusolsu.growthtools.api.ability.AbilityRegistration;
import dev.yakekusolsu.growthtools.api.ability.AbilityResult;
import dev.yakekusolsu.growthtools.api.ability.AbilityTrigger;
import dev.yakekusolsu.growthtools.api.event.GrowthToolAbilityUnlockEvent;
import dev.yakekusolsu.growthtools.api.experience.ExperienceSourceId;
import dev.yakekusolsu.growthtools.model.GrowthToolType;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

/** Minimal consumer of the experimental GrowthTools API. */
public final class GrowthToolsExamplePlugin extends JavaPlugin implements Listener {
    private AbilityRegistration registration;

    @Override public void onEnable() {
        GrowthToolsAPI api = GrowthToolsProvider.get();
        AbilityDefinition definition = new AbilityDefinition(
                new AbilityId("growthtoolsexample", "level_notice"),
                "Level notice", "Demonstrates an addon executor on a block break.",
                AbilityTrigger.BLOCK_BREAK, true, 1, Duration.ZERO,
                Set.of(GrowthToolType.PICKAXE), List.of(), Map.of());
        registration = api.abilities().register(this, definition, (context, ignored) -> {
            context.player().sendMessage("GrowthTools example ability ran for level "
                    + context.domain().toolLevel() + '.');
            return AbilityResult.success(0, 0);
        });
        getLogger().info("Registered " + registration.id() + " using GrowthTools API "
                + api.version() + '.');
        getServer().getPluginManager().registerEvents(this, this);
        var command = getCommand("gtexample");
        if (command != null) command.setExecutor((sender, ignored, label, args) -> {
            if (!(sender instanceof Player player)) return true;
            var tool = player.getInventory().getItemInMainHand();
            api.tools().getTool(tool).ifPresent(snapshot -> player.sendMessage(
                    "Held GrowthTool level: " + snapshot.level()));
            api.experience().addExperience(player, tool, 1,
                    new ExperienceSourceId("growthtoolsexample", "command"));
            return true;
        });
    }

    @EventHandler public void onAbilityUnlock(GrowthToolAbilityUnlockEvent event) {
        getLogger().info(event.getPlayer().getName() + " unlocked " + event.getAbilityId());
    }

    @Override public void onDisable() {
        if (registration != null && GrowthToolsProvider.isAvailable()) {
            getLogger().info("Unregistered example ability: " + registration.unregister());
        }
        registration = null;
    }
}
