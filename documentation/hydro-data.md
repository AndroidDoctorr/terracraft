# Freshwater hydro data (ArcGIS & vectors)

Terracraft currently places **ocean, estuary, and inland lakes** from the Terrarium DEM using depression/spill heuristics (Sprint 2). Sprint 5 improves **lake depth** from meter-space spill. **Named lakes, rivers, and reservoirs** need vector hydro layers — the same ArcGIS REST / GeoJSON pattern already used for WWF ecoregions.

---

## Can we use ArcGIS data?

**Yes.** ArcGIS Online and USGS host global and regional hydro FeatureServices queryable as GeoJSON, paginated like ecoregions. Terracraft would:

1. Download or ship GeoJSON under `.minecraft/terracraft/data/`
2. Build a tiled point-in-polygon (lakes) or distance-to-line (rivers) sampler
3. Override or augment `WaterColumnPlanner` and `RiparianPlacement` at generation time

This is **authoritative geography** — same design principle as ecoregion polygons + DEM.

---

## Recommended sprint split

| Sprint | Scope | Data source (examples) | Generator effect |
|--------|--------|------------------------|------------------|
| **5** ✓ | Lake depth from DEM spill; DEM riparian heuristics | Terrarium DEM only | Shallow basins, gallery forest in valleys |
| **6** | **Lake & reservoir polygons** | Esri Living Atlas / HydroSHEDS lakes, USGS NHD Waterbody, Natural Earth `ne_10m_lakes` | Force `LAKE` columns inside polygons; surface Y from polygon attributes or DEM |
| **7** | **River centerlines & wide channels** | NHD Flowline, Natural Earth `ne_10m_rivers_lake_centerlines`, HydroSHEDS rivers | Riparian bias along buffers; optional 1-block-wide water at 1:1 scale |
| **8** (optional) | **Wetlands / mangrove masks** | USGS NWI, Copernicus GLW | Marsh surface + biome override |

Sprint 6 alone fixes most “missing Lake Tahoe / wrong Merritt shape” issues. Sprint 7 fixes “walk 10 km with no sign of the Mississippi.”

---

## ArcGIS / data sources

### Global (good starting point)

| Dataset | Access | Notes |
|---------|--------|-------|
| [Natural Earth – lakes](https://www.naturalearthdata.com/downloads/10m-physical-vectors/10m-lakes/) | Shapefile → GeoJSON | ~1,400 major lakes; small file, easy to bundle |
| [Natural Earth – rivers + lake centerlines](https://www.naturalearthdata.com/downloads/10m-physical-vectors/10m-rivers-lake-centerlines/) | Shapefile → GeoJSON | Major rivers only |
| [HydroSHEDS](https://www.hydrosheds.org/) | GeoTIFF + vector | Global hydrology; heavier prep |
| Esri [Living Atlas World Hydrography](https://livingatlas.arcgis.com/en/browse?q=hydrography) | FeatureServer REST | Region-dependent; check license |

### United States (high detail)

| Dataset | ArcGIS / API | Notes |
|---------|--------------|-------|
| [USGS NHD](https://www.usgs.gov/national-hydrography/national-hydrography-dataset) | [Hydro National Map viewer](https://hydro.nationalmap.gov/arcgis/rest/services) | Flowlines, water bodies, areas — best for Bay Area, Grand Canyon, Iowa playtests |
| NHDPlus HR | Download by HUC | Large; tile by region like DEM cache |

Example NHD FeatureServer pattern (verify layer id before production):

```
https://hydro.nationalmap.gov/arcgis/rest/services/nhd/MapServer/<layer>/query
  ?where=1%3D1&outFields=*&outSR=4326&f=geojson&resultRecordCount=2000&resultOffset=0
```

Same pagination merge as [ecoregions-data.md](ecoregions-data.md).

---

## Implementation sketch (Sprint 6)

```
tools/download_hydro.ps1          — paginate ArcGIS / convert Natural Earth SHP
.minecraft/terracraft/data/
  hydro_lakes.geojson             — lake/reservoir polygons
  hydro_rivers.geojson            — (Sprint 7) river LineStrings

com.torr.terracraft.geo.hydro/
  HydroLakeDataset.java           — load GeoJSON, spatial index
  HydroLakeSampler.java           — lat/lon → lake id, optional elevation hint
  HydroLakeSamplerHolder.java

WaterColumnPlanner.plan(...)
  if (HydroLakeSampler.isLake(lat, lon)) → LAKE with polygon-aware surface
```

**Config (planned):**

- `useHydroLakePolygons` — default true when file present
- `hydroLakeDataFile` — `hydro_lakes.geojson`
- `autoDownloadHydroLakeData` — optional REST download URL template
- `hydroLakeMaxElevationMeters` — skip alpine glacier polygons

**Sprint 7 rivers:** buffer centerlines by real width (or fixed meters at horizontal scale) → set riparian strength to 1.0 inside buffer; optionally carve water only when `horizontalScale >= 100000` and `riverWaterEnabled`.

---

## Trade-offs

| Approach | Pros | Cons |
|----------|------|------|
| **DEM depression only** (today) | No extra data; works globally | Misses rivers; lakes need visible DEM bowl; Merritt ~1 m deep is noisy |
| **Natural Earth lakes** | Small, global, offline-friendly | Misses small lakes/reservoirs |
| **NHD / regional ArcGIS** | Accurate for US playtests | Large downloads; need regional tiling |
| **DEM flow accumulation** | No vectors; true drainage | CPU-heavy; still not “named” water bodies |

**Recommendation:** Ship **Natural Earth lakes globally** in the mod JAR (or first-run download), add **optional NHD regional packs** for US detail, reuse ecoregion tile cache architecture.

---

## Testing (after Sprint 6+)

| Location | `/tpll` | Expect |
|----------|---------|--------|
| Lake Merritt | `37.8014 -122.2585` | Lake polygon → water, plausible depth |
| Lake Tahoe | `39.0968 -120.0324` | Large lake polygon, mountain rim |
| Mississippi @ Memphis | `35.15 -90.05` | River buffer → floodplain + gallery trees (Sprint 7) |
| Grand Canyon (Colorado) | `36.066 -112.117` | River in canyon floor (Sprint 7) |

Use `/terracraft coords` — planned fields: `Hydro: lake NHD_…` / `river buffer 120 m`.

---

## Related docs

- [ecoregions-data.md](ecoregions-data.md) — ArcGIS GeoJSON download pattern
- [elevation-scaling.md](elevation-scaling.md) — DEM water & lake depth (Sprint 2 / 5)
- [design-principles.md](design-principles.md) — river network from DEM (long-term alternative)
