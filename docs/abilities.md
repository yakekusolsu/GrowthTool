# Abilities

Phase 5 provides four automatically enabled built-in abilities. Unlock state is derived from the GrowthTool type and level; lore is display-only. Cooldowns are held in memory per `tool UUID + ability ID` and reset on server restart.

| Ability ID | Tools | Default unlock | Cooldown | Effect |
| --- | --- | ---: | ---: | --- |
| `growthtools:vein_miner` | Pickaxe | 25 | 0 s | Bounded breadth-first search through the configured ore group. |
| `growthtools:auto_smelt` | Pickaxe | 50 | 0 s | Replaces eligible drops using a furnace recipe. |
| `growthtools:area_break` | Pickaxe, shovel | 75 | 0 s | Breaks a 3×3 plane perpendicular to the player's dominant view direction. |
| `growthtools:experience_boost` | All GrowthTools | 100 | 0 s | Multiplies Growth EXP by 1.25. |

The bundled `config.yml` contains the complete editable defaults. The main shape is:

```yaml
abilities:
  creative-mode: false
  lore: { enabled: true, maximum-entries: 8 }
  vein-miner:
    enabled: true
    unlock-level: 25
    cooldown-seconds: 0.0
    require-sneak: true
    max-blocks: 16
    diagonal: true
    extra-block-exp-multiplier: 0.25
    ore-groups: { diamond: [DIAMOND_ORE, DEEPSLATE_DIAMOND_ORE] }
  auto-smelt:
    enabled: true
    unlock-level: 50
    disable-with-silk-touch: true
  area-break:
    enabled: true
    unlock-level: 75
    cooldown-seconds: 0.0
    require-sneak: true
    radius: 1
    extra-block-exp-multiplier: 0.25
  experience-boost:
    enabled: true
    unlock-level: 100
    multiplier: 1.25
```

With `require-sneak: true`, Vein Miner and Area Break evaluate `player.isSneaking()` independently for every normal player `BlockBreakEvent`. Holding sneak and breaking another block therefore triggers a new eligibility check without requiring the player to release or press sneak again. Neither ability mines on sneak input alone or continues mining without another player-originated block break. Set an ability's `require-sneak` to false to allow its ordinary matching block breaks to trigger it.

The bundled Vein Miner and Area Break cooldowns are zero because normal player block breaking supplies the activation rate limit. A non-zero `cooldown-seconds` remains supported and deliberately suppresses successful reactivation until it expires. Existing custom configs are not overwritten; servers retaining Vein Miner's former `1.0` or Area Break's former `3.0` must change those values to `0.0` manually for consecutive fast mining.

One player-originated block break selects at most one built-in multi-block mining ability. A Pickaxe break whose origin belongs to a configured Vein Miner ore group selects Vein Miner when it is enabled and unlocked; Area Break is excluded for that event. Other Pickaxe and Shovel origins exclude Vein Miner and may select Area Break. A selected ability that fails its sneak or cooldown condition does not fall through to the other built-in ability. Addon-owned block-break abilities remain independently eligible.

## Safety and interaction rules

- Vein Miner uses a non-recursive BFS and is hard-limited to 128 visited blocks. Its default limit is 16, including the original block.
- Area Break has a hard maximum radius of 3. The original block remains under vanilla handling.
- Neither block ability loads chunks. Air, incompatible, player-placed, or protected blocks are skipped.
- Every additional block receives a cancellable `BlockBreakEvent`; a cancellation prevents that individual break.
- Each successful extra break damages the item through the Bukkit item-damage API, including Unbreaking handling. Processing stops if the tool breaks.
- Plugin-originated breaks use a scoped execution marker, preventing recursive ability and ordinary block-EXP processing.
- The built-in Vein Miner and Area Break are mutually exclusive for each origin event; configured ores prefer Vein Miner and other compatible terrain prefers Area Break.
- Creative ability activation is disabled by default and controlled separately by `abilities.creative-mode`.
- Extra-block Growth EXP is `floor(base EXP × multiplier)`, never below zero. Defaults to 25%.
- Normal and Vein/Area additional drops pass through the same ordered drop-transformation pipeline. Auto Smelt replaces a computed drop before its final spawn and never emits a second raw drop. Silk Touch disables it by default. Fortune runs while computing the input drops, so its adjusted count is retained. Drops without a furnace recipe are unchanged.
- Auto Smelt on additional blocks is a drop transformation inside the parent Vein Miner or Area Break operation, not another block ability trigger. The parent operation emits its normal activation event; Auto Smelt does not emit one public activation event per additional block.
- Experience Boost runs in the centralized modifier pipeline before `ExperienceService`; invalid or overflowing values are rejected or saturated safely.

For each accepted additional block, processing is ordered as protection and placed-block validation, cancellable break validation, natural drop computation, ordered drop transformations, final drop spawn, and durability. The parent ability then accounts its configured extra-block EXP from successful breaks. Rejected blocks never enter the drop pipeline.

See [`configuration.md`](configuration.md) for all keys. `/gt ability list`, `/gt ability info <id>`, and `/gt ability debug` require `growthtools.admin.ability`.
