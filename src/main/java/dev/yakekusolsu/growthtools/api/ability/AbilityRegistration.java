package dev.yakekusolsu.growthtools.api.ability;

import org.bukkit.plugin.Plugin;

/** Idempotent ownership handle. Unregister must be called on the main thread. */
public interface AbilityRegistration {
    AbilityId id();
    Plugin owner();
    boolean isRegistered();
    /** Returns true only for the first successful unregister; later calls return false. */
    boolean unregister();
}
