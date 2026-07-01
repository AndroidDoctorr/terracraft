# WWF Terrestrial Ecoregions (TEOW) data

Terracraft uses the [WWF Terrestrial Ecoregions of the World](https://www.arcgis.com/home/item.html?id=1c898239f8234ace82bf41302811916f) polygons to pick land biomes on Planet Earth worlds.

ArcGIS item pages usually do **not** show a simple “Download” button. The dataset is still reachable through the REST API or third-party mirrors.

## Option 1: Let the mod download it (easiest)

On first launch with `useEcoregionBiomes=true` (default), Terracraft downloads GeoJSON from ArcGIS if the file is missing:

```
.minecraft/terracraft/data/wwf_ecoregions.geojson
```

You can disable auto-download with `autoDownloadEcoregionData=false` in the Terracraft config.

## Option 2: Paste a URL in your browser

Open this URL (page 1 of the paginated export). Your browser will show raw GeoJSON:

```
https://services8.arcgis.com/7L75T5PDROpCazRR/arcgis/rest/services/WWF_ecoregions/FeatureServer/0/query?where=1%3D1&outFields=ECO_ID,ECO_NAME,BIOME,G200_BIOME,eco_code&outSR=4326&f=geojson&resultRecordCount=2000&resultOffset=0
```

That page only contains the first 2000 features (~14k polygon parts total). To fetch everything:

- Increment `resultOffset` by 2000 for each page: `0`, `2000`, `4000`, … until a page returns `"features":[]`.
- Merge all `features` arrays into one GeoJSON `FeatureCollection`.
- Save as `.minecraft/terracraft/data/wwf_ecoregions.geojson`.

Or run the helper script from the repo root:

```powershell
.\tools\download_ecoregions.ps1
```

## Option 3: Official WWF shapefile zip

WWF publishes the TEOW shapefile here:

https://files.worldwildlife.org/wwfcmsprod/files/Publication/file/6kcchn7e3u_official_teow.zip

Terracraft expects GeoJSON today. Convert the shapefile with [QGIS](https://qgis.org/) or [ogr2ogr](https://gdal.org/programs/ogr2ogr.html):

```bash
ogr2ogr -f GeoJSON wwf_ecoregions.geojson eco_regions.shp
```

Then copy the file to `.minecraft/terracraft/data/wwf_ecoregions.geojson`.

## REST API reference

Layer metadata:

```
https://services8.arcgis.com/7L75T5PDROpCazRR/arcgis/rest/services/WWF_ecoregions/FeatureServer/0
```

General ArcGIS REST docs: https://developers.arcgis.com/rest/

Query parameters Terracraft uses:

| Parameter | Value |
|-----------|-------|
| `where` | `1=1` (all features) |
| `outFields` | `ECO_ID,ECO_NAME,BIOME,G200_BIOME,eco_code` |
| `outSR` | `4326` (WGS84 lat/lon) |
| `f` | `geojson` |
| `resultRecordCount` | `2000` (server max) |
| `resultOffset` | page offset |

## How Terracraft uses the data

1. Load GeoJSON at startup.
2. Rasterize polygons into cached Web Mercator PNG tiles (same tiling scheme as DEM data).
3. At each block column, look up the ecoregion ID and map WWF biome codes 1–14 to vanilla biomes.
4. Ocean, beach, and high-alpine biomes still come from elevation, not ecoregions.

Cache location:

```
.minecraft/terracraft/ecoregion_cache_v3/z{zoom}_ss{supersample}/{x}_{y}.png
```

Example: `ecoregion_cache_v3/z7_ss2/42_95.png` at default zoom 7 with 2× supersampling.

**After changing `ecoregionZoom` or `ecoregionRasterSupersample`:** new tiles build automatically under a new subfolder. Old `ecoregion_cache` / `ecoregion_cache_v2` folders can be deleted to save disk space.

Config keys (`.minecraft/config/terracraft-common.toml`):

- `useEcoregionBiomes` — enable WWF-based land biomes
- `useClimateFallback` — use latitude/rainfall heuristic when no polygon matches
- `autoDownloadEcoregionData` — download GeoJSON if missing
- `ecoregionDataFile` — path to GeoJSON (relative to `terracraft/data/` or absolute)
- `ecoregionZoom` — tile zoom for the raster cache (default `7`, ~1.2 km/px at equator)
- `ecoregionRasterSupersample` — render at N×256 then downsample for smoother edges (default `2`)
