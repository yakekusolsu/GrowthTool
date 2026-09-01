package dev.yakekusolsu.growthtools.storage.database;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/** Serializes SQLite work away from Paper's main thread. */
public final class DatabaseExecutor implements AutoCloseable {
    private final ExecutorService executor = Executors.newSingleThreadExecutor(runnable -> {
        Thread thread = new Thread(runnable, "GrowthTools-Database");
        thread.setDaemon(true);
        return thread;
    });

    public CompletableFuture<Void> run(Runnable operation) {
        return CompletableFuture.runAsync(operation, executor);
    }

    public <T> CompletableFuture<T> supply(Supplier<T> operation) {
        return CompletableFuture.supplyAsync(operation, executor);
    }

    @Override
    public void close() {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException exception) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
