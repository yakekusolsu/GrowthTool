package dev.yakekusolsu.growthtools.ability;

import dev.yakekusolsu.growthtools.api.ability.AbilityContext;
import java.util.Objects;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/** Paper-only objects paired with a portable AbilityContext. */
public record PaperAbilityExecutionContext(
        AbilityContext domain, Player player, ItemStack tool, Block origin, Object payload) {
    public PaperAbilityExecutionContext {
        Objects.requireNonNull(domain, "domain");
        Objects.requireNonNull(player, "player");
        Objects.requireNonNull(tool, "tool");
        Objects.requireNonNull(origin, "origin");
    }

    public PaperAbilityExecutionContext(
            AbilityContext domain, Player player, ItemStack tool, Block origin) {
        this(domain, player, tool, origin, null);
    }
}
