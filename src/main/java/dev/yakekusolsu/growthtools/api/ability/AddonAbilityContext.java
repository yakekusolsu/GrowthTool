package dev.yakekusolsu.growthtools.api.ability;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/** Public Paper execution view. The source block can be null for non-block triggers. */
public record AddonAbilityContext(
        AbilityContext domain, Player player, ItemStack tool, Block sourceBlock) { }
