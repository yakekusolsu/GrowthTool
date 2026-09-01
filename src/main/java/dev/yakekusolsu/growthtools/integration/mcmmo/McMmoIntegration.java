package dev.yakekusolsu.growthtools.integration.mcmmo;

import com.gmail.nossr50.util.BlockUtils;
import dev.yakekusolsu.growthtools.integration.ExtraBlockRewardGuard;
import dev.yakekusolsu.growthtools.integration.IntegrationAdapter;
import org.bukkit.block.Block;

/** Uses mcMMO's public unnatural-block tracker so synthetic breaks grant no gathering XP. */
public final class McMmoIntegration implements IntegrationAdapter, ExtraBlockRewardGuard {
    private final boolean rewardExtraBlocks;

    public McMmoIntegration(boolean rewardExtraBlocks) {
        this.rewardExtraBlocks = rewardExtraBlocks;
    }

    @Override public String id() { return "mcmmo"; }
    @Override public String pluginName() { return "mcMMO"; }
    @Override public void initialize() { }
    @Override public void markExtraBlock(Block block) {
        if (!rewardExtraBlocks) BlockUtils.setUnnaturalBlock(block);
    }
}
