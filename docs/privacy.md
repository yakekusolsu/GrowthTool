# Privacy and data storage

GrowthTools has no telemetry, analytics, update checker, or intentional external network communication at runtime. Gradle and the `runServer` development task access dependency/Paper repositories only during development.

The plugin stores:

- GrowthTool UUID, type, level, EXP, schema version, and creation timestamp in item PDC
- Tool UUID, observed owner/player UUID, status, and last-seen/audit metadata in SQLite
- Player UUID, world UUID, block coordinates, and placement time for placed-block protection in SQLite
- Administrator diagnostics containing versions, health states, counts, schema versions, and file size

It does not intentionally store IP addresses, chat content, passwords, tokens, economy balances, or server credentials. Diagnostic exports use a controlled timestamp filename and do not include player UUIDs or coordinates. Server administrators remain responsible for securing backups, logs, worlds, and the plugin data directory.
