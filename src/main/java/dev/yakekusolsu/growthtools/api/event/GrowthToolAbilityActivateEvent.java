package dev.yakekusolsu.growthtools.api.event;

import dev.yakekusolsu.growthtools.api.ability.AbilityId;
import dev.yakekusolsu.growthtools.api.ability.AbilityResult;
import dev.yakekusolsu.growthtools.api.ability.AbilityTrigger;
import dev.yakekusolsu.growthtools.model.GrowthToolType;
import java.util.Objects;
import java.util.UUID;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

/** Fired synchronously after an ability produces a successful effect. */
public final class GrowthToolAbilityActivateEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();
    private final Player player;
    private final ItemStack tool;
    private final UUID toolId;
    private final AbilityId abilityId;
    private final AbilityResult result;
    private final GrowthToolType toolType;
    private final AbilityTrigger trigger;

    public GrowthToolAbilityActivateEvent(
            Player player, ItemStack tool, UUID toolId, GrowthToolType toolType,
            AbilityId abilityId, AbilityTrigger trigger, AbilityResult result) {
        this.player = Objects.requireNonNull(player, "player");
        this.tool = Objects.requireNonNull(tool, "tool");
        this.toolId = Objects.requireNonNull(toolId, "toolId");
        this.abilityId = Objects.requireNonNull(abilityId, "abilityId");
        this.toolType = Objects.requireNonNull(toolType, "toolType");
        this.trigger = Objects.requireNonNull(trigger, "trigger");
        this.result = Objects.requireNonNull(result, "result");
    }

    public Player getPlayer() { return player; }
    public ItemStack getTool() { return tool; }
    public UUID getToolId() { return toolId; }
    public AbilityId getAbilityId() { return abilityId; }
    public GrowthToolType getToolType() { return toolType; }
    public AbilityTrigger getTrigger() { return trigger; }
    public AbilityResult getResult() { return result; }

    @Override public @NotNull HandlerList getHandlers() { return HANDLERS; }
    public static @NotNull HandlerList getHandlerList() { return HANDLERS; }
}
