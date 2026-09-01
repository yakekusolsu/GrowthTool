package dev.yakekusolsu.growthtools.integration;

import java.util.UUID;

@FunctionalInterface
public interface BedrockPlayerService {
    boolean isBedrockPlayer(UUID playerId);
}
