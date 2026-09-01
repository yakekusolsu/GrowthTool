package dev.yakekusolsu.growthtools.service;

import dev.yakekusolsu.growthtools.api.ability.AbilityDefinition;
import dev.yakekusolsu.growthtools.api.ability.AbilityResult;
import dev.yakekusolsu.growthtools.api.event.GrowthToolAbilityActivateEvent;
import dev.yakekusolsu.growthtools.model.GrowthToolData;
import java.util.Map;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

public final class AbilityEventPublisher {
    private final Plugin plugin;
    private final MessageService messages;

    public AbilityEventPublisher(Plugin plugin, MessageService messages) {
        this.plugin = plugin;
        this.messages = messages;
    }

    public void activated(Player player, ItemStack tool, GrowthToolData data,
            AbilityDefinition definition, AbilityResult result, boolean notify) {
        if (result.status() != AbilityResult.Status.SUCCESS) {
            return;
        }
        plugin.getServer().getPluginManager().callEvent(new GrowthToolAbilityActivateEvent(
                player, tool, data.toolId(), data.type(), definition.id(),
                definition.trigger(), result));
        if (notify) {
            messages.send(player, "ability.activated", Map.of(
                    "ability", definition.displayName(),
                    "blocks", Integer.toString(result.affectedBlocks())));
        }
    }
}
