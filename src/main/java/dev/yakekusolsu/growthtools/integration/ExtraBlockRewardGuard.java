package dev.yakekusolsu.growthtools.integration;

import org.bukkit.block.Block;

@FunctionalInterface
public interface ExtraBlockRewardGuard {
    void markExtraBlock(Block block);
}
