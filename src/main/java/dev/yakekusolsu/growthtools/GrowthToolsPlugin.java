package dev.yakekusolsu.growthtools;

import dev.yakekusolsu.growthtools.command.GrowthToolsCommand;
import dev.yakekusolsu.growthtools.service.GrowthToolsRuntime;
import java.util.Objects;
import dev.yakekusolsu.growthtools.api.GrowthToolsProviderAccess;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public class GrowthToolsPlugin extends JavaPlugin {
    private GrowthToolsRuntime runtime;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        runtime = new GrowthToolsRuntime(this);
        GrowthToolsProviderAccess.register(this, runtime.api());
        registerCommands(runtime.command());
        runtime.listeners().forEach(listener ->
                getServer().getPluginManager().registerEvents(listener, this));
        getLogger().info("GrowthTools " + getPluginMeta().getVersion() + " enabled.");
    }

    @Override
    public void onDisable() {
        if (runtime != null) {
            GrowthToolsProviderAccess.unregister(this);
            runtime.close();
        }
        getLogger().info("GrowthTools disabled.");
    }

    private void registerCommands(GrowthToolsCommand executor) {
        PluginCommand command = Objects.requireNonNull(
                getCommand("growthtools"), "growthtools command is missing from plugin.yml");
        command.setExecutor(executor);
        command.setTabCompleter(executor);
    }
}
