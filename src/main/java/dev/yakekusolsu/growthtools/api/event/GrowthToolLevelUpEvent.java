package dev.yakekusolsu.growthtools.api.event;

import dev.yakekusolsu.growthtools.model.GrowthToolType;
import java.util.UUID;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

/** Fired synchronously after a GrowthTool's PDC and lore have been updated for a level gain. */
public final class GrowthToolLevelUpEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();

    private final Player player;
    private final ItemStack item;
    private final UUID toolId;
    private final GrowthToolType toolType;
    private final int oldLevel;
    private final int newLevel;

    public GrowthToolLevelUpEvent(
            Player player,
            ItemStack item,
            UUID toolId,
            GrowthToolType toolType,
            int oldLevel,
            int newLevel) {
        this.player = player;
        this.item = item;
        this.toolId = toolId;
        this.toolType = toolType;
        this.oldLevel = oldLevel;
        this.newLevel = newLevel;
    }

    public Player getPlayer() { return player; }
    public ItemStack getItem() { return item; }
    public UUID getToolId() { return toolId; }
    public GrowthToolType getToolType() { return toolType; }
    public int getOldLevel() { return oldLevel; }
    public int getNewLevel() { return newLevel; }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public static @NotNull HandlerList getHandlerList() {
        return HANDLERS;
    }
}
