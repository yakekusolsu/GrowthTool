package dev.yakekusolsu.growthtools.ability;

public record BlockPosition(int x, int y, int z) {
    public BlockPosition offset(int xOffset, int yOffset, int zOffset) {
        return new BlockPosition(x + xOffset, y + yOffset, z + zOffset);
    }
}
