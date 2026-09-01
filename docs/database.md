# Database Operations

## Location and purpose

GrowthTools uses SQLite by default at `plugins/GrowthTools/growthtools.db`. The filename can be changed with `database.file`, but paths outside the plugin data directory are rejected.

SQLite stores registry/audit observations and persistent player-placed block coordinates. PDC remains the source of truth for the item carried between inventories or servers.

## Tables

- `schema_version`: one current schema version used by forward-only migrations.
- `growth_tools`: UUID, type, last level/EXP, PDC version, first/last observation, last owner, and registry status.
- `placed_blocks`: world UUID, exact and chunk coordinates, placement time, and placing player UUID.
- `audit_log`: important creation, duplicate, repair, replacement, and status operations.

Indexes cover registry status, last owner, and placed-block world/chunk coordinates. The current schema version is `1`.

## Threading and cache

All JDBC operations run on one dedicated `GrowthTools-Database` executor. Bukkit objects are read only on the Paper main thread and reduced to UUIDs, coordinates, timestamps, or domain records before submission.

Placed blocks are loaded into memory once after schema initialization. Placement, removal, and movement update memory first and enqueue a write. `BlockBreakEvent` never performs SQL. The cache spans loaded and unloaded worlds/chunks; no world scan is performed.

## Backup

1. Stop the server cleanly so the database executor drains.
2. Copy `growthtools.db` and, if present, its `-wal` and `-shm` files together.
3. Store the backup with the matching plugin version and configuration.

Do not copy only the main file while the server is actively writing.

## Recovery

If the database is unavailable or corrupt, GrowthTools keeps valid PDC-based item functionality where possible. Registry/debug persistence is unavailable and Block EXP remains disabled when placed-block protection cannot be trusted. Fix permissions, restore a consistent backup, or move the damaged files aside only after stopping the server. Never delete a database as an automatic migration strategy.

## Schema migration

`SchemaMigrationService` reads `schema_version` and applies contiguous, transactional forward migrations. A database newer than the plugin is rejected. Failed migrations roll back; existing tables are not dropped and recreated.
