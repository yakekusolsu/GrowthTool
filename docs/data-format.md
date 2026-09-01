# GrowthTools Data Format

## Source-of-truth boundaries

- **PDC:** authoritative portable item identity and progression state.
- **SQLite:** registry/audit observations and placed-block protection.
- **Lore:** display-only projection that is never parsed as state.

Database loss must not cause valid PDC to be rewritten. Registry values are last-known observations, not a replacement for the item.

## PDC schema version 1

| Key | Type | Meaning |
| --- | --- | --- |
| `growthtools:id` | String | Random per-tool UUID |
| `growthtools:type` | String | GrowthToolType |
| `growthtools:level` | Integer | Cached level |
| `growthtools:experience` | Long | Authoritative total accumulated EXP |
| `growthtools:created_at` | Long | Creation epoch milliseconds |
| `growthtools:data_version` | Integer | Portable schema version |

`CURRENT_DATA_VERSION` remains `1`. `GrowthToolDataMigrationService` selects contiguous migrations when a real future format change exists. Unknown/newer versions, invalid UUIDs, negative EXP, incomplete data, and Material/type mismatches are never automatically repaired.

A valid version-1 level mismatch is repaired from total EXP. `/gt repair` also re-renders lore and queues a missing registry entry.

## Delayed action PDC

Fishing hooks and bow projectiles temporarily store the originating tool UUID and a processed marker. These values attribute a delayed catch/hit and prevent duplicate gains; they are not item progression state.

## Event timing

The successful update sequence is:

1. Gameplay event
2. EXP calculation and cap
3. Item PDC/lore write
4. `GrowthToolExperienceGainEvent`
5. `GrowthToolLevelUpEvent`, when applicable
6. Player message

Both custom events are unstable in Phase 4.

## Item lifecycle

Unique PDC normally prevents stacking; an exact cloned UUID stack is treated as a duplicate observation. Anvil, grindstone, and smithing previews validate and preserve GrowthTools PDC. Rename, enchantment, durability, chest, hopper, death-drop, and trade movement rely on normal ItemStack PDC preservation, with registry refresh occurring on give, EXP, inspect, join, or pickup. Durability destruction marks the registry `DESTROYED` when observed.
