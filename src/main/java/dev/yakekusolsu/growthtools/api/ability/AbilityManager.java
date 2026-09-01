package dev.yakekusolsu.growthtools.api.ability;

import java.util.Collection;
import java.util.Optional;
import org.bukkit.plugin.Plugin;

/** Ability registration and metadata boundary. Registration is main-thread-only. */
public interface AbilityManager {
    /**
     * Registers an immutable definition and executor owned by an enabled plugin.
     * @throws SecurityException when the ability namespace is not owned by {@code owner}
     * @throws IllegalStateException off the main thread or after GrowthTools disable
     * @throws IllegalArgumentException for duplicate definitions/executors
     */
    AbilityRegistration register(
            Plugin owner, AbilityDefinition definition, AddonAbilityExecutor executor);
    /** Async-safe immutable registration metadata lookup. */
    Optional<AbilityRegistrationInfo> registration(AbilityId id);
    /** Async-safe immutable snapshot of all built-in and addon registrations. */
    Collection<AbilityRegistrationInfo> registrations();
}
