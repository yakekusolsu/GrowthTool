package dev.yakekusolsu.growthtools.ability;

import dev.yakekusolsu.growthtools.api.ability.AbilityId;
import java.time.Clock;
import java.time.Duration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/** Per tool UUID and ability cooldowns with lazy expired-entry cleanup. */
public final class CooldownService {
    private static final int CLEANUP_INTERVAL = 256;
    private final Map<Key, Long> expiresAt = new HashMap<>();
    private final Clock clock;
    private int operations;

    public CooldownService() {
        this(Clock.systemUTC());
    }

    CooldownService(Clock clock) {
        this.clock = Objects.requireNonNull(clock, "clock");
    }

    public boolean isOnCooldown(UUID toolId, AbilityId abilityId) {
        cleanupSometimes();
        return expiresAt.getOrDefault(new Key(toolId, abilityId), 0L) > clock.millis();
    }

    public long remainingMillis(UUID toolId, AbilityId abilityId) {
        return Math.max(0L, expiresAt.getOrDefault(new Key(toolId, abilityId), 0L) - clock.millis());
    }

    public void start(UUID toolId, AbilityId abilityId, Duration duration) {
        Objects.requireNonNull(duration, "duration");
        if (duration.isNegative()) {
            throw new IllegalArgumentException("duration cannot be negative");
        }
        long now = clock.millis();
        long expiry;
        try {
            expiry = Math.addExact(now, duration.toMillis());
        } catch (ArithmeticException exception) {
            expiry = Long.MAX_VALUE;
        }
        if (expiry > now) {
            expiresAt.put(new Key(toolId, abilityId), expiry);
        }
    }

    public void clear(UUID toolId, AbilityId abilityId) {
        expiresAt.remove(new Key(toolId, abilityId));
    }

    private void cleanupSometimes() {
        if (++operations % CLEANUP_INTERVAL != 0) {
            return;
        }
        long now = clock.millis();
        Iterator<Long> iterator = expiresAt.values().iterator();
        while (iterator.hasNext()) {
            if (iterator.next() <= now) {
                iterator.remove();
            }
        }
    }

    private record Key(UUID toolId, AbilityId abilityId) {
        private Key {
            Objects.requireNonNull(toolId, "toolId");
            Objects.requireNonNull(abilityId, "abilityId");
        }
    }
}
