package dev.yakekusolsu.growthtools.ability;

import static org.junit.jupiter.api.Assertions.*;

import dev.yakekusolsu.growthtools.api.ability.AbilityId;
import java.time.*;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class CooldownServiceTest {
    @Test void zeroDurationAllowsImmediateConsecutiveUse() {
        CooldownService service = new CooldownService(new MutableClock());
        UUID tool = UUID.randomUUID();
        AbilityId ability = AbilityId.parse("test:zero");

        service.start(tool, ability, Duration.ZERO);

        assertFalse(service.isOnCooldown(tool, ability));
    }

    @Test void keysCooldownByToolAndAbilityAndExpires() {
        MutableClock clock = new MutableClock();
        CooldownService service = new CooldownService(clock);
        UUID tool = UUID.randomUUID();
        AbilityId ability = AbilityId.parse("test:one");
        service.start(tool, ability, Duration.ofSeconds(1));
        assertTrue(service.isOnCooldown(tool, ability));
        assertFalse(service.isOnCooldown(UUID.randomUUID(), ability));
        clock.millis += 1000;
        assertFalse(service.isOnCooldown(tool, ability));
    }

    private static final class MutableClock extends Clock {
        private long millis = 1_000;
        @Override public ZoneId getZone() { return ZoneOffset.UTC; }
        @Override public Clock withZone(ZoneId zone) { return this; }
        @Override public Instant instant() { return Instant.ofEpochMilli(millis); }
        @Override public long millis() { return millis; }
    }
}
