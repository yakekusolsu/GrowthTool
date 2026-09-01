# Phase 7 / 7.5 repository audit

Audit date: 2026-09-01. Scope: Java sources, resources, Gradle configuration, artifacts, tests, documentation, and the local Paper QA profile. `TESTED` means an action was actually executed; static inspection or compilation is never promoted to runtime-tested status.

## Findings

### CRITICAL

None found.

### HIGH — fixed

- Addon ability conditions/executors could propagate `RuntimeException` or `LinkageError` through the event path. `AbilityService` now logs the owning ability ID, returns `ERROR`, and continues dispatching other abilities. A regression test proves isolation.
- Diagnostics could read the mutable ability registry from the database completion thread. The main-thread-owned value is now captured before entering the asynchronous continuation.
- A SQLite operation failure after successful initialization left the runtime marked ready. Operation failures now enter degraded mode; a real second-connection write-lock test covers the transition.

### MEDIUM — fixed

- Provider registration lifecycle and integration snapshots leaked implementation responsibilities into the public artifact. Lifecycle access is now internal-only and concrete integration state moved outside `api`.
- Ability IDs, addon definitions, settings, and PlaceholderAPI input had insufficient size bounds. Explicit limits now prevent hostile allocation/input growth.
- The old Paper run task could no longer resolve current downloads. The wrapper/run plugin was updated and the manual target is now Paper 1.21.11.

### LOW / open validation gaps

- Reported Paper 1.21.11 human-player QA now covers the core gameplay, protection, reward-isolation, held-placeholder, and Bedrock scopes in `manual-qa-results.md`; unchecked edge matrices remain open.
- Paper 1.21.4 build 232 and 1.21.10 build 130 startup/shutdown plus a Paper 1.21.11 player runtime are tested for documented scopes. Other 1.21.x builds remain expected compatible, not tested.
- SQLite invalid-path and real write-lock degraded modes are automated. OS-level read-only behavior remains untested.
- Optional plugins were loaded together on Paper 1.21.10 where documented. Later player evidence is limited to the exact WorldGuard, PlaceholderAPI, mcMMO, Jobs, Geyser, and Floodgate scenarios in the compatibility matrix.

## Reviewed areas

Package boundaries, duplicated utilities, TODO/FIXME, apparently unused/dead classes, deprecated API warnings, Bukkit async access, SQL scheduling, reflection, class-loading hazards, dependency leakage, logs, exception/null handling, resources, and executor shutdown were inspected. No duplicate utility, TODO/FIXME debt, unnecessary reflection, blocking per-event SQL, resource leak, or compiler deprecation warning was found. SQL repositories use prepared statements for values; database work is serialized off the Paper main thread and shutdown waits up to five seconds before interruption.

The public API artifact contains only the API contracts plus `GrowthToolType`; it excludes internal implementations, storage/SQLite, built-in executors, plugin main, concrete integrations, and bundled dependencies. The full signature list is generated during verification and checked against the alpha baseline.
