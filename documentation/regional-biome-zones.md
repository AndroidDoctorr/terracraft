# Regional biome zones (GeoJSON polygons)

WWF ecoregion **rasters** are coarse (zoom 7, 256 px tiles). At desert edges, playa margins, and landmarks they often bleed the wrong biome for hundreds of blocks. **Regional biome zones** are small GeoJSON polygons that override biome choice after ecoregion lookup.

## How it works

1. `RegionalBiomeZones` loads polygons at startup (bundled + optional user data).
2. `EcoregionBiomeOverrides` checks zones **first** (before rectangular geo boxes and eco pins).
3. Each feature has properties:
   - `biome` — Terracraft biome id without namespace (e.g. `playa_salt`, `desert_arid`)
   - `max_elevation_m` — optional ceiling (Bonneville playa uses ~1320 m so Cobb/Graham peaks stay mountain)
   - `min_elevation_m` — optional floor

Bundled zones live in:

```
1.20.1/src/main/resources/data/terracraft/regional/biome_zones/
```

Drop custom zones in:

```
.minecraft/terracraft/data/regional/biome_zones/*.geojson
```

Restart the game (or reload world) after adding files.

## Example feature

```json
{
  "type": "Feature",
  "properties": {
    "biome": "playa_salt",
    "max_elevation_m": 1320,
    "name": "Bonneville Salt Flats"
  },
  "geometry": {
    "type": "Polygon",
    "coordinates": [[
      [-113.92, 40.715],
      [-112.98, 40.715],
      [-112.98, 40.805],
      [-113.92, 40.805],
      [-113.92, 40.715]
    ]]
  }
}
```

Coordinates are **`[longitude, latitude]`** (GeoJSON order).

## Importing from OpenStreetMap

Google Maps highlight polygons are not directly exportable. **OpenStreetMap** and **ArcGIS** are practical:

| Source | Best for | How |
|--------|----------|-----|
| **OSM Overpass** | Parks, nature reserves, named features (`natural=salt_pan`, `place=locality`) | `tools/import_osm_biome_zone.ps1` |
| **ArcGIS / Living Atlas** | US playas, BLM units, NPS boundaries | Export GeoJSON → drop in biome_zones folder |
| **Natural Earth / WWF** | Large ecoregions (already used globally) | Too coarse for Dover cliffs / Bonneville highway edge |

Run the Overpass helper:

```powershell
.\tools\import_osm_biome_zone.ps1 -Name "Bonneville Salt Flats" -Biome playa_salt -MaxElevationM 1320
```

Output goes to `.minecraft/terracraft/data/regional/biome_zones/`. Edit the polygon if the relation includes highways or mountains.

### Bonneville highway edge

OSM relations often follow the **I-80** shoulder — exactly where the real playa ends. That is usually **correct** for gameplay (salt stops at the road). North of I-80 near Great Salt Lake there are separate salt/mud flats (e.g. **Great Salt Lake Desert**, **Bonneville Salt Flats north extensions**) — add a second polygon when you find an OSM relation or trace from satellite.

## Hyper-arid desert belts

Where raster bleed is extreme (Sahara interior, Arabian Empty Quarter), rectangular **desert belts** in `EcoregionBiomeOverrides` force `desert_arid` before clone-map lookup. Belts are a blunt instrument; replace with polygons as you curate them.

## Surface blocks (Sprint B)

Biome choice is separate from **surface blocks**:

| Biome | Surface (default) |
|-------|-------------------|
| `playa_salt` | Materia `salt_block` on flat low ground; sand ring on slopes; stone on peaks |
| `desert_arid` | Sand |
| `semi_arid_scrub` | Coarse dirt / sand / stone by slope |

See `BiomeSurfaceRules.java`.

## Related

- [landmark-features-plan.md](landmark-features-plan.md) — Dover chalk, Sedona, future landmark surfaces
- [ecoregions-data.md](ecoregions-data.md) — WWF pipeline
