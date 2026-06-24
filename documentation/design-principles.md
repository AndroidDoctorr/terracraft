# Design principles & resolved decisions

Product rules and answered design questions for Planet Earth. Complements [variation-smoothing-detail-plan.md](variation-smoothing-detail-plan.md).

---

## Ornamental and “close enough” placement

**Default stance:** If a plant is decorative (no meaningful functional or resource difference from a similar real-world stand-in) and **looks similar enough in Minecraft**, it is acceptable to place it outside its strict native range.

- Example: **wisteria** in the Mediterranean as ornamental — same block as East Asian wisteria; players can treat it as a regional look-alike.
- This applies **mod-wide** to vines, flowers, and other decorative flora — not to crops, ores, or items with distinct gameplay roles.
- **Historical** mode still gates **gameplay-relevant** plants (wild corn, flax, rubber, etc.) by geography. Decorative extras may use broader tags when the visual read is “close enough.”
- When adding bridge rules, prefer **low weight** or **sparse** placement for ornamental outliers so native-range plants dominate.

---

## Resolved open questions

| Question | Decision | Notes |
|----------|----------|-------|
| **Wisteria in Historical mode** | **Yes — ornamental Mediterranean OK** | Also native Indo-Malayan / East Asian ranges. Same block, different “pretend” species. |
| **Ore geo-gating** | **Future create-world UI option** | **Not now.** Vanilla Minecraft ore gen (or Materia’s global overworld rules if present) is fine until terrain, biomes, and flora are solid. Data source for deposit density maps TBD; tuning will take time. See [world-generation-options.md](world-generation-options.md#planned-options). |
| **Clone count** | **Prefer full archetype × realm cross product** | See below. |
| **Border blend width** | **Fixed default for now** (`ecoregion_border_blend_blocks`, default 64) | Config/datapack only; no create-world UI yet. Random width, distance functions, or per-edge tuning — **later**, after playtesting. |
| **Create-world UI (border blend)** | Deferred | Flora, map scale, and vertical scale shipped in 0.5.1; border blend stays config-only until needed. |

### Clone count — what the question meant

Planet Earth land biomes are **clones**: a vanilla archetype (forest, plains, jungle, …) plus **realm** tags (`_palearctic`, `_neotropical`, …) baked into each registry id.

Two approaches were considered:

| Approach | Description | Tradeoff |
|----------|-------------|----------|
| **Minimal set (~40 clones)** | Fewer ids; one clone carries multiple overlapping geo tags | Less registry bloat; harder to give each realm distinct surface/features without tag overlap |
| **Full cross product** | Separate clone per **archetype × realm** (e.g. `forest_palearctic`, `forest_neotropical`, …) | More biomes to maintain; **each cell gets correct tags and templates without compromise** — better long-term |

**Decision:** Target the **full cross product** as the stable model. New archetypes and realm-specific rules should assume distinct clone ids, not shared tags on a generic clone. The current ~44-clone set is a stepping stone toward that grid.

---

## Planned create-world options (not implemented)

| Control | Purpose | Status |
|---------|---------|--------|
| **Ore placement** | Geographic / density-aware ores vs global vanilla/Materia | **Deferred** — UI toggle when data + tuning exist |
| **Border blend** | Expose `ecoregion_border_blend_blocks` on World tab | **Deferred** — fixed default sufficient for now |

---

## Mission-aligned ideas (suggested, not yet in roadmap)

Doable with Minecraft worldgen logic; would support “real Earth that **feels** right” without OSM cities or full climate simulation.

### High leverage

| Idea | Why | Rough approach |
|------|-----|----------------|
| **Extend `/terracraft coords` (or F3)** | Exploration and debugging — tie terrain to real geography | Show WWF `ECO_NAME`, clone id, flora mode, horizontal scale |
| **Vanilla template geo-gating** | Pumpkin, cactus, sugar cane spawn globally via clone templates — top realism gap | Per-clone feature lists or Terracraft `remove_features` / replacement modifiers on `#terracraft:earth` |
| **Elevation + latitude snow line** | Andes / Himalaya alpine should differ from Arctic tundra at same Y | Adjust alpine override thresholds from lat + real elevation (meters), not fixed Y bands only |
| **Coast / basin riparian bias** | Floodplain and gallery forest without full hydrology | Low elevation + low slope + optional distance-to-coast noise → boost trees/grass/wet features on steppe and savanna edges |
| **Biome variant profiles (Phase 4)** | Single ecoregion polygons feel monotonous | Already planned — prioritize Mediterranean and temperate forest palettes first |

### Medium leverage

| Idea | Why | Rough approach |
|------|-----|----------------|
| **Rain-shadow heuristic** | Deserts and scrub east of mountains (e.g. Basin and Range) | Simple leeward mask: prevailing wind by hemisphere + elevation gradient from nearest high DEM |
| **Structure sanity for Earth** | Villages in wrong biomes break immersion | Reduce or disable vanilla structures on Planet Earth, or tag allow-lists per clone |
| **Horizontal scale → feature density** | At 1:4, one chunk covers more real area — wild crop patches feel too dense | Scale Materia/vanilla vegetation attempt counts by `horizontal_scale` |
| **Landmark spawn bias (optional)** | Quick “start near home” or famous places | Create-world optional spawn near lat/lon; never default without consent |
| **Minimap / map mod hooks** | Server tourism, education | Document API or debug packet for lat/lon + ecoregion name for Xaero’s, JourneyMap, etc. |

### Lower priority / needs data

| Idea | Why | Blocker |
|------|-----|---------|
| **Multi-scale elevation (Fourier / frequency split)** | `coastal_log` compresses large-scale relief but also squashes small features (Lake Merritt ~1 m, Grand Canyon walls at 1:4). Goal: compress **regional** trend while preserving **local** DEM contrast — e.g. treat each column's elevation as low-frequency baseline + high-frequency detail, compress only the baseline. | Needs profiling and tuning; not started |
| **Geology-aware ores** | Realistic tin/copper/iron belts | Deposit maps or proxies; pairs with future **Ore placement** UI option |
| **River network from DEM** | True riparian corridors | Derive channels from DEM flow accumulation — heavier than heuristics |
| **Cubic Chunks / taller worlds** | Extreme mountains | No stable 1.20.1 Forge CC; datapack height extension documented in [elevation-scaling.md](elevation-scaling.md) |

### Explicitly out of scope (unchanged)

Cities, roads, buildings (OSM), livestock breeds, regional mob replacements.

---

## Maintenance

When a resolved decision changes implementation (e.g. wisteria bridge tags, clone grid expansion), update:

1. This file (if the principle or decision changed)
2. [plant-distribution-reference.md](plant-distribution-reference.md)
3. [variation-smoothing-detail-plan.md](variation-smoothing-detail-plan.md) — keep in sync with **Resolved** table
