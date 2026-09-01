package dev.yakekusolsu.growthtools.api;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import org.bukkit.plugin.Plugin;

/** Static discovery point only; implementation services remain dependency-injected. */
public final class GrowthToolsProvider {
    private static final AtomicReference<Entry> CURRENT = new AtomicReference<>();

    private GrowthToolsProvider() { }

    /** Returns the active API or fails after/before the GrowthTools lifecycle. */
    public static GrowthToolsAPI get() {
        Entry entry = CURRENT.get();
        if (entry == null || !entry.owner().isEnabled()) {
            throw new IllegalStateException("GrowthTools API is not available");
        }
        return entry.api();
    }

    public static boolean isAvailable() {
        Entry entry = CURRENT.get();
        return entry != null && entry.owner().isEnabled();
    }

    /** Internal lifecycle hook guarded by the owning plugin identity. */
    static void register(Plugin owner, GrowthToolsAPI api) {
        requireGrowthTools(owner);
        Entry replacement = new Entry(owner, Objects.requireNonNull(api, "api"));
        if (!CURRENT.compareAndSet(null, replacement)) {
            throw new IllegalStateException("GrowthTools API is already registered");
        }
    }

    /** Internal lifecycle hook; stale facade instances are independently deactivated. */
    static void unregister(Plugin owner) {
        requireGrowthTools(owner);
        CURRENT.updateAndGet(entry -> entry != null && entry.owner() == owner ? null : entry);
    }

    private static void requireGrowthTools(Plugin owner) {
        Objects.requireNonNull(owner, "owner");
        if (!"GrowthTools".equals(owner.getName())) {
            throw new SecurityException("Only the GrowthTools plugin can manage its provider");
        }
    }

    private record Entry(Plugin owner, GrowthToolsAPI api) { }
}
