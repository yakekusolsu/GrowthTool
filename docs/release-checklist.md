# Release checklist — 0.7.0-alpha.1

## Automated release gates

- [x] Java 21 `releaseBuild --warning-mode all`
- [x] Unit and MockBukkit integration tests
- [x] API signature baseline
- [x] API Javadocs, plugin/API/source artifacts, and JAR boundary audit
- [x] SQLite invalid-path and real write-lock degradation tests
- [x] Version remains `0.7.0-alpha.1`
- [x] MIT and bundled-dependency license review
- [x] Secrets, personal-path, and tracked-file dry-run review

## Reported Paper 1.21.11 player QA

- [x] Core startup, `/gt doctor`, `/gt give`, `/gt inspect`, lore/UUID/level/EXP
- [x] Survival Block EXP with reported pickaxe, axe, and shovel targets
- [x] Persistent placed-block protection across restart
- [x] Vein Miner held-sneak consecutive activation, additional breaks, EXP interaction, and recursion prevention
- [x] Area Break held-sneak consecutive 3×3, floor/wall/ceiling, Vein priority, and recursion prevention
- [x] Auto Smelt on normal, Vein additional, Area additional, and mixed targets
- [x] Fortune quantity preservation, Silk Touch policy, and no double drop
- [x] Fishing core cast/catch behavior
- [x] Bow hit/miss and post-shot item-switch attribution
- [x] Non-destructive duplicate detection and explicit regenerate ID state transition
- [x] WorldGuard normal/Vein/Area deny-region protection
- [x] PlaceholderAPI core held-tool values
- [x] mcMMO normal/Vein reward-isolation scope
- [x] Jobs normal/Vein/Area reward isolation with extra rewards disabled
- [x] Real Bedrock gameplay through Geyser/Floodgate

## Remaining validation gaps — not current alpha blockers

- [ ] Exhaustive player matrix beyond the reported scopes in `manual-qa.md`
- [ ] Vault with an economy provider
- [ ] OS-level read-only SQLite file
- [ ] mcMMO Area Break reward path
- [ ] PlaceholderAPI no-tool and every ability placeholder
- [ ] Player-triggered addon failure fixture
- [ ] Full malformed PDC/config, cancellation, durability, and chunk-boundary matrices

## Repository/publication state

- [x] README, Japanese README, changelog, compatibility, QA, known-issues, privacy, and release documentation audited
- [x] Git tracking dry run includes source/docs/examples/API baseline and excludes generated/local runtime data
- [ ] Clean, fully tracked repository — initial `git add`/commit intentionally not performed yet
- [ ] GitHub Release, Modrinth, Hangar, or other publication — intentionally not performed yet

The unchecked validation gaps above are documented alpha limitations, not observed critical failures. Reclassify the release as blocked only if a server crash/startup failure, data loss, duplication exploit, protection bypass, severe database corruption, fatal API lifecycle error, or major unusable gameplay regression is discovered.
