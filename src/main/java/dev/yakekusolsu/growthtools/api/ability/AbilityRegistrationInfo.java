package dev.yakekusolsu.growthtools.api.ability;

import java.time.Instant;

public record AbilityRegistrationInfo(
        AbilityId id, String ownerPlugin, Instant registeredAt, AbilityRegistrationStatus status) { }
