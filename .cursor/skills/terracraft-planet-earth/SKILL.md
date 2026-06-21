---
name: terracraft-planet-earth
description: >-
  Work on Terracraft Planet Earth worldgen, WWF ecoregions, elevation mapping,
  biome clones, and the optional Materia flora bridge. Use when editing Terracraft
  world generation, geographic/climate biome tags, biome modifiers, create-world
  settings, or plant-distribution docs.
---

# Terracraft Planet Earth

## Before coding

1. Read `AGENTS.md` and `documentation/plant-distribution-reference.md`
2. Confirm active module: `1.20.1/`
3. For flora changes, decide **Historical** vs **Biome** tag target

## Materia bridge workflow

```
Pick plant + realism target (plant-distribution-reference.md)
    → Add biome id(s) to terracraft tag JSON
    → Add feature to terracraft_* biome modifier (mod_loaded:materia)
    → Update plant-distribution-reference.md table + gaps
```

Do **not** edit Materia for Planet Earth placement rules.

## Ecoregion / biome archetype changes

1. Edit `tools/biome_templates/` or generator script
2. Run `tools/generate_terracraft_biomes.ps1`
3. Reconcile tag membership JSON
4. Update plant-distribution archetype table if templates change

## Test locally

```bat
cd 1.20.1
gradlew runClient
```

Create **Planet Earth** world; use `/tpll` and `/terracraft coords`. Compare Historical vs Biome flora modes.

## Reference files

| File | Purpose |
|------|---------|
| `documentation/reference/ARCHITECTURE.md` | Pipeline |
| `documentation/reference/CODE_MAP.md` | Java/datapack paths |
| `documentation/reference/MATERIA_BRIDGE.md` | Bridge tags and modifiers |
| `documentation/world-generation-options.md` | Create-world fields |
| `documentation/ecoregions-data.md` | WWF GeoJSON |

## Out of scope unless requested

- Porting to 1.18/1.19/1.21 bootstrap modules
- Potato Old World reclassification
- Filling every historical flora gap in one pass
