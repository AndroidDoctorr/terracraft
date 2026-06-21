# Terracraft

Real-world Earth terrain mod for Minecraft, inspired by [Terra 1:1](https://www.curseforge.com/minecraft/mc-mods/terra-1-to-1-minecraft-world-project).

## Layout

- `1.20.1/` — Forge 1.20.1 module (run Gradle tasks from here)
- `shared/` — shared assets and datapack resources across versions
- `documentation/` — design notes

## Current status (0.2.0)

- AWS Terrarium DEM tiles with on-disk cache (`/.minecraft/terracraft/dem_cache/`)
- **Planet Earth** world preset on the create-world screen
- Climate-based biome selection (latitude + elevation + pseudo-rainfall)
- `/tpll <lat> <lon> [elevationMeters]` and `/terracraft coords`

## Try in-game

1. **Create New World** → **World** tab → **World Type** → choose **Planet Earth**  
   (It appears alongside Default, Amplified, Large Biomes, etc.)
2. First exploration downloads DEM tiles (cached in `.minecraft/terracraft/dem_cache/`). Expect pauses while tiles fetch.
3. `/tpll 36.066 -112.117` — Grand Canyon area.
4. `/tpll 27.988 -86.925 8848` — Everest (vertically compressed into vanilla height).
5. `/terracraft coords` — lat/lon/elevation at your position.

## Config (`config/terracraft-common.toml`)

| Option | Default | Purpose |
|--------|---------|---------|
| `useStubElevation` | `false` | Procedural hills, no network |
| `demZoom` | `12` | Tile resolution (8–15) |
| `verticalScale` | `0.05` | Real meters → block Y compression |
| `horizontalScale` | `100000` | Blocks per degree |

## Biomes

Biomes use **real elevation** plus a simplified climate model:

- Latitude drives temperature (equator → jungle/savanna, poles → snow/taiga).
- Pseudo-rainfall (deterministic noise from lat/lon) splits deserts vs forests vs jungles.
- Elevation drives oceans, beaches, alpine peaks, and snow line.

This is not as accurate as Terra 1:1’s soil/climate databases, but broad regions should look recognizable.

## Next steps

1. Real precipitation / Koppen climate datasets
2. OSM water polygons (rivers, lakes)
3. Optional CubicChunks when CC3 is usable

## Development

```bat
cd 1.20.1
gradlew runClient
gradlew runServer
gradlew build
```
