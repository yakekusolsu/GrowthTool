package dev.yakekusolsu.growthtools.model;

/** On-demand SQLite statistics for the administrator debug command. */
public record DatabaseStats(
        boolean connected,
        int schemaVersion,
        long registeredTools,
        long duplicateTools,
        long trackedPlacedBlocks,
        long databaseFileSize) {}
