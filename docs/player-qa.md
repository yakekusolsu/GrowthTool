# Player QA kit — 0.7.0-alpha.1

This is a 15–30 minute core pass for a disposable Paper server. Use Java 21, record exact Paper/plugin versions, empty the test player's inventory, and back up `plugins/GrowthTools/`. Stop Paper, replace the generated config with `testing-config.yml`, and restart. Never use production items or worlds. Write `PASS` or `FAIL` in the last column; preserve a sanitized log for every failure.

## Recorded Paper 1.21.11 player result

The final alpha review records the following real-player results as `PASS`. Scope is exact; blank rows in the reusable matrices below remain future QA work.

| Scenario | Result | Verified scope |
| --- | --- | --- |
| Core commands/item | PASS | Startup, `/gt doctor`, give, inspect, lore, UUID, level, and EXP display |
| Block EXP | PASS | Listed stone/ore/deepslate pickaxe targets, log family with axe, and DIRT/SAND-class shovel targets |
| Placed-block persistence | PASS | Player-placed ore gives no Growth EXP before or after restart |
| Vein Miner | PASS | Sneak, held-sneak consecutive breaks, additional blocks, recursion protection, and Block EXP interaction |
| Area Break | PASS | 3×3, held-sneak consecutive use, floor/wall/ceiling, Vein priority, and recursion protection |
| Auto Smelt | PASS | Normal, Vein additional, Area additional, mixed blocks, and no double drop |
| Fortune / Silk Touch | PASS | Fortune quantity retained; default Silk Touch bypasses smelting and drops the ore block |
| Fishing | PASS | Cast alone gives no EXP; completed catch gives EXP |
| Bow | PASS | Mob hit, miss, and post-shot held-item switch attribution |
| Duplicate/regenerate | PASS | Non-destructive duplicate detection and explicit UUID replacement states |
| WorldGuard | PASS | Normal, Vein additional, and Area additional `block-break deny` |
| PlaceholderAPI | PASS | Core held-tool level/EXP/type/UUID/max placeholders |
| mcMMO / Jobs | PASS | Reported normal/ability reward-isolation scenarios |
| Bedrock | PASS | GrowthTool, Block EXP, Vein, Area, fishing, and bow through Geyser/Floodgate |
| Extended edge-case rows below | MANUAL REQUIRED | Only rows fully represented above are promoted to PASS |

The QA curve uses one EXP per level step and maximum level 105. Total EXP needed to start level `n` is `(n-1)×n/2`. Ability unlocks are levels 2/3/4/5. Expected base gains are DIRT 1, STONE 1000, COAL_ORE 3, IRON_ORE 5, DEEPSLATE_IRON_ORE 5, COPPER_ORE 3, GOLD_ORE 7, DIAMOND_ORE 20, DEEPSLATE_DIAMOND_ORE 25, fishing 5, and bow hit 3. At level 5+, Experience Boost applies `floor(base × 1.25)` once.

## Fast core pass

| Test | Command | Setup | Action | Expected result | PASS/FAIL |
| --- | --- | --- | --- | --- | --- |
| Create | `/gt give <player> pickaxe` | Survival, empty main hand | Hold the given pickaxe | One item; lore shows level 1 and EXP 0 | |
| Inspect | `/gt inspect`; `/gt debug tool` | Hold that item | Run both commands | Same valid UUID/type, level 1, EXP 0, PDC version 1; Registry ACTIVE | |
| Debug level | `/gt debug add-level 4` | OP, hold the test GrowthTool at level 1 | Run once, then inspect | Level 5 with exact threshold EXP; ordered level/unlock messages; no Experience Boost overshoot | |
| Level 1→2 | `/gt inspect` | Place natural DIRT before testing or use generated terrain | Break one DIRT | +1 EXP, level 2, Vein Miner unlock/lore; no duplicate gain | |
| Multi-level | `/gt inspect` | Natural IRON_ORE isolated from other ore | Break it with pickaxe | +5 base EXP; total 6, reaches level 4; unlock messages are ordered once | |
| Boost floor | `/gt inspect` | Reach level 5; isolated DIRT then IRON_ORE | Break DIRT, inspect; break iron, inspect | DIRT grants `floor(1.25)=1`; iron grants `floor(6.25)=6` | |
| Fast level 100/max | `/gt inspect` | Reach level 5 | Break isolated natural STONE blocks, inspecting after each | Each gain is 1250; level reaches 100 quickly, caps at 105, further EXP does not increase | |
| Creative gate | `/gamemode creative`; `/gamemode survival` | Record EXP | Break configured natural block in Creative, then comparable block in Survival | Creative +0; Survival configured/boosted amount | |
| Placed block | none | Place STONE yourself in Survival | Break it before and after a full restart | +0 both times; doctor says placed-block tracker ready | |
| Diagnostics | `/gt doctor`; `/gt integrations`; `/gt debug database` | OP | Run commands | All core checks PASS; plausible schema/counts; no stack trace | |

## Block EXP regression recheck

Use naturally generated blocks in a disposable world. Do not hand-place the test ores: player-placed blocks intentionally award zero EXP. Temporarily set `debug: true` only while diagnosing, run `/gt reload`, and disable it again after the matrix.

| Test | Command | Setup | Action | Expected result | PASS/FAIL |
| --- | --- | --- | --- | --- | --- |
| Pickaxe matrix | `/gt inspect` | Isolated natural COAL_ORE, COPPER_ORE, REDSTONE_ORE, LAPIS_ORE, EMERALD_ORE, NETHER_QUARTZ_ORE, ANCIENT_DEBRIS, and OBSIDIAN | Break each with the same GrowthTool pickaxe | Each positive configured value is added; no `incompatible-tool` rejection | |
| Wrong tool | `/gt inspect` | Natural OAK_LOG, DIAMOND_ORE, and REDSTONE_ORE | Break log with pickaxe, diamond with axe, and redstone with shovel | All add 0 EXP; debug reason is `incompatible-tool` | |
| Correct axe/shovel | `/gt inspect` | Natural OAK_LOG, DIRT, and SAND | Break with matching GrowthTool | Each adds its configured EXP | |
| Hoe/crop maturity | `/gt inspect` | Mature and immature WHEAT plus natural OAK_LEAVES | Break with a GrowthTool hoe | Mature WHEAT and leaves add configured EXP; immature WHEAT adds 0 | |
| Placed protection | none | Hand-place one configured ore | Break it with matching GrowthTool | Adds 0 EXP; debug reason is `player-placed` | |

## Vein Miner

| Test | Command | Setup | Action | Expected result | PASS/FAIL |
| --- | --- | --- | --- | --- | --- |
| Unlock | `/gt ability debug` | Level 1, then level 2 pickaxe | Compare output | Locked at 1, unlocked at 2 | |
| Held sneak repetition | none | `require-sneak: true`, cooldown 0, two separated ore veins | Hold sneak continuously; break one origin in each vein | Vein Miner activates on both player breaks without releasing sneak; no mining occurs between breaks | |
| Sneak disabled | none | Set `require-sneak: false`, reload, separated ore vein | Break origin without sneaking | Vein Miner activates; restoring true makes the same non-sneaking action ineligible | |
| Optional cooldown | `/gt ability debug` | Set cooldown to 1 second, reload, two veins | Break both origins inside one second | First activates; second normal block breaks but its vein is not expanded until cooldown expires | |
| Bounded vein | `/gt inspect` | 16 connected natural IRON_ORE; separately 17+ | Break origin | At most 15 extra blocks after origin (16 total traversal); no recursive second wave | |
| Variants/diagonal | none | IRON_ORE joined diagonally to DEEPSLATE_IRON_ORE | Break origin | Diagonal and configured normal/deepslate group are followed | |
| Extra EXP | `/gt inspect` | Level 2–4, 5 extra IRON_ORE | Break origin | Origin gives 5; each successful extra gives `floor(5×0.25)=1`; inspect matches actual affected count | |
| Safety | none | Player-placed ore, one ore over unloaded chunk edge, protected ore | Break origin | Placed/protected/unloaded targets remain; no chunk is loaded; only broken blocks cost durability | |
| Durability | none | Compare plain and Unbreaking pickaxes | Break controlled veins | Normal Minecraft durability probability; operation stops immediately if tool breaks | |

## Area Break

| Test | Command | Setup | Action | Expected result | PASS/FAIL |
| --- | --- | --- | --- | --- | --- |
| Held sneak repetition | none | `require-sneak: true`, cooldown 0, two separated compatible 3×3 surfaces | Hold sneak continuously; break one origin on each surface | Area Break activates on both player breaks without releasing sneak; no mining occurs between breaks | |
| Sneak disabled | none | Set `require-sneak: false`, reload, compatible 3×3 surface | Break origin without sneaking | Area Break activates; restoring true makes the same non-sneaking action ineligible | |
| Optional cooldown | `/gt ability debug` | Set cooldown to 3 seconds, reload, two surfaces | Break both origins inside three seconds | First activates; second origin breaks normally but its area is not expanded until cooldown expires | |
| Ability priority | none | Both abilities unlocked; configured ore vein, then 3×3 STONE | Hold sneak and break each origin | Ore runs Vein Miner only; STONE runs Area Break only; neither origin starts both abilities | |
| Planes | `/gt ability debug` | Level 4+, 3×3 natural compatible blocks as floor/wall/ceiling | Break center while targeting each face | Correct 3×3 plane; origin is not processed twice | |
| Safety/durability | none | Include player-placed, protected, bedrock, chunk-edge blocks and nearly broken tool | Break center | Unsafe targets remain; one durability attempt per successful extra; stop when tool breaks | |

## Auto Smelt

| Test | Command | Setup | Action | Expected result | PASS/FAIL |
| --- | --- | --- | --- | --- | --- |
| Ores | none | Level 3+ pickaxe; isolated iron, gold, copper ore | Break each | Raw drop is replaced once by equal-count iron/gold/copper ingots; no raw+ingot double drop | |
| Silk Touch | none | Silk Touch GrowthTool | Break each ore | Normal ore block drop remains; no smelt transformation | |
| Fortune | none | Fortune GrowthTool | Break controlled ore repeatedly | Whatever raw count Minecraft produces becomes the same ingot count; no multiplication beyond Fortune | |
| Vein composition | none | Vein Miner and Auto Smelt unlocked; connected natural IRON_ORE | Hold sneak and break the origin | Origin and every successfully broken additional ore drop ingots only; no raw+ingot pair; parent extra EXP remains 25% | |
| Area composition | none | Area Break and Auto Smelt unlocked; mixed 3×3 containing smeltable ore and STONE | Hold sneak and break a non-ore origin | Successfully broken ore drops are smelted; STONE remains its normal drop; no second multi-block wave | |
| Additional Silk/Fortune | none | Repeat Vein composition with Silk Touch, then Fortune | Break controlled veins | Silk Touch prevents additional Auto Smelt; Fortune-adjusted raw count becomes the same ingot count | |
| Additional protection | none | Include protected and player-placed ore in the additional target set | Break origin | Rejected ore remains intact and produces no raw or smelted drop | |

## Fishing and bow

| Test | Command | Setup | Action | Expected result | PASS/FAIL |
| --- | --- | --- | --- | --- | --- |
| Fishing | `/gt give <player> fishing_rod`; `/gt inspect` | Test cast, fish/item, entity | Perform each | Cast-only +0; completed catch base 5 (boosted only after unlock) exactly once | |
| Rod attribution | `/gt inspect` | Cast, switch slot or remove rod | Complete catch | Switched rod receives EXP if still in inventory; removed rod receives none; cancelled event +0 | |
| Bow | `/gt give <player> bow`; `/gt inspect` | Shoot miss, mob hit, zero/cancelled damage | Perform each | Miss/zero/cancelled +0; successful damage base 3 exactly once | |
| Bow attribution | `/gt inspect` | Shoot, switch slot/remove bow before impact | Observe original item | Inventory-retained original gets EXP; removed bow gets none; same projectile cannot award twice | |

## Duplicate and repair — disposable items only

1. Back up and empty the test inventory. Create one GrowthTool, open Creative inventory, and middle-click the item to clone its exact PDC into a separate slot. Return to Survival and inspect/pick up both copies. Expected: warning, Registry `DUPLICATE`, neither item deleted.
2. Keep one copy, run `/gt regenerate-id`, then `/gt debug registry <old-uuid>` and inspect the new item. Expected: old row `REPLACED`, new UUID different and `ACTIVE`.
3. For repair tests, use only a disposable server copy and a trusted NBT/PDC inspection tool. Save three backups: remove lore only; change the cached level while retaining valid EXP; delete only the registry DB row. `/gt repair` must restore display/cache/registration without changing valid UUID/EXP.
4. Separately corrupt UUID, set an unknown data version, or mismatch material/type. `/gt repair` must refuse each and leave the item unchanged. Do not attempt this on a real player item.

## External integrations

- WorldGuard: create a small region and set `block-break deny`; test ordinary, Vein, Area, and Auto Smelt origins/extra blocks. No protected block or transformed bypass is allowed.
- PlaceholderAPI: while holding no tool and then a GrowthTool, parse `%growthtools_level%`, `%growthtools_exp%`, `%growthtools_type%`, `%growthtools_uuid%`, `%growthtools_max_level%`, and `%growthtools_is_max_level%`. No-tool values are empty.
- Vault: test no provider and, if available, a provider; core must remain enabled.
- mcMMO/Jobs: record numeric reward before/after one origin and controlled extra blocks. With extra rewards disabled, synthetic blocks add zero external reward.
- Geyser/Floodgate: server-side load is insufficient. Join with a Bedrock client and repeat create, block EXP, automatic ability, fishing, and bow checks.

After testing, restore the original config and `plugins/GrowthTools/` backup, remove QA worlds/databases, and restart. Transfer results to `manual-qa-results.md`.
