package dev.yakekusolsu.growthtools.api.event;

import dev.yakekusolsu.growthtools.api.ability.AbilityId;
import dev.yakekusolsu.growthtools.model.GrowthToolType;
import java.util.Objects;
import java.util.UUID;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

/** Fired synchronously for each registered ability crossed by a successful level gain. */
public final class GrowthToolAbilityUnlockEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();
    private final Player player;
    private final ItemStack tool;
    private final UUID toolId;
    private final GrowthToolType toolType;
    private final AbilityId abilityId;
    private final int level;

    public GrowthToolAbilityUnlockEvent(Player player, ItemStack tool, UUID toolId,
            GrowthToolType toolType, AbilityId abilityId, int level) {
        this.player = Objects.requireNonNull(player, "player");
        this.tool = Objects.requireNonNull(tool, "tool");
        this.toolId = Objects.requireNonNull(toolId, "toolId");
        this.toolType = Objects.requireNonNull(toolType, "toolType");
        this.abilityId = Objects.requireNonNull(abilityId, "abilityId");
        this.level = level;
    }

    public Player getPlayer() { return player; }
    public ItemStack getTool() { return tool; }
    public UUID getToolId() { return toolId; }
    public GrowthToolType getToolType() { return toolType; }
    public AbilityId getAbilityId() { return abilityId; }
    public int getLevel() { return level; }
    @Override public @NotNull HandlerList getHandlers() { return HANDLERS; }
    public static @NotNull HandlerList getHandlerList() { return HANDLERS; }
}
