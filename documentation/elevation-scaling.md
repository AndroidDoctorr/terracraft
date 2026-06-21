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
