package dev.yakekusolsu.growthtools.integration;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;

@FunctionalInterface
public interface BlockProtectionGate {
    boolean canBreak(Player player, Block block);
}
