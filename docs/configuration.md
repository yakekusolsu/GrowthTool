# Configuration

GrowthTools loads `config.yml` into immutable runtime snapshots. `/gt reload` validates and replaces level, EXP, and ability snapshots; gameplay events do not read YAML.

All four ability sections support `enabled`, `unlock-level`, and `cooldown-seconds`. Vein Miner also supports `require-sneak`, `max-blocks`, `diagonal`, `extra-block-exp-multiplier`, and configurable `ore-groups`. Area Break supports `require-sneak`, `radius`, and `extra-block-exp-multiplier`. Both mining abilities bundle `require-sneak: true` and `cooldown-seconds: 0.0`; zero is valid, while negative or non-finite cooldowns are rejected. Auto Smelt supports `disable-with-silk-touch`. Experience Boost supports `multiplier`.

Sneak state is sampled on each player-originated block break. Sneaking alone never starts or continues mining. When both built-ins are unlocked, an origin in a configured Vein Miner ore group selects Vein Miner only; other Pickaxe or Shovel origins select Area Break only. This prevents one origin event from causing both multi-block abilities.

When Auto Smelt is enabled and unlocked for the GrowthTool pickaxe, Vein Miner and Area Break additional drops use the same furnace-recipe transformation as the origin drop. Fortune-adjusted input amounts are preserved, `disable-with-silk-touch` applies unchanged, and non-smeltable drops remain unchanged. This composition does not recursively trigger any block ability or change the parent ability's extra-block EXP multiplier.

`abilities.creative-mode` defaults to false. `abilities.lore.enabled` controls the display-only section and `maximum-entries` bounds its growth.

Hard limits apply even when validation reports a warning:

- Vein Miner: 128 blocks
- Area Break: radius 3
- EXP multiplier: 100
- Cooldown: 86,400 seconds
- Lore: 32 entries

Negative cooldowns, unlock levels below 1, invalid/non-finite multipliers, invalid Vein limits, excessive radius, and invalid ore materials use a default or clamped value. PDC data version remains 1 because unlocks are derived and no ability state is persisted.

## Block EXP decisions

A block grants EXP only when its `experience.blocks.<MATERIAL>` value is positive, the held GrowthTool type is compatible, the event is not cancelled, and the Creative-mode policy allows it. Player-placed blocks are rejected, except for mature ageable crops harvested with a GrowthTool hoe.

Compatibility is resolved in three layers: vanilla `MINEABLE_*`/`CROPS` tags first, stable Material naming groups second, and compact explicit fallback groups third. The bundled defaults cover common stone, normal/deepslate/Nether ores, obsidian, prismarine and brick families; Overworld/Nether/bamboo wood families; common shovel terrain; and crops, leaves, moss, hay, and sculk. An unlisted Material still grants zero EXP even when its category is compatible.

GrowthTools copies the bundled config only when no user config exists. It does not merge newly bundled block entries into an existing custom config during startup or `/gt reload`, and it does not overwrite custom EXP values. Add desired new Material entries manually when upgrading an existing server.

Player-placed configured blocks intentionally grant zero EXP. Ageable WHEAT, CARROTS, POTATOES, BEETROOTS, and NETHER_WART are the exception: only their maximum-age state may award EXP, which prevents immature-crop harvesting from becoming an EXP farm. If a block is unexpectedly rejected, temporarily set `debug: true`, reproduce once, and inspect the `Block EXP rejected` line. It includes the rejection reason, Material, configured EXP, tool type, compatibility, placed state, and Creative state. Disable debug again after diagnosis to avoid per-break diagnostic logging.

## Integrations

Every `integrations.<id>.enabled` switch defaults to true but does not install the external
plugin. mcMMO and Jobs default `ability-extra-block-rewards` to false to reduce duplicate
rewards from synthetic Vein/Area breaks. `grant-growthtools-exp-from-mcmmo` is reserved for a
future inbound mcMMO-to-GrowthTools reward feature and currently remains false. `/gt reload`
revalidates these settings and rebuilds only integration adapters and built-in configuration;
addon-owned abilities remain registered.
