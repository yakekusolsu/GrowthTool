# Known issues and validation limits

## Open limits

- 0.7.0-alpha.1 is pre-release software. Back up worlds and `plugins/GrowthTools/` before installation or update; the experimental API may change during 0.x with documented notice.
- Vault without an economy provider is intentionally isolated as an integration `ERROR`; GrowthTools remains enabled. Runtime QA with a real economy provider is `NOT TESTED`. Vault currently exposes an internal abstraction and GrowthTools does not charge players.
- SQLite invalid-path and real write-lock degradation are automated. An OS-level read-only database scenario is `NOT TESTED` on this Windows workspace.
- After a real SQLite operation failure, GrowthTools enters degraded mode. PDC inspection remains available, while database-backed behavior and Block EXP stay disabled until a clean restart.
- Paper 1.21.4 build 232, 1.21.10 build 130, and a reported 1.21.11 player runtime are tested only for their documented scenarios. Other 1.21.x builds are expected compatible; versions outside 1.21.x are unsupported.
- WorldEdit 7.3.9 previously reported limited Paper/Minecraft 1.21.10 support. WorldGuard deny-region player QA nevertheless passed for normal, Vein additional, and Area additional breaks on the reported Paper 1.21.11 runtime.
- Jobs artifact 5.2.6.2 declares runtime version 5.2.6.1. Reward-isolation QA passed, but the upstream version-metadata mismatch remains visible.
- Full edge matrices remain incomplete for Experience Boost limits, malformed PDC, OS read-only SQLite, player-triggered addon failure fixtures, mcMMO Area rewards, Vault provider-present behavior, and several cancellation/durability/chunk-boundary cases. These are `MANUAL REQUIRED` or `NOT TESTED`, not known failures.
- Plugin hot-reloaders are unsupported. Restart Paper when replacing GrowthTools or integration JARs.
- Skill trees, GUI selection, persistent ability toggles, MySQL, Redis, and cross-server synchronization are not implemented.

## Resolved before alpha review

- Limited Block EXP compatibility was replaced by tag-first, naming-group, and compact fallback resolution; representative pickaxe/axe/shovel materials passed real Paper 1.21.11 player QA.
- Vein Miner and Area Break now evaluate held sneak for every player-originated BlockBreak and bundle zero-second cooldowns; consecutive held-sneak activation passed player QA.
- Vein Miner and Area Break are mutually exclusive per origin; ore priority and recursion prevention passed player QA.
- Auto Smelt now transforms successful Vein and Area additional-block drops through the shared drop pipeline. Mixed drops, Fortune quantity, Silk Touch, and no-double-drop behavior passed player QA.
- Persistent placed-block tracking passed a restart test, and duplicate UUID detection plus explicit ID regeneration passed without automatic item deletion.
- No WorldGuard bypass was observed for normal, Vein additional, or Area additional breaks in the reported deny-region QA.

No unresolved server crash, startup failure, item-loss path, duplication exploit, protection bypass, severe database corruption, fatal API lifecycle failure, or major unusable core-gameplay issue is known from the completed review.
