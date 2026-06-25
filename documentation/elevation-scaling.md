# Elevation scaling

Terracraft maps real DEM elevations (meters) to Minecraft Y levels. The old default (`verticalScale = 0.05`, linear) made **one block ≈ 20 real meters**, so a 400 m hill (Berkeley) was only ~20 blocks — easy to miss when flying, and coastlines looked nearly flat.

## Default: `coastal_log` mapping

Since 0.3.1 the default is **coastal logarithmic** compression:

```
blockOffset = coastalVerticalScale × elevationCompressionMeters × ln(1 + |Δm| / elevationCompressionMeters)
```

- **Δm** = elevation minus sea level (meters)
- **At sea level** the slope is `coastalVerticalScale` blocks per meter (steep enough for recognizable shores and hills)
- **Higher up** each extra meter adds less and less height (Everest’s summit squeeze into vanilla Y range)

### Example heights (defaults: coastal=0.25, knee=250 m, sea Y=63)

| Real elevation | Δ from sea | Minecraft Y (approx) |
|----------------|------------|----------------------|
| Sea level | 0 m | 63 |
| +50 m (low bluff) | 50 m | ~76 |
| +400 m (Berkeley hills) | 400 m | ~123 |
| +8848 m (Everest) | 8848 m | ~287 |

Compare to old linear 0.05: Berkeley ≈ Y 83, barely noticeable.

## Config (`config/terracraft-common.toml`)

| Key | Default | Purpose |
|-----|---------|---------|
| `elevationMapping` | `coastal_log` | `coastal_log` or `linear` |
| `coastalVerticalScale` | `0.25` | Blocks per meter near sea level |
| `elevationCompressionMeters` | `250` | Knee — larger = taller terrain before flattening |
| `verticalScale` | `0.05` | Only used when `elevationMapping=linear` |
| `seaLevelBlockY` | `63` | Minecraft Y for sea level |
| `minWorldY` / `maxWorldY` | `-64` / `320` | Clamp generated terrain |

### Tuning tips

- **Hills still too flat:** raise `coastalVerticalScale` (try `0.35`–`0.5`)
- **Mountains hit Y=320 too early:** lower `coastalVerticalScale` or `elevationCompressionMeters`
- **Want old behavior:** set `elevationMapping = "linear"`

**Important:** Existing worlds keep already-generated chunks. Create a **new Planet Earth world** (or explore new chunks) after changing mapping.

## High-pass terrain (Sprint 1)

When `elevationHighPassEnabled = true` (default), Terracraft splits the DEM into two layers:

| Layer | Source | Vertical mapping |
|-------|--------|------------------|
| **Baseline** | Coarser Terrarium zoom (`demZoom - demBaselineZoomOffset`) | `coastal_log` / `linear` — regional hills and mountains |
| **Detail** | `raw - baseline` at full-res zoom | `elevationDetailVerticalScale` blocks per meter (default 0.25) |

Example at defaults (`demZoom = 12`, offset `2` → baseline zoom 10):

- Regional plateau trend follows ~160 m DEM pixels (compressed with coastal_log).
- Canyon rims, lake basins, and bluffs keep local contrast at ~40 m pixel resolution.

### Config (terrain section in `terracraft-common.toml`)

| Key | Default | Purpose |
|-----|---------|---------|
| `demBilinearSampling` | `true` | Bilinear interpolation between DEM pixels |
| `elevationHighPassEnabled` | `true` | Enable baseline + detail split |
| `demBaselineZoomOffset` | `2` | Baseline zoom = demZoom minus this (min 8) |
| `elevationDetailVerticalScale` | `0.25` | Blocks per meter for local detail |
| `elevationDetailMaxAbsMeters` | `0` | Clamp detail spikes (0 = off) |

### Tuning tips

- **Canyons/walls still too soft:** raise `elevationDetailVerticalScale` (try `0.35`–`0.5`) or lower `demBaselineZoomOffset` to `1`.
- **Terrain too spiky / noisy:** set `elevationDetailMaxAbsMeters` to `150`–`300`.
- **Mountains too tall overall:** lower `coastalVerticalScale` (baseline), not detail scale.
- **Revert to old single-layer mapping:** `elevationHighPassEnabled = false`.

## Water, shorelines, and lakes (Sprint 2)

Terrain generation uses an 18×18 elevation grid per chunk for neighbor spill, slope, and water planning.

| Feature | Behavior |
|---------|----------|
| **Ocean** | Raw DEM ≤ sea level (+ threshold) → seafloor from bathymetry, water filled to `seaLevelBlockY` |
| **Estuaries** | Low land touching ocean (within ~25 m) → water filled to sea level (harbors, tidal flats) |
| **Coastal clamp** | High-pass mapping capped to direct `coastal_log` height for low elevations — prevents cities floating far above sea level |
| **Shoreline bands** | Sand/gravel/stone **only adjacent to water**, not all low-elevation land |
| **Inland lakes** | 8-neighbor spill detects basins; water fills to spill Y |

### Why Sprint 2.1 was needed

Early Sprint 2 painted **sand on all land within 12 m of sea level**, making Manhattan and Oakland look like deserts. Water also failed when high-pass terrain mapped coasts **above** `seaLevelBlockY` — the generator never filled air with water. Coastal clamp + ocean/estuary water columns fix both.

### Config (water section in `terracraft-common.toml`)

| Key | Default | Purpose |
|-----|---------|---------|
| `oceanSurfaceThresholdMeters` | `0.5` | DEM at/below sea + this → open ocean |
| `estuaryMaxElevationMeters` | `25` | Max elevation for harbor fill when touching ocean |
| `coastalTerrainClampEnabled` | `true` | Cap high-pass height on low elevations |
| `coastalTerrainClampBelowMeters` | `75` | Apply clamp below sea + this |
| `coastalTerrainClampBlockMargin` | `3` | Extra blocks above direct mapping when clamping |
| `depressionMinDepthBlocks` | `2` | Min depth below spill for inland lakes |
| `shorelineBandsEnabled` | `true` | Sand/stone only next to water |
| `coastalInundationEnabled` | `true` | Fill below-sea-level mapped terrain in coastal band |
| `waterSurfaceBlockOffset` | `1` | Water fills to `seaLevelBlockY` + this (default y=64) |

### Test coordinates

| Location | `/tpll` | What to check |
|----------|---------|---------------|
| Governor's Island → Manhattan | `40.689 -74.016` | NY Harbor water, grass on island not endless sand |
| Lake Merritt | `37.8014 -122.2585` | Lake basin; `/terracraft coords` → `kind LAKE` |
| Oakland downtown | `37.804 -122.271` | Grass/plains surface, hills eastward |
| SF Bay coast | `37.8199 -122.4783` | Water in bay, sand only at shore |

**New chunks required** after changing water/terrain settings.

## Extending vertical space (optional)

Vanilla Planet Earth uses `min_y: -64`, `height: 384` (Y up to 320). You do **not** need a mod to go a bit taller — edit the datapack dimension type:

`data/terracraft/dimension_type/earth.json`:

```json
"min_y": -64,
"height": 512
```

Then set in config:

```toml
maxWorldY = 447
```

(Max build Y = min_y + height - 1)

### Compatibility mods (1.20.1 Forge)

| Approach | Notes |
|----------|--------|
| **Datapack height** | Safest; increase `height` in `earth.json` + matching `maxWorldY` |
| **Cubic Chunks** | Not available for 1.20.1 Forge in a stable release; overview mentions future CC support |
| **“Extended world height” mods** | If a mod changes overworld height, Planet Earth uses its **own** dimension (`terracraft:earth`) — you must still edit `earth.json` or Terracraft config, not rely on overworld-only mods |

Terracraft does not depend on Cubic Chunks; all mapping is clamped to `minWorldY`/`maxWorldY`.

## Commands

`/tpll 37.8199 -122.4783` — Golden Gate (fly east toward Berkeley and watch Y rise in F3)

`/terracraft coords` — shows real elevation from inverse mapping
