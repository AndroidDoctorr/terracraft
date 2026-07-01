# Biome, variation, and detail plan

Design document for Planet Earth worlds: terrain feel, custom biomes, within-region diversity, and **Materia** integration. **Planning only** — implementation follows the phased order at the end.

Related docs: [overview.md](overview.md), [ecoregions-data.md](ecoregions-data.md), [elevation-scaling.md](elevation-scaling.md), [world-generation-options.md](world-generation-options.md), [plant-distribution-reference.md](plant-distribution-reference.md).

---

## Decided (product direction)

| Decision | Choice |
|----------|--------|
| **Flora placement mode** | Player-selectable: **Historical** (pre-Columbian / native ranges) vs **Biome** (climate-tag rules like Materia today) |
| **Where to choose mode** | Create World UI (cycle buttons on World tab) + config default + per-world save data |
| **Biome representation** | **Hard-coded biome clones** with geo tags in JSON; runtime only picks clone + variant details |
| **High-level data** | Bundled JSON / generated tables (`eco_id`, `REALM`, biome code → clone); not computed ad hoc |
| **Materia changes** | **Avoid** — Terracraft owns all Planet Earth flora/ore placement when Materia is present |
| **Integration shape** | Optional **Materia bridge inside Terracraft** (soft dependency), separate bridge jar only if classpath gets messy |
| **Biome borders** | **Buffer / spillover band** at ecoregion edges (`ecoregion_border_blend_blocks`); future: dedicated transition biomes |
| **World scale options** | Per-world **horizontal scale** (1:1, 1:2, 1:4) and **vertical mapping** (linear vs coastal_log) via create-world UI + biome source codec |

See [world-generation-options.md](world-generation-options.md) for preset list and config fields.

---

## Goals (summary)

| Goal | Approach |
|------|----------|
| Recognizable hills, coasts, valleys | Coastal-log elevation (shipped 0.3.1) + DEM smoothing + optional micro-relief |
| Real biomes Minecraft lacks (Mediterranean, steppe, etc.) | **Terracraft custom biomes** built from vanilla + Materia blocks |
| Diversity inside huge WWF polygons | **Biome variants** (tree density, clearings, secondary species) — same region, patchy feel |
| Realistic crops/trees (corn in Americas, olives in Levant, etc.) | **Geographic tags** from ecoregion data → Materia feature placement |
| Fill remaining realism gaps | New plants/ores listed below — prefer **Materia** for items, **Terracraft** for geo rules |

---

## What we have today (v0.5.0)

| Layer | Source | Notes |
|-------|--------|-------|
| **Terrain height** | Terrarium DEM + per-world `elevation_mapping` | Create-world UI: Logarithmic (default) or Linear |
| **Horizontal scale** | Per-world `horizontal_scale` on biome source | Create-world UI: 1:1, 1:2, 1:4 |
| **Land biomes** | WWF ecoregions → **44 Terracraft clone biomes** + elevation overrides | Clone maps for historical and biome-flora modes |
| **Border blend** | `BiomeTransition` + `ecoregion_border_blend_blocks` | Neighbor clone spillover in buffer band (default 64 blocks) |
| **Flora modes** | Historical vs Biome | Create-world UI cycle button |
| **Materia (optional)** | Forge biome modifiers + `mod_loaded:materia` | Historical geo tags + biome climate tags; **1.2.0** plants (esparto, cotton, rice, tea, baobab, palm, cypress) |
| **Ecoregion fields** | `ECO_ID`, `ECO_NAME`, `BIOME`, `G200_BIOME`, `REALM` | Bundled clone maps; GeoJSON download includes REALM |

### Core problems (remaining)

1. **Single clone per ecoregion** — large polygons still uniform; variants and transition biomes not yet implemented.
2. **Terrain terracing** — block-column DEM sampling; smoothing pass still planned.
3. **Biome borders** — spillover helps; dedicated “in-between” biomes still on the roadmap.

---

## Design principles

1. **Geography first** — ArcGIS ecoregion polygons + DEM stay authoritative; variation fills *inside* regions.
2. **Deterministic** — `hash(worldSeed, ecoId, quartX, quartZ)` for variants and clearings.
3. **Layered pipeline** — elevation → ocean/alpine override → ecoregion → **base biome** → **variant profile** → surface/features.
4. **Mod boundaries** — **Terracraft** owns lat/lon, ecoregions, biome clones, geo tags, and **all Materia feature placement on Planet Earth**. **Materia** stays a content mod (blocks/items/features definitions only).
5. **Vanilla-first blocks** — custom biomes reuse vanilla/Materia blocks; new assets only where crucial.
6. **Data in files** — clone definitions, tag membership, and ecoregion→clone maps live in repo JSON; runtime code only resolves lookups and variants.

---

## Target architecture

```
lat/lon + world seed
        │
        ▼
┌─────────────────────┐
│ ElevationSampler    │  DEM + smooth pass + optional micro-relief
│ (+ ElevationScale)  │
└──────────┬──────────┘
           │
           ▼
┌─────────────────────┐
│ BiomePlacement      │  ocean / beach / alpine (elevation overrides)
└──────────┬──────────┘
           │
           ▼
┌─────────────────────┐
│ EcoregionSampler    │  ECO_ID, ECO_NAME, BIOME, REALM…
└──────────┬──────────┘
           │
           ▼
┌─────────────────────┐
│ EcoregionBiomeMapper│  WWF code + ECO_ID overrides → base Terracraft or vanilla biome
└──────────┬──────────┘
           │
           ▼
┌─────────────────────┐
│ BiomeCloneRegistry  │  hard-coded / JSON: ecoregion → terracraft:* clone
└──────────┬──────────┘
           │
           ▼
┌─────────────────────┐
│ BiomeVariantPicker  │  patch-scale variant (density, clearing, secondary trees)
└──────────┬──────────┘
           │
           ▼
┌─────────────────────┐
│ ChunkGenerator      │  surface rules + vanilla feature steps
└──────────┬──────────┘
           │
           ▼
┌─────────────────────┐
│ MateriaBridge       │  if Materia loaded: inject placed features by mode + clone tags
│ (Terracraft module) │  does NOT rely on Materia biome modifiers for Earth clones
└─────────────────────┘
```

**New planned types**

| Type | Role |
|------|------|
| `BiomeCloneRegistry` | Loads bundled JSON; `resolve(ecoId, realm, biomeCode, lat, lon)` → clone biome key |
| `TerracraftBiomes` | Registered clone biomes + JSON (climate, surface, **static geo tags**) |
| `BiomeVariantProfile` | Per-patch: tree density, clearings, secondary features (runtime only) |
| `FloraPlacementMode` | `HISTORICAL` \| `BIOME` — per world |
| `MateriaBridge` | Registers Forge feature modifiers targeting `#terracraft:*` tags; references `materia:*_placed` features |
| `PlanetEarthWorldData` | Persists flora mode in level custom data |

---

## Part A — WWF biomes vs Minecraft (gaps)

Olson **14 TEOW biome codes** (from ArcGIS `BIOME` / `G200_BIOME`):

| Code | WWF name | Current vanilla mapping | Problem | Planned Terracraft biome |
|------|----------|-------------------------|---------|--------------------------|
| 1 | Tropical moist forest | `JUNGLE` | OK-ish | + variants; Materia rubber |
| 2 | Tropical dry forest | `SPARSE_JUNGLE` | Dry forest ≠ sparse jungle | `terracraft:tropical_dry_forest` |
| 3 | Tropical conifer forest | `JUNGLE` | Wrong tree feel | `terracraft:tropical_conifer_woodland` |
| 4 | Temperate broadleaf/mixed | `FOREST` | OK base | variants + Materia grape/wisteria |
| 5 | Temperate conifer | `TAIGA` | OK base | variants |
| 6 | Boreal/taiga | `TAIGA` | OK | variants, snow line from elevation |
| 7 | Tropical savanna | `SAVANNA` | OK | variants |
| 8 | Temperate grassland/steppe | `PLAINS` | Steppe ≠ plains | `terracraft:temperate_steppe` |
| 9 | Flooded grassland | `SWAMP` | OK-ish | `terracraft:floodplain_meadow` |
| 10 | Montane grassland | `GROVE` | Alpine meadow ≠ grove | `terracraft:montane_meadow` |
| 11 | Tundra | `SNOWY_PLAINS` | OK | variants |
| **12** | **Mediterranean forests, woodlands & scrub** | `FLOWER_FOREST` | **Major mismatch** | **`terracraft:mediterranean_scrub`** |
| 13 | Desert/xeric shrubland | `DESERT` | Misses semi-arid scrub | `terracraft:semi_arid_scrub` (+ desert variant) |
| 14 | Mangrove | `MANGROVE_SWAMP` | OK where coast + code 14 | elevation/coast gate |

**Fine tuning:** `ECO_NAME` and `ECO_ID` overrides in JSON (e.g. "California interior chaparral" → semi-arid scrub even if code 8).

**Data upgrade:** extend GeoJSON download with **`REALM`**, **`ECO_REG`**, **`NNH`** (if present on FeatureServer) for crop distribution and realm tags.

---

## Part B — Biome clones (catalog)

Planet Earth **never returns vanilla biome IDs** from `TerracraftBiomeSource`. Every column gets a **clone** — same climate/surface as a vanilla archetype, but a distinct registry entry with **baked-in geo tags**.

### Naming convention

```
terracraft:{archetype}_{region}

Examples:
  terracraft:mediterranean_scrub          — custom archetype (WWF code 12)
  terracraft:forest_palearctic            — clone of forest + Palearctic tags
  terracraft:plains_nearctic              — clone of plains + Nearctic + crop_new_world
  terracraft:steppe_central_asia          — custom / override for ECO_ID groups
```

### What is hard-coded vs runtime

| Hard-coded (repo JSON, easy to edit) | Runtime only |
|--------------------------------------|--------------|
| Clone biome definitions (climate, colors, surface builders) | Variant profile (clearing, tree density) |
| Tags on each clone (`#terracraft:region_*`, `#terracraft:climate_*`) | Ecoregion polygon → `ECO_ID` sample |
| `ecoregion/clone_map.json` — `ECO_ID` → clone | Elevation / ocean overrides |
| `ecoregion/realm_defaults.json` — fallback when eco missing | Smooth elevation sampling |
| `ecoregion/name_overrides.json` — `ECO_NAME` substring rules | |
| Flora feature → tag mapping tables for MateriaBridge | |

### Clone tag layers (always on biome JSON)

Every clone carries **three tag layers** (orthogonal):

| Layer | Tag prefix | Purpose |
|-------|------------|---------|
| **Geo** | `#terracraft:region_*`, `#terracraft:crop_*` | Historical flora mode |
| **Climate** | `#terracraft:climate_grassy`, `climate_temperate`, `climate_tropical`, … | Biome flora mode (Materia-like) |
| **Archetype** | `#terracraft:archetype_forest`, `archetype_plains`, … | Shared variant palettes |

Historical mode uses **geo** tags. Biome mode uses **climate** tags. Same clone, different bridge modifier tables.

### Priority clones (phase 1)

#### `terracraft:mediterranean_scrub`

- **Look:** open oak/cypress-scrub, dry grass, dirt paths, limestone/gravel patches (Materia `limestone`, `rock`)
- **Plants:** Materia **olive**, **grape** vines; sparse vanilla oak; optional dead bushes
- **Tags:** `#terracraft:region_mediterranean`, `#terracraft:crop_old_world`, `#terracraft:climate_temperate`, `#terracraft:archetype_mediterranean`
- **Test:** `/tpll 36.2017 36.1600` (Antakya), `/tpll 40.8518 14.2681` (Naples)

#### `terracraft:temperate_steppe`

- **Look:** yellow-ish grass, very sparse trees, wind-swept
- **Plants:** wild **flax** (Old World); no corn/peppers
- **Tags:** `#terracraft:region_palearctic`, `#terracraft:crop_old_world`, `#terracraft:climate_grassy`

#### `terracraft:semi_arid_scrub`

- **Look:** between desert and plains — terracotta, acacia-like (vanilla acacia), dead bushes
- **Plants:** saltbush feel via dead bush; Materia saltpeter in nearby desert tag
- **Tags:** `#terracraft:region_semi_arid`

#### `terracraft:tropical_dry_forest`

- **Look:** sparse trees, muted green, seasonal dry feel
- **Tags:** `#terracraft:region_tropical_dry`, `#materia:tropical` (limited)

#### `terracraft:montane_meadow`

- **Look:** high elevation — grass, flowers, few trees; links to elevation override band
- **Tags:** `#terracraft:region_montane`

### Secondary biomes (phase 2)

- `terracraft:floodplain_meadow` — long grass, occasional waterlogged soil
- `terracraft:tropical_conifer_woodland` — pine-like conifers in tropics (vanilla spruce palette)
- `terracraft:chaparral` — US/Mediterranean-climate scrub (can merge with med scrub + ECO_ID split)
- `terracraft:wooded_savanna` — baobab/acacia savanna variant for Afrotropical code 7

All clones must be listed in `TerracraftBiomeSource.POSSIBLE_BIOMES`.

### Bundled data files

```
data/terracraft/ecoregion/clone_map.json       — ECO_ID → clone biome id (867 rows, generated once from ArcGIS)
data/terracraft/ecoregion/realm_defaults.json — REALM + WWF code → default clone
data/terracraft/ecoregion/name_overrides.json   — ECO_NAME substring → clone (sparse manual fixes)
data/terracraft/worldgen/biome/*.json           — clone definitions + tags
data/terracraft/tags/worldgen/biome/*.json      — tag membership (optional duplicate of inline tags)
```

Regenerate `clone_map.json` with a repo script (`tools/build_ecoregion_clone_map.ps1`) when WWF data updates — not at game runtime.

---

## Part C — Biome variants (diversity inside one region)

**Problem:** A single WWF ecoregion can cover 500+ km. One biome ID → monotonous.

**Solution:** Keep **one base biome** per column (for fog/music/spawn rules), but attach a **variant profile** that changes **feature placement** (not a separate biome registry entry — unless we need distinct tags).

### Variant dimensions

| Dimension | Example | Implementation |
|-----------|---------|----------------|
| **Tree density** | 0.4× – 1.6× vanilla tree features | Scale `ProbabilityConfig` or skip/add tree feature pass |
| **Clearings** | 5–12% of patches → meadow/plains surface | Noise threshold on `(seed, ecoId, chunkX, chunkZ)` |
| **Secondary tree species** | birch mixed into forest; acacia in savanna | Extra `SimpleRandomFeature` with variant-gated chance |
| **Understory** | ferns, grass, flowers | Vanilla forest flowers / Materia indigo where tagged |
| **Dead / dry patch** | Mediterranean summer-dry feel | dead bush clusters, netherrack-free "dry grass" via custom surface state optional |

### Variant profiles (examples)

**`mediterranean_scrub`**

| Variant | Weight | Effect |
|---------|--------|--------|
| `OPEN_SCRUB` | 45% | low tree density, more grass/rock |
| `WOODLAND` | 35% | olive + oak, Materia grape on trellis |
| `CLEARING` | 12% | meadow-like, no trees |
| `ROCKY_OUTCROP` | 8% | Materia limestone/rock surface bias |

**`temperate_broadleaf` (vanilla forest base)**

| Variant | Weight | Effect |
|---------|--------|--------|
| `MIXED_FOREST` | 50% | oak + birch |
| `BEECH_LIKE` | 25% | dark forest palette |
| `FLOWER_UNDERSTORY` | 15% | flower forest features |
| `CLEARING` | 10% | plains pocket |

**`temperate_steppe`**

| Variant | Weight | Effect |
|---------|--------|--------|
| `OPEN_STEPPE` | 70% | almost no trees |
| `RIparian_GALLERY` | 20% | tree line along low elevation (future river data) |
| `CULTivated_FIELD` | 10% | wild flax patch bias (Old World) |

### Coherence

- Patch size: **64–256 blocks** (`variationPatchScaleBlocks`)
- Hash input: `worldSeed ^ ecoId ^ (blockX >> 6) ^ (blockZ >> 6)`
- Same patch always same variant (multiplayer-safe)

### API sketch

```java
BiomeVariantProfile pick(long seed, int ecoId, int quartX, int quartZ, ResourceKey<Biome> baseBiome);
void applyVariantFeatures(ChunkAccess chunk, BiomeVariantProfile profile, RandomSource random);
```

---

## Part D — Flora placement modes & Materia bridge (no Materia edits)

### The problem with Materia’s default modifiers

Materia registers Forge `add_features` modifiers on **`#materia:grassy`**, **`#materia:temperate`**, etc. Those tags list **vanilla** biome IDs. They run in **every** overworld — including Planet Earth — and ignore geography.

We will **not** ask Materia to change those files. Instead, Terracraft makes Materia’s modifiers **irrelevant on Planet Earth** and replaces them with its own bridge.

### Strategy: clones + Terracraft-owned injection

```
┌─────────────────────────────────────────────────────────────┐
│ Planet Earth column                                         │
│   TerracraftBiomeSource → terracraft:forest_palearctic      │
│   (never minecraft:forest)                                  │
└─────────────────────────────────────────────────────────────┘
         │
         │  Materia modifiers still target minecraft:forest
         │  → no match on Planet Earth clones ✓
         ▼
┌─────────────────────────────────────────────────────────────┐
│ MateriaBridge (Terracraft, if ModList.isLoaded("materia")) │
│   Registers add_features on #terracraft:… tags only       │
│   Uses existing Materia placed features:                    │
│     materia:wild_corn_placed, materia:olive_tree_placed, …│
└─────────────────────────────────────────────────────────────┘
```

Materia continues to define blocks, items, and **placed feature** IDs. Terracraft only decides **where** they spawn on Earth.

**Ores:** Materia ore modifiers use `#materia:overworld` — they still run on Planet Earth. **Acceptable until core worldgen is done.** Geographic ore placement is a **planned create-world UI option** (data source and density tuning TBD) — not in scope yet. **Flora is the priority.**

### Flora placement modes

| Mode | ID | Behavior |
|------|-----|----------|
| **Historical** | `historical` | Native ranges — corn/peppers only on clones tagged `#terracraft:crop_new_world`; olive on `#terracraft:region_mediterranean`; etc. |
| **Biome** | `biome` | Climate-style — mirrors Materia’s current tag logic using `#terracraft:climate_*` on clones (grassy clones get all grassy crops worldwide). |

Both modes use the **same biome clones** and the same ecoregion→clone map. Only the **bridge modifier table** changes.

#### Example: wild corn

| Mode | Bridge targets | Result |
|------|----------------|--------|
| Historical | `#terracraft:crop_new_world` ∩ grassy archetypes | Americas only |
| Biome | `#terracraft:climate_grassy` | Any Earth grassland clone globally |

#### Example: olive tree

| Mode | Bridge targets | Result |
|------|----------------|--------|
| Historical | `#terracraft:region_mediterranean` (+ Levant eco_ids in clone map) | Mediterranean / Middle East |
| Biome | `#terracraft:climate_temperate` | All temperate clones (gameplay-friendly, less realistic) |

### Choosing the mode

**Shipped (v0.5.1)** — single **Planet Earth** world type with cycle buttons on the World tab (flora, map scale, vertical scale). Settings persist in the biome source codec.

**Persisted save data:** `PlanetEarthWorldData` attached to level — stores `flora_placement` so bridge registers correct modifiers on load. Codec field on biome source copied at world creation.

### MateriaBridge implementation sketch

```java
// Terracraft — runs at common setup if materia loaded
public final class MateriaBridge {
  public static void register(FloraPlacementMode mode) {
    for (FloraRule rule : FloraRuleTable.load(mode)) {
      // ForgeBiomeModifiers.addFeatures(rule.biomeTag(), rule.step(), rule.placedFeature())
    }
  }
}
```

Bundled JSON:

```
data/terracraft/materia_bridge/historical_flora.json
data/terracraft/materia_bridge/biome_flora.json
```

Each entry:

```json
{
  "biomes": "terracraft:crop_new_world",
  "step": "vegetal_decoration",
  "features": [
    "materia:wild_corn_placed",
    "materia:wild_peppers_placed",
    "materia:wild_beans_placed",
    "materia:wild_squash_placed"
  ]
}
```

**Soft dependency:** compile against Materia optionally (`compileOnly`) or use resource locations only (no Java import from Materia — safest, no classpath coupling).

**Wild hops:** bridge adds `materia:wild_hops_vine_placed` on historical temperate-forest geo tag (Materia never wired this in 1.20.1 — we fix it here only on Earth).

### If Materia is not installed

- Terracraft clones still generate with vanilla trees/surface
- Bridge module skipped
- No crash, no optional-dependency requirement in `mods.toml` (soft `mandatory=false` entry optional for CurseForge clarity)

### Separate bridge jar?

**Default:** single Terracraft jar, `com.torr.terracraft.integration.materia` package.

**Split later** only if we need Terracraft without Materia compile artifacts confusing IDE — not needed for v1.

### Minimal Materia change (optional, not planned)

If double-spawning ever appears (Materia + bridge on same block): add one line to Materia `#materia:disabled` tag docs — **not required** if Planet Earth uses zero vanilla biomes.

---

## Part E — Geographic distribution reference

### Crops & trees (Materia + rules)

| Materia content | Real-world native range | Terracraft gate tag | Notes |
|-----------------|-------------------------|---------------------|-------|
| **Corn, peppers, beans, squash** (wild) | Americas | `#terracraft:crop_new_world` | Not in Europe/Asia/Africa |
| **Flax** (wild) | Eurasia | `#terracraft:crop_old_world` | Not in Americas |
| **Olive** | Mediterranean, Middle East | `#terracraft:region_mediterranean` + eco override | Not in northern Europe taiga |
| **Grape** | temperate + Mediterranean | med + `#terracraft:region_palearctic` forest variants | |
| **Wisteria** | East Asia + ornamental elsewhere | `#terracraft:region_indomalayan`, `#terracraft:region_mediterranean` (ornamental) | decorative look-alike OK in Med — [design-principles.md](design-principles.md) |
| **Hops** | Europe, western Asia, eastern NA | ecoregion allow-list JSON | fix missing biome modifier |
| **Rubber** | Amazon, SE Asia | `#terracraft:region_neotropical` + `#terracraft:region_indomalayan` | not Africa |
| **Indigo** | tropics/subtropics | same as rubber | |

### Terracraft geographic tags (on clone JSON)

| Tag | Meaning | Used in mode |
|-----|---------|--------------|
| `#terracraft:earth` | All Planet Earth clones | both |
| `#terracraft:region_mediterranean` | Mediterranean + Levant | historical (olive, grape) |
| `#terracraft:region_nearctic` / `neotropical` / … | WWF REALM | historical |
| `#terracraft:crop_new_world` | Americas native crops | historical |
| `#terracraft:crop_old_world` | Flax, etc. | historical |
| `#terracraft:climate_grassy` | Materia-like grassy | biome |
| `#terracraft:climate_temperate` | Materia-like temperate | biome |
| `#terracraft:climate_tropical` | Materia-like tropical | biome |
| `#terracraft:climate_temperate_forest` | Forest vines | biome |

Tags are **static on clone JSON**. `clone_map.json` assigns each `ECO_ID` to a clone that already carries the correct tags.

### Ecoregion-driven clone map (not runtime overrides)

```json
{
  "527": "terracraft:mediterranean_scrub",
  "798": "terracraft:chaparral_nearctic",
  "defaults": {
    "12": "terracraft:mediterranean_scrub",
    "8": "terracraft:steppe_palearctic",
    "4": "terracraft:forest_palearctic"
  },
  "realm_fallback": {
    "Nearctic": "terracraft:plains_nearctic",
    "Palearctic": "terracraft:plains_palearctic"
  }
}
```

Build the full `ECO_ID` table offline from ArcGIS (`REALM`, `BIOME`, `ECO_NAME`). Manual `name_overrides.json` only for edge cases.

---

## Part F — Suggested new content (gaps)

Prefer adding **items/blocks to Materia**; Terracraft only adds geo rules and biome shells.

### High priority (realism impact)

| Content | Type | Region | Rationale |
|---------|------|--------|-----------|
| **Cork oak / cypress log-leaves** | tree | Mediterranean | Distinct from olive; vanilla oak wrong |
| **Date palm** | tree | Middle East, N Africa | Desert oasis identity |
| **Tea bush** | crop | China, India, Japan ecoregions | Major Old World crop gap |
| **Rice** (wild paddies) | crop/water | monsoon Asia | Distinct from wheat |
| **Cotton** | crop | subtropical | Industrial/ag history |
| **Papyrus** | plant block | Nile, wetlands | Swamp/floodplain detail |
| **Esparto / dry grass block** | ground cover | Mediterranean, steppe | Surface variety |

### Medium priority

| Content | Type | Region |
|---------|------|--------|
| **Baobab** (reuse thick trunk mechanic) | tree | Madagascar, Afrotropical savanna |
| **Neem / acacia variant** | tree | Indian subcontinent, Sahel |
| **Coconut palm** | tree | coastal tropics |
| **Sugarcane enhancement** | crop | already vanilla — geo-gate density only |
| **Opium poppy / lavender** | flower crop | Mediterranean, Golden Triangle — optional, config-gated |
| **Laterite soil block** | block | tropical laterite regions | pairs with Materia bauxite |
| **Peat block** | block | boreal wetlands | taiga/floodplain |

### Ores (mostly covered by Materia)

Materia already has tin, bauxite, malachite, sphalerite, sulfur, saltpeter, magnetite, limestone, marble. **Terracraft** may only need **regional density modifiers** (e.g. more bauxite in `#terracraft:region_indomalayan`).

### Explicitly defer

- Cities, roads, buildings (OSM)
- Livestock breeds
- Regional mob replacements

---

## Part G — Terrain smoothing & micro-detail

*(Complements [elevation-scaling.md](elevation-scaling.md).)*

### Elevation (partially shipped)

- **`coastal_log`** default — recognizable hills (Berkeley ~+60 blocks vs old ~+20)
- **Still needed:** bilinear DEM sampling, sea-level feathering, optional ±1–3 block micro-relief (off by default)

### Biome borders

- Raise `ecoregionZoom` to 7–8
- Supersampled polygon rasterization (2×)
- Optional border blend band for variant weights (not biome ID blend)

### Shoreline

- Variable beach width from slope + noise
- Cliff coast → `STONY_SHORE` band

### Config (terrain section)

| Key | Default | Purpose |
|-----|---------|---------|
| `elevationSmoothingMode` | `bilinear` | DEM interpolation |
| `elevationSmoothingMaxDeltaMeters` | `12` | Clamp smooth deviation |
| `ecoregionZoom` | `7` | Sharper ecoregion tiles (~1.2 km/px) |
| `ecoregionRasterSupersample` | `2` | Anti-alias eco borders (shipped Sprint 8) |
| `microReliefEnabled` | `false` | Tiny height noise |
| `biomeVariationEnabled` | `true` | Variant profiles |
| `variationPatchScaleBlocks` | `128` | Variant patch size |

---

## Part H — Data files to add

| File | Purpose |
|------|---------|
| `data/terracraft/ecoregion/clone_map.json` | ECO_ID → clone biome (generated) |
| `data/terracraft/ecoregion/realm_defaults.json` | REALM + WWF code fallbacks |
| `data/terracraft/ecoregion/name_overrides.json` | Sparse ECO_NAME fixes |
| `data/terracraft/ecoregion/variant_palettes.json` | Archetype → variant weights |
| `data/terracraft/materia_bridge/historical_flora.json` | Historical mode feature rules |
| `data/terracraft/materia_bridge/biome_flora.json` | Biome mode feature rules |
| `data/terracraft/worldgen/biome/*.json` | Clone definitions |
| `data/terracraft/worldgen/world_preset/planet_earth.json` | Single world type; defaults for create-world UI |
| `.minecraft/terracraft/data/wwf_ecoregions.geojson` | Add **REALM** to download fields |

---

## Sprint 3 — Biome variants (shipped)

Patch-scale variation within Terracraft clone biomes without adding new biome registry entries.

| Component | Path |
|-----------|------|
| Variant palettes | `data/terracraft/ecoregion/variant_palettes.json` |
| Picker / decorator | `BiomeVariantPicker`, `BiomeVariantDecorator` |
| Archetypes | Mediterranean, forest, plains, chaparral, steppe |

**Behavior:** After vanilla biome decoration, each 128×128 block patch gets a weighted profile (OPEN_SCRUB, WOODLAND, CLEARING, MARSH_POCKET, …). Woodland patches may receive extra trees; clearing patches become meadows; marsh pockets add tall grass. Tree density by elevation is tracked in `/terracraft coords` for tuning — **post-gen per-block tree removal was removed** after it produced floating leaf clumps.

**Config:** `biomeVariantsEnabled`, `variationPatchScaleBlocks`, `variantElevationFalloffMeters`, `variantLowElevationTreeBoost`, `variantHighElevationTreeScale`

**Debug:** `/terracraft coords` shows `Variant: WOODLAND, tree density 1.05, …`

**Flora polish:** Mediterranean uses green `patch_grass_plain` + precipitation; plains/forest/chaparral tree counts raised in `build_flora_placed_features.ps1`.

**New chunks required** after updating jar or regenerating biome JSON.

---

## Sprint 4 — Ecoregion borders (shipped)

Softens hard ecoregion polygon edges with distance-weighted blending, transition clones, and rain-shadow nudges.

| Component | Path |
|-----------|------|
| Border sampler | `EcoregionBorderSampler` — 8-direction multi-distance edge strength |
| Transition map | `data/terracraft/ecoregion/transition_palettes.json` |
| Rain shadow | `RainShadowPlacement` — leeward semi-arid/chaparral bias |

**Behavior:** Near ecoregion edges, spill probability scales with border strength. Common pairs (forest↔steppe, med↔semi-arid) use transition clones like `montane_meadow` or `chaparral_nearctic` instead of abrupt neighbor swaps. Rain-shadow zones nudge forest/plains toward `semi_arid_scrub`.

**Config:** `ecoregionBorderBlendEnabled`, `ecoregionBorderBlendBlocks` (default 96), `ecoregionBorderTransitionEnabled`, `ecoregionBorderSpillWeight`, `rainShadowEnabled`, `rainShadowMinUpwindMeters`

**Debug:** `/terracraft coords` shows border strength and rain-shadow status.

**Test:** Walk ecoregion boundaries in Antioch foothills, Alps↔Mediterranean coast, Basin & Range (Nevada east of Sierra).

---

## Sprint 5 — Lake depth & riparian heuristics (shipped)

Improves inland lake depth from DEM meter spill and adds drainage-corridor vegetation without vector hydro data yet.

| Component | Path |
|-----------|------|
| Lake depth | `LakeDepthMapper` — meter spill + shallow-basin cap |
| Riparian relief | `RiparianSampler` — cardinal DEM relief / corridor detection |
| Biome nudge | `RiparianPlacement` — strong corridors → `floodplain_meadow` |
| Variants | Wetland + gallery tree boost on steppe/savanna/plains corridors |

**Behavior:** Inland lakes use real-world spill elevation (meters) for water surface when block-space spill collapses under vertical compression. Shallow basins preserve DEM depth instead of flat fill. Low valleys with directional relief get wetland understory and optional floodplain biome nudge.

**Config:** `lakeMeterSurfaceEnabled`, `lakeShallowPreserveEnabled`, `lakeMinDepthBlocksFromDem`, `lakeMaxDepthBlocks`, `riparianEnabled`, `riparianBiomeNudgeStrength`, `riparianWetlandStrength`

**Debug:** `/terracraft coords` shows lake spill/depth and riparian strength.

**Test:** Lake Merritt (`37.8014 -122.2585`), Sacramento Delta (`38.3 -121.5`), Nile valley (`26.0 32.5`), Iowa river bottoms (`41.6 -93.6`).

**Next:** Vector hydro from ArcGIS — [hydro-data.md](hydro-data.md) (Sprint 6 lakes, Sprint 7 rivers).

---

## Sprint 6 — Hydro lakes (reverted for Great Lakes)

Natural Earth polygons and DEM “lake surface” fill both **broke Great Lakes coasts** (straight lines, flooded Chicago). Current defaults:

| Setting | Default | Notes |
|---------|---------|-------|
| `useHydroLakePolygons` | **false** | Do not enable for Lake Michigan |
| `hydroLakeSupplementEnabled` | true | Lake Merritt only (California) |
| Water elsewhere | Sprint 2/5 DEM | Depression basins + ocean/estuary only |

**Great Lakes + Chicago River:** need proper regional hydro (USGS NHD bathymetry + river lines) in a future sprint — not global NE polygons or flat-elevation DEM fill.

**After updating:** set `useHydroLakePolygons = false` in `terracraft-common.toml` if you enabled it earlier; regenerate Chicago chunks.

---

## Implementation order (revised)

```
Phase 0  GeoJSON + REALM; tools/build_ecoregion_clone_map.ps1
Phase 1  Biome clone registry + med/steppe/scrub clones + clone_map.json
Phase 2  FloraPlacementMode + create-world UI + level save data
Phase 3  MateriaBridge (historical + biome JSON tables, soft dep)
Phase 4  Biome variant profiles (density, clearings) — **shipped Sprint 3** (see below)
Phase 5  Terrain smoothing (bilinear DEM, shoreline) — **partial: lake depth Sprint 5**
Phase 5b Hydro vectors (ArcGIS lakes) — **shipped Sprint 6**, see [hydro-data.md](hydro-data.md)
Phase 5c Hydro vectors (rivers / riparian) — **planned Sprint 7**
Phase 6  Feature gen audit on clones
Phase 7  Ecoregion border anti-alias — **shipped Sprint 4**
Phase 8  New content in Materia (tea, date palm…) + bridge entries only
Phase 9  Ore geo-gating (create-world UI + deposit data) — after Phases 4–8 stable
```

Each phase behind config flags where sensible.

Mission-aligned ideas not yet scheduled: [design-principles.md](design-principles.md#mission-aligned-ideas-suggested-not-yet-in-roadmap).

---

## Testing checklist

| Location | `/tpll` | Expect after full plan |
|----------|---------|------------------------|
| **Antakya** | `36.2017 36.1600` | Mediterranean scrub, olive, no corn |
| **Naples** | `40.8518 14.2681` | Mediterranean variant mix, grape, Vesuvius slope |
| **San Francisco** | `37.8199 -122.4783` | Chaparral/semi-arid, hills visible, no olive |
| **Iowa** | `41.5868 -93.6250` | Corn/peppers wild, temperate steppe/forest variants |
| **Amazon** | `-3.4653 -62.2159` | Rubber, indigo; no flax |
| **Mesopotamia** | `33.3152 44.3661` | Date palm (future), no corn |

Use `/terracraft coords` and F3 biome display (debug) to verify.

---

## Biome transition zones (design)

**Goal:** Soften ecoregion edges so forest does not end in a straight wall at steppe.

| Approach | Status | Notes |
|----------|--------|-------|
| **Border buffer / spillover** | **Shipped (v0.5.0)** | Sample neighbor ecoregion within `ecoregion_border_blend_blocks`; probabilistic neighbor clone |
| **Transition biomes** | Planned | e.g. `forest_steppe_edge` clones used only in buffer band |
| **Vegetation spill** | Partial (via Materia tags + border blend) | Feature density could later vary by distance-to-border |
| **In-between biomes** | Planned | Optional third clone for common adjacency pairs (Mediterranean↔steppe, taiga↔tundra) |

Buffer width is per-world (preset JSON) or global config. **Default: fixed 64 blocks** — no create-world UI yet; random width or distance-based functions are a later experiment. See [design-principles.md](design-principles.md).

---

### Open questions (remaining)

1. **Mod rename** — `terracraft:` namespace is fine until rename; clone map regenerates either way.

### Resolved

| Question | Answer |
|----------|--------|
| Historical vs biome flora | Both; player/world selectable |
| Biome clones vs runtime tags | Clones with baked tags; runtime = variant + lookup only |
| Materia repo changes | Avoid; Terracraft MateriaBridge |
| Bridge mod vs inline | Inline in Terracraft first |
| Create-world UI (core) | Flora, map scale, vertical scale shipped v0.5.1; border blend config-only for now |
| Wisteria in Historical mode | Ornamental Mediterranean OK; native Indo-Malayan / East Asian ranges too — see [design-principles.md](design-principles.md) |
| Ornamental / “close enough” plants | Mod-wide: decorative look-alikes OK when no gameplay/resource difference |
| Ore geo-gating | **Deferred** — future create-world option; vanilla/Materia global ores until then |
| Clone count | **Full archetype × realm cross product** as target model (~40 clones today is interim) |
| Border blend width | Fixed default; UI and advanced edge rules later |

---

## Success criteria

Walking 500 blocks in Antakya or Naples feels like **mixed scrub and woodland**, not one uniform flower forest. In **Historical** mode, corn does not spawn in the Mediterranean and olives do not spawn in Iowa. In **Biome** mode, placement matches Materia’s climate logic but still on geographically correct **terrain**. Real-world `/tpll` coordinates remain accurate.
