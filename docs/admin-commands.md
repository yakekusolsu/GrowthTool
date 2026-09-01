# Administrator Commands

## Inspection and diagnostics

- `/gt inspect` — Main-hand PDC plus registry status, owner, last seen, Material, creation time, and duplicate status. Permission: `growthtools.admin.inspect`.
- `/gt debug tool` — Expanded main-hand diagnostic output. Permission: `growthtools.admin.debug`.
- `/gt debug registry <uuid>` — Read one registry entry asynchronously. Permission: `growthtools.admin.debug`.
- `/gt debug database` — On-demand connection status, schema version, tool/duplicate/placed-block counts, and file size. Permission: `growthtools.admin.debug`.
- `/gt debug add-level <levels>` — Adds a positive number of exact levels to the main-hand GrowthTool, capped at the configured maximum. Gameplay EXP modifiers are bypassed, while PDC, lore, registry observation, EXP/level/unlock events, and an audit entry are updated normally. Permission: `growthtools.admin.debug`; player-only.
- `/gt ability list` — Registered IDs, unlock levels, and enabled state. Permission: `growthtools.admin.ability`.
- `/gt ability info <ability>` — Immutable definition details and settings. Permission: `growthtools.admin.ability`.
- `/gt ability debug` — Main-hand unlock, enabled, and remaining in-memory cooldown state. Permission: `growthtools.admin.ability`.
- `/gt integrations` — Optional-plugin state and version/detail. Permission: `growthtools.admin.integrations`.
- `/gt doctor` — PASS/WARN/FAIL summary for plugin/API/Paper/Java, config, DB/schema, PDC, placed-block tracker, abilities, and integrations. Permission: `growthtools.admin.doctor`.
- `/gt doctor export` — Writes the same privacy-safe report below `plugins/GrowthTools/diagnostics/`. It excludes player IPs, chat, world contents, and secrets.

## Safe repair

`/gt repair` revalidates the main-hand item, recalculates a valid cached level from total EXP, rewrites display lore, and queues a registry observation. Permission: `growthtools.admin.repair`.

It refuses unknown data versions, invalid UUIDs, negative/out-of-range EXP, incomplete PDC, and Material/type mismatch. Refusal does not modify the item.

## Duplicate UUID repair

`/gt regenerate-id` gives one safely readable main-hand GrowthTool a new random UUID. Permission: `growthtools.admin.regenerateid`.

The old registry row is retained as `REPLACED`, the replacement is registered, and old/new UUID, player, and timestamp are logged. GrowthTools never performs this action automatically after duplicate detection.

Back up the server and inspect all copies before regenerating an ID.
