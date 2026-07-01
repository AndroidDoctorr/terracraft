# Landmark features (future)

Some places are iconic because of **specific geology or human history**, not because a climate polygon says "scrub" or "plains." Terracraft's ecoregion + DEM pipeline handles broad regions well; this doc tracks **point/area landmarks** that may need bespoke treatment later.

**Master backlog:** see [special-places.md](special-places.md) for the full curated list (Sedona, playas, coasts, volcanoes, etc.).

## Goal

Capture recognizable sites that topography and biome alone miss:

| Site | What makes it unique | Possible approach |
|------|----------------------|-------------------|
| **White Cliffs of Dover** | Chalk faces above the Channel | Geo-fenced surface override (chalk/concrete powder blocks), cliff band by slope + lat/lon box |
| **Sedona red rock** | Red sandstone gorges | Sandstone palette + iron oxide tint in a polygon; optional stratified layers |
| **Rainbow eucalyptus groves** (Hawaii, parts of Asia) | Multicolor bark | **Terracraft-only easter egg** — not in Materia; small geo patches with custom log/leaf models |
| **Grand Canyon** | (Mostly working via high-pass terrain) | Tune detail scale; optional rim vegetation band |
| **Manhattan historical** | Marsh + forest pockets pre-colonial | Biome **variants** inside ecoregion (see [variation-smoothing-detail-plan.md](variation-smoothing-detail-plan.md)) |

## Design principles

1. **Optional overlays** — Landmarks should not replace ecoregion logic globally; use small GeoJSON polygons or bounding boxes checked after biome classification.
2. **Terracraft vs Materia** — Regional crops/trees stay in Materia; purely ornamental or joke content (rainbow eucalyptus) can live in Terracraft as easter eggs.
3. **No hardcoded block spam** — Prefer surface rules + placed features over hand-sculpted NBT.
4. **Document coordinates** — Each landmark gets test `/tpll` coords and a reference screenshot in `reference/`.

## Implementation sketch (when prioritized)

```
1. data/terracraft/regional/biome_zones/*.geojson  — bundled polygons (Bonneville, Giza, …)
2. .minecraft/terracraft/data/regional/biome_zones/ — user/OSM-imported polygons
3. RegionalBiomeZones.java                         — point-in-polygon biome override (shipped)
4. BiomeSurfaceRules.java                          — playa salt / desert sand / slope bands (shipped)
5. ChunkSurfaceSpillField.java                     — directional downhill spill (Sedona, playa, Cairo) (shipped)
6. tools/import_osm_biome_zone.ps1                 — Overpass → GeoJSON helper
7. LandmarkSurfaceRules.java                       — future: chalk cliffs, travertine, lava palettes
```

See [regional-biome-zones.md](regional-biome-zones.md) for OSM/ArcGIS import notes.

## Rainbow eucalyptus (easter egg)

- Materia may add normal **eucalyptus** for Australia / California-style plantings.
- Terracraft-only **rainbow eucalyptus**: rare patches near real-world coords (e.g. Maui, Mindanao) with custom bark texture cycling hues.
- Keep spawn rate low; discovery reward, not biome filler.

## Related

- [variation-smoothing-detail-plan.md](variation-smoothing-detail-plan.md) — biome variants, smoothing
- [elevation-scaling.md](elevation-scaling.md) — terrain/water pipeline
- [design-principles.md](design-principles.md) — ornamental vs gameplay plants
