# Terracraft

Real-world **Earth terrain** for Minecraft. Terracraft generates a **Planet Earth** world type with accurate elevation from global DEM data and land biomes derived from [WWF Terrestrial Ecoregions](https://www.arcgis.com/home/item.html?id=1c898239f8234ace82bf41302811916f) (ArcGIS). Optional create-world settings tune horizontal scale, vertical compression, and how flora is placed — including a **Historical** mode that keeps New World crops and many regional plants in plausible native ranges.

Inspired by [Terra 1:1](https://www.curseforge.com/minecraft/mc-mods/terra-1-to-1-minecraft-world-project).

## Companion mod: Materia

Terracraft is designed to work with **[Materia](https://github.com/AndroidDoctorr/Materia)** (`C:/MCMods/Materia`), a progression overhaul that adds regional crops, vines, and trees (olive, grape, wild corn, rice, baobab, palm, cypress, and others).

| Materia installed | Flora on Planet Earth |
|-------------------|------------------------|
| **Yes** | Terracraft’s Materia **bridge** places regional plants via geographic or climate tags (see [plant-distribution-reference.md](documentation/plant-distribution-reference.md)). |
| **No** | Vanilla Minecraft vegetation from cloned biome templates only. |

Terracraft owns **where** Materia plants spawn on Planet Earth; Materia owns **block/item definitions**. The bridge uses Forge biome modifiers with `forge:mod_loaded` conditions — no hard dependency.

## Recommended: Distant Horizons

Planet Earth worlds are enormous at 1:1 scale. **[Distant Horizons](https://www.curseforge.com/minecraft/mc-mods/distant-horizons)** (or similar LOD mods) is not required, but strongly recommended — it makes coastlines, valleys, and landmarks readable from afar while chunks load. Terracraft will remain compatible without it; we plan to call this out on the CurseForge page.

## Quick start (1.20.1 — active development)

```bat
cd 1.20.1
gradlew runClient
```

In-game:

1. **Create New World** → **World** tab → **World Type** → **Planet Earth**
2. Cycle **Flora** (Historical / Biome), **Map scale** (1:1 / 1:2 / 1:4), **Vertical scale** (Logarithmic / Linear)
3. `/tpll 36.066 -112.117` — Grand Canyon area
4. `/tpll 27.988 -86.925 8848` — Everest
5. `/terracraft coords` — lat, lon, and real elevation at your position

First launch downloads DEM tiles and (by default) WWF ecoregion GeoJSON into `.minecraft/terracraft/`.

## Repository layout

| Path | Purpose |
|------|---------|
| `1.20.1/` | **Active development** — Planet Earth generation, Materia bridge, create-world UI (v0.5.x) |
| `1.18.2/`, `1.19.2/`, `1.21.1/` | Bootstrap modules (mod id + lang only; gameplay not ported yet) |
| `shared/` | Shared lang/assets across versions |
| `documentation/` | Design docs, plant tables, agent reference |
| `tools/` | Ecoregion/biome generator scripts |

See [multi-version-setup.md](documentation/multi-version-setup.md) for build instructions per version.

## Documentation

| Doc | Topic |
|-----|--------|
| [documentation/README.md](documentation/README.md) | Documentation index |
| [overview.md](documentation/overview.md) | Status, layout, try-in-game |
| [world-generation-options.md](documentation/world-generation-options.md) | Create-world settings |
| [plant-distribution-reference.md](documentation/plant-distribution-reference.md) | **Master flora table** — vanilla + Materia placement, tags, known gaps |
| [ecoregions-data.md](documentation/ecoregions-data.md) | WWF / ArcGIS data setup |
| [elevation-scaling.md](documentation/elevation-scaling.md) | DEM → Minecraft Y mapping |
| [variation-smoothing-detail-plan.md](documentation/variation-smoothing-detail-plan.md) | Roadmap (variants, smoothing, future plants) |
| [landmark-features-plan.md](documentation/landmark-features-plan.md) | Future iconic sites (White Cliffs, Sedona, etc.) |
| [design-principles.md](documentation/design-principles.md) | Resolved decisions, ornamental rules, suggested ideas |

For AI-assisted development, see [AGENTS.md](AGENTS.md) and [documentation/reference/](documentation/reference/).

## License

MIT License — see [LICENSE](LICENSE).
