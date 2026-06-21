# World generation options

Planet Earth exposes **one world type** with **inline settings** on the Create World screen (World tab). Values are saved into the world’s biome source when you create the world.

Related: [elevation-scaling.md](elevation-scaling.md), [variation-smoothing-detail-plan.md](variation-smoothing-detail-plan.md), [plant-distribution-reference.md](plant-distribution-reference.md).

---

## Create World UI

Select **Planet Earth** as the world type. Three cycle buttons appear below the world type selector:

| Control | Options | Saved field |
|---------|---------|-------------|
| **Flora** | Historical · Biome | `flora_placement` |
| **Map scale** | 1:1 · 1:2 · 1:4 | `horizontal_scale` |
| **Vertical scale** | Logarithmic · Linear | `elevation_mapping` |

Defaults: Historical flora, 1:1 map, logarithmic vertical (coastal log).

Global config (`terracraft-common.toml`) still provides defaults when fields are omitted from older worlds.

---

## Horizontal scale

**Blocks per degree of latitude/longitude** — how “big” Earth feels when you walk it.

| UI label | `horizontal_scale` | Meaning |
|----------|-------------------|---------|
| **1:1** | 100 000 | ~1 block ≈ 1 m at the equator |
| **1:2** | 50 000 | Half-size Earth |
| **1:4** | 25 000 | Quarter-size Earth |

---

## Vertical scale

| UI label | Value | Behavior |
|----------|-------|----------|
| **Logarithmic** | `coastal_log` | Steep near sea level; mountains compressed |
| **Linear** | `linear` | Constant meters→blocks ratio everywhere |

Tune steepness via `coastalVerticalScale`, `verticalScale`, and `elevationCompressionMeters` in config. See [elevation-scaling.md](elevation-scaling.md).

---

## Ecoregion border blend

`ecoregion_border_blend_blocks` (default **64**) controls vegetation spillover at ecoregion borders. Configurable in preset JSON or `terracraft-common.toml`; not yet on the create-world UI.

---

## Flora placement & Materia

- **Historical** — geographic Terracraft tags (`region_mediterranean`, `crop_new_world`, …)
- **Biome** — climate Terracraft tags (`climate_grassy`, `climate_tropical`, …)

Materia features use `forge:mod_loaded`. Terracraft runs without Materia; with Materia 1.2.0+ regional plants appear on the appropriate tags.

---

## Datapack / modder override

Example biome source snippet:

```json
"biome_source": {
  "type": "terracraft:earth",
  "seed": 0,
  "flora_placement": "historical",
  "horizontal_scale": 50000,
  "elevation_mapping": "coastal_log",
  "ecoregion_border_blend_blocks": 64
}
```
