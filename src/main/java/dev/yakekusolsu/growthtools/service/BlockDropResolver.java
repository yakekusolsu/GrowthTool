package dev.yakekusolsu.growthtools.service;

import java.util.Collection;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/** Computes the natural, enchantment-adjusted drops before transformations. */
@FunctionalInterface
public interface BlockDropResolver {
    Collection<ItemStack> resolve(Block block, ItemStack tool, Player player);
}
