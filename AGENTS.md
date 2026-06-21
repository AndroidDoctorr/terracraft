# Agent guide — Terracraft

Concise context for LLMs working in this repository. Read this before changing world generation, biomes, or the Materia bridge.

## What this mod does

Terracraft adds a **Planet Earth** world preset: real lat/lon coordinates, Terrarium DEM elevation, WWF ecoregion-driven land biomes (44 clone biomes + elevation overrides), and optional **Historical** vs **Biome** flora placement. It is a **soft companion** to **Materia** (separate repo at `C:/MCMods/Materia`).

## Mod boundaries (do not blur)

| Mod | Owns |
|-----|------|
| **Terracraft** | Earth projection, DEM sampling, ecoregion lookup, biome clones, geographic/climate tags, Materia feature placement on Planet Earth, create-world UI |
| **Materia** | Block/item/crop definitions, placed features (`materia:*_placed`), vanilla-world `#materia:*` biome tags |

**Avoid changing Materia** for Planet Earth flora rules — add or adjust Terracraft bridge biome modifiers and tags instead.

## Active code path

- **Work in `1.20.1/`** unless explicitly porting another version.
- Entry: `com.torr.terracraft.terracraft`
- Worldgen: `world/gen/TerracraftChunkGenerator.java`, `geo/*`, `world/biome/*`
- Config: `config/TerracraftConfig.java`, per-world fields on biome source codec
- Materia bridge: `data/forge/biome_modifier/terracraft_*.json` + `data/terracraft/tags/worldgen/biome/*.json`

## Flora modes

| Mode | Config / UI | Tag namespace | Intent |
|------|-------------|---------------|--------|
| **Historical** | `historical` | `#terracraft:region_*`, `#terracraft:crop_*`, `#terracraft:coastal_warm` | Pre-Columbian / native-range placement |
| **Biome** | `biome` | `#terracraft:climate_*` | Climate-band rules (similar to Materia on vanilla worlds) |

Without Materia loaded, bridge modifiers no-op; clone biomes still use vanilla template vegetation.

## Essential docs (read before flora/biome edits)

1. [plant-distribution-reference.md](documentation/plant-distribution-reference.md) — master plant table, tag membership, **known gaps**
2. [world-generation-options.md](documentation/world-generation-options.md) — create-world fields
3. [documentation/reference/ARCHITECTURE.md](documentation/reference/ARCHITECTURE.md) — pipeline and file map
4. [variation-smoothing-detail-plan.md](documentation/variation-smoothing-detail-plan.md) — product direction / roadmap

## When changing flora or tags

Update in lockstep:

1. `documentation/plant-distribution-reference.md`
2. `1.20.1/src/main/resources/data/terracraft/tags/worldgen/biome/*.json`
3. `1.20.1/src/main/resources/data/forge/biome_modifier/terracraft_*.json`
4. Regenerate clone/tag data if archetypes change: `tools/generate_terracraft_biomes.ps1`

## Design rules (read before flora debates)

- [design-principles.md](documentation/design-principles.md) — ornamental / “close enough” decorative plants OK; ore geo-gating deferred; clone grid = archetype × realm
- Gameplay crops and resources stay geographically gated in Historical mode; decoration can be broader

## Known deferred items (do not “fix” unless asked)

- **Potatoes** — Old World classification may need revisiting later
- **Ore geo-gating** — future create-world UI; vanilla/Materia global ores until core worldgen is done
- **Border blend UI** — fixed config default for now
- Historical bridge gaps (rubber, indigo, wisteria wiring, hops, flax tags) — implement when tasked

## Build

```bat
cd 1.20.1
gradlew runClient
gradlew build
```

Java 17. Forge 47.x for 1.20.1.

## Cursor rules & skills

- `.cursor/rules/terracraft-core.mdc` — always-on project context
- `.cursor/rules/terracraft-worldgen.mdc` — worldgen, datapacks, flora bridge
- `.cursor/skills/terracraft-planet-earth/SKILL.md` — workflow for Planet Earth / Materia bridge tasks
