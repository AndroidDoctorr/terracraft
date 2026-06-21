# Plant distribution reference

Master reference for **Minecraft vanilla** and **Materia** vegetation on **Planet Earth** (Terracraft). Use this to check realism, tune the Materia bridge, and spot gaps.

Related: [world-generation-options.md](world-generation-options.md), [ecoregions-data.md](ecoregions-data.md), [variation-smoothing-detail-plan.md](variation-smoothing-detail-plan.md).

**Last synced with code:** Terracraft 0.5.1, Materia 1.2.0.

**Placement philosophy:** Gameplay-relevant plants (crops, rubber, etc.) follow geographic tags in Historical mode. **Decorative** plants with no distinct resource role may use broader “look-alike” placement — e.g. wisteria in the Mediterranean as ornamental. See [design-principles.md](design-principles.md).

---

## How to read this doc

| Column / term | Meaning |
|---------------|---------|
| **Vanilla default** | Where the block/feature spawns in a normal Minecraft world (or on Planet Earth via cloned biome JSON). |
| **Materia (vanilla world)** | Materia’s own `#materia:*` biome tags on **vanilla** biomes — global climate rules, not geographic. |
| **TC Historical** | Terracraft bridge when flora mode = **Historical** (`terracraft:*` geographic tags). Requires Materia. |
| **TC Biome** | Terracraft bridge when flora mode = **Biome** (`terracraft:climate_*` tags). Requires Materia. |
| **Real-world target** | Where the species is native or historically domesticated — design intent, not strict simulation. |
| **Gap** | Known mismatch or missing coverage. |

On Planet Earth, **land** uses Terracraft clone biomes (ecoregion-driven). **Ocean, beach, and alpine** bands use real vanilla biomes. Materia’s vanilla-world modifiers do **not** apply to Terracraft clone biomes unless the bridge adds them.

---

## Planet Earth biome archetypes

Each row is one **clone archetype** (many realm-specific IDs share the same vanilla template).

| Terracraft archetype | Vanilla template | Typical real-world placement (WWF / realm) | Vanilla trees & notable plants (from template) |
|----------------------|------------------|-----------------------------------------------|------------------------------------------------|
| `plains_*` | `plains` | Temperate grasslands, steppe edges (all realms) | Oak (sparse), tall grass, grass, mushrooms, sugar cane, pumpkin |
| `plains_*_bf` | `plains` | Same (biome-flora grassy archetype) | Same |
| `temperate_steppe` / `*_bf` | `plains` | Eurasian / North American steppe | Same |
| `forest_*` | `forest` | Broad temperate forest (realm-specific) | Oak, birch, forest flowers, grass, mushrooms, sugar cane, pumpkin |
| `forest_*_bf` | `forest` | Same (biome-flora) | Same |
| `taiga_*` | `taiga` | Boreal forest / southern tundra edge | Spruce, large fern, sweet berries, grass, mushrooms |
| `tundra_*` | `snowy_plains` | Arctic / alpine tundra | Spruce (sparse), grass, snow |
| `savanna_*` | `savanna` | Tropical / subtropical savanna | Acacia, oak, savanna grass, sugar cane, pumpkin |
| `jungle_*` | `jungle` | Humid tropical forest | Jungle trees, bamboo, vines, melon, grass, mushrooms |
| `tropical_dry_forest` | `sparse_jungle` | Tropical dry forest, woodland savanna | Sparse jungle trees, vines, melon (sparse), grass |
| `semi_arid_scrub` | `badlands` | Semi-desert, scrub, outback-style dry | Dead bush, cactus, badlands grass, mushroom, sugar cane |
| `chaparral_nearctic` | `badlands` | California / NW Mexico chaparral (tuned warm/dry) | Same as semi-arid (vanilla badlands set) |
| `mediterranean_scrub` | `savanna` (tuned) | Mediterranean basin, dry coasts | Acacia/oak savanna set — **not** true maquis/chaparral |
| `montane_meadow` | `meadow` | Mountain meadows, high plateaus | Meadow flowers, sparse trees, tall grass |
| `floodplain_meadow` | `swamp` | River floodplains, inland wetlands | Swamp oak, lily pad, seagrass, grass, mushrooms, sugar cane |
| `mangrove_coastal` | `mangrove_swamp` | Tropical/subtropical coasts | Mangrove, lily pad, seagrass, grass |
| *(elevation)* `BEACH`, `STONY_SHORE` | vanilla | Coasts worldwide | Minimal vegetation |
| *(elevation)* `GROVE` | vanilla | High elevation temperate | Spruce, azalea, glow lichen |
| *(elevation)* `SNOWY_SLOPES`, `STONY_PEAKS`, `JAGGED_PEAKS` | vanilla | High mountains, polar | Snow, stone, minimal plants |

Realm suffix on clones (`_palearctic`, `_neotropical`, `_afrotropical`, `_indomalayan`, `_australasian`, `_nearctic`) maps land to Earth via WWF ecoregion polygons — e.g. `forest_neotropical` ≈ Amazon / Central American forest, `forest_palearctic` ≈ Europe / N Asia.

---

## Master plant table

### Trees & woody plants

| Plant | Source | Category | Vanilla default | Materia (vanilla world) | TC Historical | TC Biome | Real-world target | Gap / notes |
|-------|--------|----------|-----------------|-------------------------|---------------|----------|-------------------|-------------|
| Oak | Minecraft | Tree | Plains, forest, savanna, swamp clones | — | Via clone templates | Via clone templates | Global temperate | Stands in for many broadleaf species |
| Birch | Minecraft | Tree | Forest clones | — | Same | Same | Northern temperate | No realm-specific birch vs oak mix |
| Spruce | Minecraft | Tree | Taiga, tundra, snowy clones; `GROVE` | — | Same | Same | Boreal / montane | — |
| Acacia | Minecraft | Tree | Savanna, Mediterranean scrub clones | — | Same | Same | African / Australian savanna | Also used for Mediterranean (wrong silhouette) |
| Jungle tree | Minecraft | Tree | Jungle clones | — | Same | Same | Humid tropics | — |
| Mangrove | Minecraft | Tree | `mangrove_coastal` | — | Same | Same | Tropical coasts | — |
| Azalea | Minecraft | Tree/shrub | `GROVE` (alpine override) | — | High mountains only | Same | Temperate mountains (incl. E Asia, Appalachians) | Not on Japanese `forest_*` lowlands — **cherry/maple gap** |
| Bamboo | Minecraft | Plant | Jungle clones | — | Same | Same | East / South Asia, tropics | Also in African jungle clones (acceptable) |
| **Olive** | Materia | Tree | — | `#materia:temperate` | `#terracraft:region_mediterranean` | `#terracraft:climate_temperate` | Mediterranean, Middle East | Also spawns all temperate clones in Biome mode |
| **Cypress** | Materia | Tree | — | `#materia:temperate`, `#materia:temperate_forest` | `#terracraft:region_mediterranean` | `climate_temperate`, `climate_temperate_forest` | Mediterranean, some temperate | Biome mode: wide temperate band |
| **Palm** | Materia | Tree | — | `#materia:beach`, `#materia:tropical` | `#terracraft:coastal_warm` | `#terracraft:climate_tropical` | Tropical/subtropical coasts | Historical tag is broad (incl. Mediterranean, all jungles) |
| **Baobab** | Materia | Tree | — | `#materia:tropical` | `#terracraft:region_afrotropical` | `#terracraft:climate_tropical` | Africa, Madagascar, NW Australia | Biome mode: all tropical clones (Americas, SE Asia) |
| **Rubber tree** | Materia | Tree | — | `#materia:tropical` | *(not placed)* | `#terracraft:climate_tropical` | Amazon, SE Asia | **Historical gap** — no bridge rule; only Biome mode |

### Crops & wild harvestables

| Plant | Source | Category | Vanilla default | Materia (vanilla world) | TC Historical | TC Biome | Real-world target | Gap / notes |
|-------|--------|----------|-----------------|-------------------------|---------------|----------|-------------------|-------------|
| Wheat, carrots, potatoes, beetroot | Minecraft | Crop | Villages, player farms only | — | — | — | Old World domesticates | Not world-gen |
| Pumpkin, melon | Minecraft | Crop | Patches in plains, forest, savanna, jungle, swamp, taiga, badlands clones | — | Same | Same | Americas (pumpkin/squash family) | Spawns globally via vanilla templates |
| Sugar cane | Minecraft | Crop | Many clones + beaches | — | Same | Same | Old World (S. Pacific origin) | Ubiquitous — unrealistic globally |
| Sweet berries | Minecraft | Bush | Taiga clones | — | Same | Same | Northern temperate | — |
| **Wild corn** | Materia | Crop | — | `#materia:grassy` (global) | `#terracraft:crop_new_world` | `#terracraft:climate_grassy` | Mesoamerica → Americas | Materia alone spawns in Europe; Historical fixes |
| **Wild peppers** | Materia | Crop | — | `#materia:grassy` | `#terracraft:crop_new_world` | `#terracraft:climate_grassy` | Americas | Same |
| **Wild beans** | Materia | Crop | — | `#materia:grassy` | `#terracraft:crop_new_world` | `#terracraft:climate_grassy` | Americas (domestication center) | Same |
| **Wild squash** | Materia | Crop | — | `#materia:grassy` | `#terracraft:crop_new_world` | `#terracraft:climate_grassy` | Americas | Same |
| **Wild cotton** | Materia | Crop | — | `#materia:grassy` | `#terracraft:crop_new_world` | `#terracraft:climate_grassy` | Americas, Africa, India, etc. | Historical: Americas only; real range wider |
| **Wild flax** | Materia | Crop | — | `#materia:grassy` | `#terracraft:crop_old_world` | `#terracraft:climate_grassy` | Europe, Middle East, N Africa | Old-world tag misses `plains_palearctic`, `temperate_steppe` |
| **Wild rice** | Materia | Crop | — | `#materia:river`, `#materia:warm_wet_surface` | `#terracraft:region_indomalayan` | `#terracraft:climate_river` | S / SE / East Asia, W Africa | Historical: whole Indomalayan realm; river tag = floodplain + mangrove + Indo jungle |
| **Tea bush** | Materia | Crop/bush | — | `#materia:temperate`, `#materia:temperate_forest` | `#terracraft:region_indomalayan` | `climate_temperate`, `climate_temperate_forest` | China, India, Japan, etc. | Biome mode: tea in Europe (`climate_temperate`) |
| **Esparto** | Materia | Grass | — | `#materia:desert`, `#materia:temperate` | `#terracraft:region_mediterranean` | `#terracraft:climate_temperate` | W Mediterranean, N Africa | Materia default very wide; Historical OK |
| **Indigo** | Materia | Dye plant | — | `#materia:tropical` | *(not placed)* | `#terracraft:climate_tropical` | Tropics/subtropics (Indigofera) | **Historical gap** |

### Vines & climbing plants

| Plant | Source | Category | Vanilla default | Materia (vanilla world) | TC Historical | TC Biome | Real-world target | Gap / notes |
|-------|--------|----------|-----------------|-------------------------|---------------|----------|-------------------|-------------|
| Vines (vanilla) | Minecraft | Vine | Jungle, sparse jungle clones | — | Same | Same | Tropics | — |
| Glow lichen | Minecraft | Lichen | Most clone templates | — | Same | Same | Ubiquitous | — |
| **Wild grape** | Materia | Vine | — | `#materia:temperate_forest` | `#terracraft:region_mediterranean` | `#terracraft:climate_temperate_forest` | Mediterranean, temperate | Historical: Med only; Biome: all forest clones |
| **Wild wisteria** | Materia | Vine | — | `#materia:temperate_forest` | `#terracraft:region_indomalayan` + `#terracraft:region_mediterranean` (ornamental) | `#terracraft:climate_temperate_forest` | East Asia; ornamental in Mediterranean OK | **Bridge gap** — planned tags above; Biome mode global temperate forest |
| **Wild hops** | Materia | Vine | — | *(no biome modifier in 1.20.1)* | *(not placed)* | *(not placed)* | Europe, W Asia, N America | **Not wired** — feature exists, no spawn rule |

### Grass, flowers & ground cover

| Plant | Source | Category | Vanilla default | Materia (vanilla world) | TC Historical | TC Biome | Real-world target | Gap / notes |
|-------|--------|----------|-----------------|-------------------------|---------------|----------|-------------------|-------------|
| Grass / tall grass | Minecraft | Grass | Nearly all land clones | — | Same | Same | Ubiquitous | — |
| Forest flowers | Minecraft | Flowers | Forest clones | — | Same | Same | Temperate mixed forest | Generic mix everywhere |
| Meadow flowers | Minecraft | Flowers | `montane_meadow` | — | Same | Same | Montane meadows | — |
| Large fern | Minecraft | Fern | Taiga clones | — | Same | Same | Boreal / wet temperate | — |
| Dead bush | Minecraft | Shrub | Badlands, semi-arid, chaparral | — | Same | Same | Arid regions | — |
| Cactus | Minecraft | Shrub | Badlands template (semi-arid, chaparral) | — | Same | Same | Americas arid | Spawns in non-American semi-arid clones |
| Mushrooms | Minecraft | Fungus | Most clones | — | Same | Same | Ubiquitous | — |
| Lily pad | Minecraft | Aquatic | Swamp, mangrove | — | Same | Same | Freshwater | — |
| Seagrass | Minecraft | Aquatic | Swamp, mangrove | — | Same | Same | Shallow seas | — |
| Kelp | Minecraft | Aquatic | Vanilla ocean biomes | — | Oceans | Oceans | Cold/temperate seas | — |
| **Esparto** | Materia | Grass | — | see Crops table | see Crops table | see Crops table | Mediterranean | — |

---

## Terracraft geographic tags (Historical mode)

Quick reference for which **clone biomes** receive Materia bridge features.

| Tag | Member biomes (summary) | Materia features |
|-----|-------------------------|------------------|
| `#terracraft:crop_new_world` | `chaparral_nearctic`, `forest_neotropical`, `plains_neotropical`, `savanna_neotropical` | Corn, peppers, beans, squash, cotton |
| `#terracraft:crop_old_world` | `forest_*` + `plains_*` in Palearctic, Afrotropical, Indomalayan, Australasian (not `_bf`) | Flax only |
| `#terracraft:region_mediterranean` | `mediterranean_scrub` | Olive, grape, esparto, cypress |
| `#terracraft:region_indomalayan` | All `_indomalayan` land clones (not `_bf`) | Rice, tea |
| `#terracraft:region_afrotropical` | All `_afrotropical` land clones (not `_bf`) | Baobab |
| `#terracraft:coastal_warm` | Mangrove, Mediterranean, all jungles & savannas (many realms) | Palm |

---

## Terracraft climate tags (Biome mode)

| Tag | Member biomes (summary) | Materia features |
|-----|-------------------------|------------------|
| `#terracraft:climate_grassy` | All `*_bf` grassy clones + `temperate_steppe_bf` | All wild crops (corn, peppers, beans, squash, flax, cotton) |
| `#terracraft:climate_temperate` | `taiga_palearctic`, `chaparral_nearctic`, `forest_palearctic_bf`, `forest_neotropical_bf` | Olive, esparto, cypress, tea |
| `#terracraft:climate_temperate_forest` | All `forest_*` clones | Grape, wisteria, cypress, tea |
| `#terracraft:climate_tropical` | Jungles, savannas, mangrove, `tropical_dry_forest` | Rubber, indigo, baobab, palm |
| `#terracraft:climate_river` | `floodplain_meadow`, `mangrove_coastal`, `jungle_indomalayan` | Rice |

---

## Materia `#materia:*` tags (vanilla worlds only)

For reference when **not** using Planet Earth, or when testing Materia alone.

| Tag | Vanilla biomes (summary) |
|-----|---------------------------|
| `#materia:grassy` | Plains, meadows, forests, savannas, jungles, `#is_forest`, `#is_savanna`, `#is_jungle` |
| `#materia:temperate` | Plains, meadows, forests, `#is_forest`, `#is_taiga` |
| `#materia:temperate_forest` | Forest variants, `#is_forest` |
| `#materia:tropical` | Jungles, savannas, `#is_jungle`, `#is_savanna` |
| `#materia:desert` | Desert, badlands |
| `#materia:beach` | Beach biomes |
| `#materia:river` | River biomes |
| `#materia:warm_wet_surface` | Jungle, savanna, mangrove |

---

## Known gaps (priority list)

Grouped by impact on “mostly consistent with reality” on Planet Earth **Historical** mode.

### High — wrong or missing native placement

| Issue | Detail |
|-------|--------|
| **Vanilla template bleed** | Pumpkin, melon, sugar cane, cactus, acacia/Oak “Mediterranean” follow clone **template**, not geography — global patches where the template allows. |
| **Wild hops** | Materia block exists; **no** biome modifier on 1.20.1; Terracraft bridge does not add it. Target: Europe, W Asia, N America temperate forest. |
| **Historical rubber & indigo** | Only in Biome mode (`climate_tropical`). Amazon / SE Asia rubber and tropical indigo missing in Historical. |
| **Historical wisteria** | Bridge not wired yet — target Indo-Malayan + ornamental Mediterranean ([design-principles.md](design-principles.md)). |
| **Eastern NA forest identity** | Temperate forest clones use generic oak/birch — no red maple, larch, or other regional trees. |
| **Mediterranean biome feel** | `mediterranean_scrub` uses **savanna** template (acacia) — wrong aesthetic vs olive/cypress/esparto Materia layer. |
| **Flax old-world tag** | `crop_old_world` omits `plains_palearctic`, `temperate_steppe`, `plains_indomalayan` — flax under-spawns in obvious Old World grasslands. |

### Medium — broad tags / Biome mode realism

| Issue | Detail |
|-------|--------|
| **Biome mode tea & olive** | `climate_temperate` includes European and American forest — tea/olive spawn outside native range. |
| **Biome mode baobab & rubber** | All `climate_tropical` clones — baobabs in Neotropical jungle, rubber outside Amazon if Biome mode. |
| **Cotton Historical** | Only New World tag; real cotton diversity includes Africa and S Asia. |
| **Palm `coastal_warm`** | Very wide (all savannas, jungles, Mediterranean) — may over-spawn inland. |
| **Rice Historical** | Whole Indomalayan realm — rice in Indian desert scrub clones if those existed; river tag narrower for Biome mode. |

### Lower — future content

| Issue | Detail |
|-------|--------|
| **Cherry, dark oak, mangrove alternatives** | No Japanese cherry, N American maple, cork oak, date palm, etc. |
| **Date palm / coconut distinction** | Single Materia palm for all warm coasts. |
| **Australasia** | Few unique species (eucalyptus, acacia as wattle) — hardest floristic gap. |
| **Crops not in Materia** | Tobacco, potato, tomato, sunflower, etc. — not in mod yet. |
| **Transition biomes** | Border spillover helps; no dedicated scrub/edge biomes. |

---

## Suggested next bridge additions (Historical)

| Plant | Suggested tag / region |
|-------|------------------------|
| Wild hops | `#terracraft:region_palearctic` temperate forest + `#terracraft:crop_new_world` forest (N America) — **new tag** |
| Rubber tree | `#terracraft:region_neotropical` jungle + `#terracraft:region_indomalayan` jungle |
| Indigo | Tropical/subtropical clones by realm, not global Biome tag |
| Wild wisteria | `#terracraft:region_indomalayan` + `#terracraft:region_palearctic` (China/Japan) + `#terracraft:region_mediterranean` (ornamental, sparse) |
| Flax | Expand `#terracraft:crop_old_world` to include `plains_palearctic`, `temperate_steppe`, `plains_indomalayan` |

---

## Maintenance

When adding Materia plants or Terracraft tags, update:

1. This file (master table + gap list).
2. `tools/generate_terracraft_biomes.ps1` (tag membership).
3. `data/forge/biome_modifier/terracraft_*.json` (bridge rules).
4. [world-generation-options.md](world-generation-options.md) if UI/presets change.

Source of truth for tag membership: generated JSON under `data/terracraft/tags/worldgen/biome/`.
