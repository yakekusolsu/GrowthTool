package dev.yakekusolsu.growthtools.integration.vault;

import dev.yakekusolsu.growthtools.integration.EconomyService;
import dev.yakekusolsu.growthtools.integration.IntegrationAdapter;
import java.util.UUID;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.Plugin;

public final class VaultEconomyBridge implements IntegrationAdapter, EconomyService {
    private final Plugin plugin;
    private Economy economy;
    public VaultEconomyBridge(Plugin plugin) { this.plugin = plugin; }
    @Override public String id() { return "vault"; }
    @Override public String pluginName() { return "Vault"; }
    @Override public void initialize() {
        var registration = plugin.getServer().getServicesManager().getRegistration(Economy.class);
        if (registration == null) throw new IllegalStateException("No Vault Economy provider");
        economy = registration.getProvider();
    }
    @Override public boolean available() { return economy != null; }
    @Override public double balance(UUID playerId) {
        if (economy == null) return 0;
        OfflinePlayer player = plugin.getServer().getOfflinePlayer(playerId);
        return economy.getBalance(player);
    }
    @Override public void close() { economy = null; }
}
