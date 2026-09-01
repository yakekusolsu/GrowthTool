package dev.yakekusolsu.growthtools.integration.worldguard;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.flags.Flags;
import dev.yakekusolsu.growthtools.integration.BlockProtectionGate;
import dev.yakekusolsu.growthtools.integration.IntegrationAdapter;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public final class WorldGuardProtectionBridge implements IntegrationAdapter, BlockProtectionGate {
    @Override public String id() { return "worldguard"; }
    @Override public String pluginName() { return "WorldGuard"; }
    @Override public void initialize() { WorldGuard.getInstance(); }
    @Override public boolean canBreak(Player player, Block block) {
        try {
            var query = WorldGuard.getInstance().getPlatform().getRegionContainer().createQuery();
            return query.testState(BukkitAdapter.adapt(block.getLocation()),
                    WorldGuardPlugin.inst().wrapPlayer(player), Flags.BLOCK_BREAK);
        } catch (RuntimeException | LinkageError exception) {
            return false;
        }
    }
}
