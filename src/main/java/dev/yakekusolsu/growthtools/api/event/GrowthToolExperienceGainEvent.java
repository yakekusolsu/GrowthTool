package dev.yakekusolsu.growthtools.api.event;

import dev.yakekusolsu.growthtools.api.experience.ExperienceSourceId;
import dev.yakekusolsu.growthtools.model.GrowthToolType;
import java.util.UUID;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * Fired synchronously after a successful PDC/lore EXP update and before level-up events.
 * Getter values are non-null; the ItemStack is live Bukkit state and is main-thread-only.
 */
public final class GrowthToolExperienceGainEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();

    private final Player player;
    private final ItemStack item;
    private final UUID toolId;
    private final GrowthToolType toolType;
    private final ExperienceSourceId source;
    private final long requestedAmount;
    private final long actualAmount;
    private final long oldExperience;
    private final long newExperience;

    public GrowthToolExperienceGainEvent(
            Player player,
            ItemStack item,
            UUID toolId,
            GrowthToolType toolType,
            ExperienceSourceId source,
            long requestedAmount,
            long actualAmount,
            long oldExperience,
            long newExperience) {
        this.player = player;
        this.item = item;
        this.toolId = toolId;
        this.toolType = toolType;
        this.source = source;
        this.requestedAmount = requestedAmount;
        this.actualAmount = actualAmount;
        this.oldExperience = oldExperience;
        this.newExperience = newExperience;
    }

    public Player getPlayer() { return player; }
    public ItemStack getItem() { return item; }
    public UUID getToolId() { return toolId; }
    public GrowthToolType getToolType() { return toolType; }
    public ExperienceSourceId getSource() { return source; }
    public long getRequestedAmount() { return requestedAmount; }
    public long getActualAmount() { return actualAmount; }
    public long getOldExperience() { return oldExperience; }
    public long getNewExperience() { return newExperience; }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public static @NotNull HandlerList getHandlerList() {
        return HANDLERS;
    }
}
