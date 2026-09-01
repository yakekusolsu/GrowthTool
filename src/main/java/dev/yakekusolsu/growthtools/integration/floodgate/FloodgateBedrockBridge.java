package dev.yakekusolsu.growthtools.integration.floodgate;

import dev.yakekusolsu.growthtools.integration.BedrockPlayerService;
import dev.yakekusolsu.growthtools.integration.IntegrationAdapter;
import java.util.UUID;
import org.geysermc.floodgate.api.FloodgateApi;

public final class FloodgateBedrockBridge implements IntegrationAdapter, BedrockPlayerService {
    private FloodgateApi api;
    @Override public String id() { return "floodgate"; }
    @Override public String pluginName() { return "floodgate"; }
    @Override public void initialize() { api = FloodgateApi.getInstance(); }
    @Override public boolean isBedrockPlayer(UUID playerId) {
        return api != null && api.isFloodgatePlayer(playerId);
    }
    @Override public void close() { api = null; }
}
