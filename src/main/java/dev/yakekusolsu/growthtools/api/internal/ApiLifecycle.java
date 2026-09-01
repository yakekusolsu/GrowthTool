package dev.yakekusolsu.growthtools.api.internal;

import java.util.concurrent.atomic.AtomicBoolean;

public final class ApiLifecycle {
    private final AtomicBoolean active = new AtomicBoolean(true);
    public void ensureActive() {
        if (!active.get()) throw new IllegalStateException("GrowthTools API instance is no longer active");
    }
    public void deactivate() { active.set(false); }
}
