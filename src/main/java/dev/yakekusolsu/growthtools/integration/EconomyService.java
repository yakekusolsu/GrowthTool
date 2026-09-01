package dev.yakekusolsu.growthtools.integration;

import java.util.UUID;

public interface EconomyService {
    boolean available();
    double balance(UUID playerId);
}
