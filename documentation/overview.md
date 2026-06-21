# Terracraft

Real-world Earth terrain mod for Minecraft, inspired by [Terra 1:1](https://www.curseforge.com/minecraft/mc-mods/terra-1-to-1-minecraft-world-project).

## Layout

| Path | Purpose |
|------|---------|
| `1.18.2/` | Forge 1.18.2 module (bootstrap) |
| `1.19.2/` | Forge 1.19.2 module (bootstrap) |
| `1.20.1/` | Forge 1.20.1 module — **active development** |
| `1.21.1/` | Forge 1.21.1 module (bootstrap) |
| `shared/` | Shared lang/assets across versions |
| `documentation/` | Design notes — see [multi-version-setup.md](documentation/multi-version-setup.md) |
| `tools/` | Ecoregion/biome generator scripts |

## Current status

**1.20.1 (v0.5.1)** — Planet Earth world type, WWF ecoregion biomes, DEM terrain, Materia bridge, create-world UI options.

**1.18.2 / 1.19.2 / 1.21.1** — Gradle + mod metadata wired; loads in-game as empty bootstrap (`0.0.1-bootstrap`). Gameplay port not started.

## Try in-game (1.20.1)

1. **Create New World** → **World** tab → **World Type** → **Planet Earth**
2. Set **Flora**, **Map scale**, and **Vertical scale** with the cycle buttons below the world type.
3. `/tpll 36.066 -112.117` — Grand Canyon area.
4. `/tpll 27.988 -86.925 8848` — Everest.
5. `/terracraft coords` — lat/lon/elevation at your position.

## Development

```bat
cd 1.20.1
gradlew runClient
gradlew build
```

For other versions, `cd` into that folder and use the same Gradle tasks. See [multi-version-setup.md](documentation/multi-version-setup.md).

## Documentation index

- [multi-version-setup.md](documentation/multi-version-setup.md) — version modules and porting
- [world-generation-options.md](documentation/world-generation-options.md) — create-world settings
- [plant-distribution-reference.md](documentation/plant-distribution-reference.md) — flora placement tables
- [ecoregions-data.md](documentation/ecoregions-data.md) — WWF TEOW data
- [elevation-scaling.md](documentation/elevation-scaling.md) — terrain height mapping
- [variation-smoothing-detail-plan.md](documentation/variation-smoothing-detail-plan.md) — roadmap
