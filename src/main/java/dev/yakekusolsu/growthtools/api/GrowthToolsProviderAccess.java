package dev.yakekusolsu.growthtools.api;

import org.bukkit.plugin.Plugin;

/** Plugin implementation lifecycle access; deliberately excluded from the API artifact. */
public final class GrowthToolsProviderAccess {
    private GrowthToolsProviderAccess() { }

    public static void register(Plugin owner, GrowthToolsAPI api) {
        GrowthToolsProvider.register(owner, api);
    }

    public static void unregister(Plugin owner) {
        GrowthToolsProvider.unregister(owner);
    }
}
