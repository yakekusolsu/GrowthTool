package dev.yakekusolsu.growthtools.api.internal;

import org.bukkit.Bukkit;

final class MainThreadGuard {
    private MainThreadGuard() { }
    static void check() {
        if (!Bukkit.isPrimaryThread()) {
            throw new IllegalStateException("This GrowthTools API method is MAIN THREAD ONLY");
        }
    }
}
