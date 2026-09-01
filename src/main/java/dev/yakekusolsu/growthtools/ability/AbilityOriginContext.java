package dev.yakekusolsu.growthtools.ability;

/** Scoped recursion guard for plugin-generated block break events. */
public final class AbilityOriginContext {
    private static final ThreadLocal<Integer> DEPTH = ThreadLocal.withInitial(() -> 0);

    private AbilityOriginContext() {
    }

    public static boolean isAbilityOrigin() {
        return DEPTH.get() > 0;
    }

    public static Scope enter() {
        DEPTH.set(DEPTH.get() + 1);
        return new Scope();
    }

    public static final class Scope implements AutoCloseable {
        private boolean closed;

        private Scope() {
        }

        @Override
        public void close() {
            if (closed) {
                return;
            }
            closed = true;
            int remaining = DEPTH.get() - 1;
            if (remaining <= 0) {
                DEPTH.remove();
            } else {
                DEPTH.set(remaining);
            }
        }
    }
}
