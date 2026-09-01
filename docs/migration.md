# Alpha migration guide

1. Stop Paper and back up the entire `plugins/GrowthTools/` directory. Keep a world/player-data backup because GrowthTool PDC is stored on items.
2. Save the old plugin JAR, config, messages, database, and the output of `/gt doctor export`.
3. Read `CHANGELOG.md` and `known-issues.md`, then replace only the plugin JAR.
4. Start the server. Do not copy a new default config over local customizations.
5. Check startup migration/validation messages, then run `/gt version`, `/gt doctor`, and `/gt debug database`.
6. Inspect representative tools and a known placed block before reopening the server to players.

0.7.0-alpha.1 keeps config version 1, PDC data version 1, and SQLite schema version 1. Forward migration foundations exist, but downgrade migrations are unsupported. If validation fails, stop the server and restore the complete backup rather than editing PDC or SQLite manually.
