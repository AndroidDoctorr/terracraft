# Biome flora inventory (Planet Earth)

Checklist for **what should spawn** in each Terracraft clone biome on Planet Earth. Use with `/terracraft coords` to see which biome you are standing in, then compare to this list.

**Requires Materia** for regional crops/trees (olive, cypress, etc.). Without Materia you only get the **Vanilla template** column.

**Flora mode** (create-world UI):

| Mode | Materia column to use |
|------|------------------------|
| **Historical** (default) | + Historical Materia |
| **Biome** | + Biome Materia |

Realm suffixes (`_palearctic`, `_neotropical`, …) share the same **vanilla template** unless noted. `_bf` clones use the **same template** as their non-`_bf` twin but get extra **Biome-mode** grassy crops.

Related: [plant-distribution-reference.md](plant-distribution-reference.md) (species-level gaps), [design-principles.md](design-principles.md).

---

## Quick read: Antioch (mediterranean_scrub)

`/tpll 36.2017 36.1600` → usually `terracraft:mediterranean_scrub`.

| What you see | Why |
|--------------|-----|
| **Oak trees** | Vanilla oak stand-in for holm oak (`flora_oak_tree`) |
| **Spruce** | Vanilla spruce stand-in for Aleppo pine (`flora_spruce_tree`) |
| **Olive / cypress** | Materia trees baked into biome JSON |
| **“Vines” on trunks** | Glow lichen (vanilla) + Materia wild grape |
| **Esparto / flax** | Materia ground cover for Old World scrub |

Regional flora is now **baked into each clone biome JSON** via `tools/generate_terracraft_biomes.ps1` (not Forge biome modifiers). Historical-mode Forge modifiers are disabled (`forge:none`).

---

## Archetype summary (all 55 clones roll up to these)

| Archetype | Example clone IDs | Vanilla template | Trees & woody (vanilla) | Ground / other (vanilla) |
|-----------|-------------------|------------------|-------------------------|---------------------------|
| **mediterranean_scrub** | `mediterranean_scrub` | savanna | Oak, acacia | Tall grass, savanna grass, warm flowers, glow lichen, mushrooms, sugar cane, pumpkin |
| **temperate_steppe** | `temperate_steppe`, `temperate_steppe_bf` | plains | Oak (sparse) | Tall grass, plains grass, plains flowers, glow lichen, mushrooms, sugar cane, pumpkin |
| **plains_*** | `plains_palearctic`, `plains_neotropical`, … + `*_bf` | plains | Oak (sparse) | Same as steppe |
| **forest_*** | `forest_palearctic`, `forest_neotropical`, … + `*_bf` | forest | Oak, birch | Forest flowers, grass, glow lichen, mushrooms, sugar cane, pumpkin |
| **taiga_*** | `taiga_palearctic`, `taiga_nearctic`, … | taiga | Spruce | Large fern, grass, taiga mushrooms, sweet berries, sugar cane, pumpkin |
| **tundra_*** | `tundra_palearctic`, … | snowy_plains | Spruce (sparse, snowy) | Grass (patch), flowers, glow lichen, mushrooms, sugar cane, pumpkin |
| **savanna_*** | `savanna_afrotropical`, … | savanna | Oak, acacia | Same as mediterranean_scrub |
| **jungle_*** | `jungle_indomalayan`, … | jungle | Jungle trees | Bamboo, **vines**, melon, warm flowers, jungle grass, mushrooms, sugar cane, pumpkin |
| **tropical_dry_forest** | `tropical_dry_forest` | sparse_jungle | Sparse jungle trees | Vines, sparse melon, warm flowers, jungle grass, mushrooms, sugar cane, pumpkin |
| **semi_arid_scrub** | `semi_arid_scrub` | badlands | — (no trees) | Dead bush, badlands grass, cactus, glow lichen, mushrooms, sugar cane, pumpkin |
| **chaparral_nearctic** | `chaparral_nearctic` | badlands | — | Same as semi_arid (tuned warm/dry) |
| **montane_meadow** | `montane_meadow` | meadow | Meadow trees (sparse oak/birch) | Meadow flowers, tall grass, grass, glow lichen |
| **floodplain_meadow** | `floodplain_meadow` | swamp | **Swamp oak** (viney oaks) | Lily pads, seagrass, swamp flowers, grass, dead bush, mushrooms, sugar cane, pumpkin |
| **mangrove_coastal** | `mangrove_coastal` | mangrove_swamp | **Mangrove** | Lily pads, seagrass, grass, dead bush, glow lichen |

**Elevation overrides (vanilla biomes, not Terracraft clones):** beach/stony shore (bare), grove (spruce + azalea), snowy slopes / peaks (minimal).

---

## Regional flora (all archetypes)

Flora is assigned per **template archetype + realm suffix** in `Get-FloraFeatures()` inside `tools/generate_terracraft_biomes.ps1`. Placed features live under `terracraft:flora_*`.

| Archetype | Realm / notes | Materia + stand-ins added |
|-----------|---------------|---------------------------|
| **mediterranean_scrub** | — | Flax, esparto, oak, spruce, olive, cypress, grape |
| **plains / steppe** | Old World | Flax |
| **plains / steppe** | New World | Corn, peppers, beans, squash, cotton |
| **plains / steppe** | Nearctic | + maple |
| **plains / steppe** | all | Sparse oak |
| **forest** | Old World | Flax, grape |
| **forest** | New World | New World crops, maple |
| **forest** | Indomalayan | Tea, wisteria, rubber |
| **forest** | Neotropical | Rubber, indigo |
| **forest** | Palearctic | Birch |
| **forest** | Afrotropical | Baobab |
| **savanna** | all | Acacia supplement |
| **savanna** | Afrotropical | Baobab |
| **savanna** | coastal warm | Palm |
| **savanna** | New World | New World crops; Old World flax |
| **jungle** | Indomalayan | Rice, tea, rubber, wisteria |
| **jungle** | Neotropical / Nearctic | Rubber, indigo |
| **jungle** | Afrotropical | Baobab, rubber |
| **jungle / savanna / mangrove** | coastal warm | Palm |
| **sparse_jungle** | tropical dry | Sparse oak; rubber/indigo in Neotropical/Afrotropical/Australasian |
| **taiga / tundra** | Old World taiga | Flax + spruce |
| **taiga / tundra** | all | Spruce |
| **meadow** | Old World | Flax, sparse oak, birch |
| **swamp / floodplain** | Indomalayan | Wild rice |
| **swamp** | Old World | Flax, sparse oak |
| **mangrove** | tropical | Palm, wild rice |
| **badlands / chaparral** | Nearctic chaparral | Grape, corn, peppers, sparse oak |
| **semi_arid_scrub** | — | Sparse oak only |

**Vanilla stand-ins:** oak ≈ holm oak / scrub oak; spruce ≈ pine; birch ≈ temperate broadleaf; acacia ≈ savanna woodland.

### Historical mode (legacy tags — modifiers disabled)

Forge biome modifiers under `data/terracraft/forge/biome_modifier/` are set to `forge:none`. Flora above replaces the old tag-based injection.

---

## Biome-by-biome checklist

Use **Y** / **N** / **sparse** when playtesting. “Vanilla” = template column above + Materia for your flora mode.

### Mediterranean & dry temperate

#### `terracraft:mediterranean_scrub`
- **Real-world examples:** Antakya, coastal Turkey, Levant scrub, S California-like ecoregions mapped to WWF Mediterranean codes
- **Vanilla ground:** Tall grass, savanna grass, warm flowers, mushrooms, glow lichen
- **Vanilla trees (stand-ins):** Oak (holm oak), spruce (Aleppo pine)
- **Materia (biome JSON):** Olive, cypress, esparto, wild grape vine, wild flax
- **Should *not* have (Historical):** Corn, baobab, jungle vines, swamp oaks, acacia, palm

#### `terracraft:temperate_steppe` / `temperate_steppe_bf`
- **Examples:** Kazakh steppe, Great Plains edges (if mapped)
- **Vanilla:** Sparse oak, plains grass/flowers, tall grass, pumpkin, sugar cane
- **Historical Materia:** Flax *(only if `plains_palearctic`-class — steppe **not** in `crop_old_world` tag — gap)*
- **Biome Materia (`_bf`):** Full grassy crop set
- **Should feel:** Open grassland, few trees

#### `terracraft:semi_arid_scrub`
- **Examples:** Outback, Sahel edge, SW US desert scrub
- **Vanilla:** Dead bush, cactus, badlands grass — **no trees**
- **Historical:** Baobab if `_afrotropical` realm mapping; palm if near coast in `coastal_warm`
- **Should feel:** Dry, shrubby, no forest

#### `terracraft:chaparral_nearctic`
- **Examples:** California chaparral
- **Vanilla:** Same as semi_arid (badlands template)
- **Historical Materia:** New World crops; olive/cypress/tea only in **Biome** mode (`climate_temperate`)
- **Gap:** Badlands template ≠ chaparral; no unique scrub oak/manzanita

---

### Forest & woodland (by realm)

#### `terracraft:forest_palearctic` (+ `_bf`)
- **Examples:** European temperate forest, N Asian broadleaf
- **Vanilla:** Oak, birch, forest flowers, grass, pumpkin, sugar cane
- **Historical:** Flax (`crop_old_world`); wisteria **not wired**
- **Biome (`_bf`):** Grassy crops; `climate_temperate_forest` → grape, wisteria, cypress, tea

#### `terracraft:forest_nearctic` (+ `_bf`)
- **Examples:** Eastern US, eastern Canada
- **Historical:** New World crops; flax **no** (not in crop_old_world)
- **Gap:** Generic oak/birch only — no maple/larch identity

#### `terracraft:forest_neotropical` (+ `_bf`)
- **Examples:** Atlantic Forest, Mesoamerican foothills
- **Historical:** New World crops (corn, etc.)

#### `terracraft:forest_afrotropical` (+ `_bf`)
- **Historical:** Flax + baobab (whole-realm baobab tag — can feel odd in forest)

#### `terracraft:forest_indomalayan` (+ `_bf`)
- **Historical:** Flax, rice, tea

#### `terracraft:forest_australasian` (+ `_bf`)
- **Historical:** Flax only

---

### Plains & savanna

#### `terracraft:plains_*` (all realms, + `_bf`)
- **Vanilla:** Sparse oak, plains vegetation, pumpkin, sugar cane globally (template bleed)
- **Historical crops:** New World tags for Nearctic/Neotropical; Old World flax for Palearctic/Afrotropical/Indomalayan/Australasian — **not** `plains_palearctic` in flax tag (gap)

#### `terracraft:savanna_*` (all realms)
- **Vanilla:** Same as `mediterranean_scrub` (oak, acacia, savanna grass)
- **Historical:** Palms (`coastal_warm`); baobab in Afrotropical; New World crops in Neotropical/Nearctic

---

### Boreal, tundra, mountains

#### `terracraft:taiga_*`
- **Vanilla:** Spruce, large fern, sweet berries
- **Historical:** Realm-specific (baobab in Afrotropical only, etc.)

#### `terracraft:tundra_*`
- **Vanilla:** Sparse spruce on snowy plains, minimal undergrowth
- **Should feel:** Tree line, mostly open

#### `terracraft:montane_meadow`
- **Vanilla:** Meadow flowers, sparse trees, tall grass
- **Should feel:** Alpine/subalpine openings, not dense forest

---

### Wet & tropical

#### `terracraft:floodplain_meadow`
- **Examples:** River valleys, inland deltas
- **Vanilla:** **Swamp oak** (often vine-covered), lily pads, seagrass — strongest “swamp oak + vines” look
- **Historical/Biome:** Rice (Indomalayan / river tag)
- **Note:** If Antioch looked swampy, confirm biome with `/terracraft coords` — true floodplain uses **swamp** template, not savanna

#### `terracraft:mangrove_coastal`
- **Vanilla:** Mangrove, lily pads, seagrass
- **Historical/Biome:** Palm + rice (Biome river)

#### `terracraft:jungle_*`
- **Vanilla:** Jungle trees, bamboo, **vines**, melon
- **Historical:** Palm; baobab (Afrotropical); rubber/indigo **Biome only**

#### `terracraft:tropical_dry_forest`
- **Vanilla:** Sparse jungle trees, vines, sparse melon
- **Biome:** Full tropical Materia set (rubber, indigo, baobab, palm)

---

## Suggested playtest coordinates

| Location | `/tpll` | Expected biome | What to check |
|----------|---------|----------------|---------------|
| Antakya | `36.2017 36.1600` | `mediterranean_scrub` | Olive/cypress vs oak; lichen “vines” |
| Lake Merritt | `37.8014 -122.2585` | nearctic forest/plains | Oak/birch mix, New World crops if plains/forest |
| Governor’s Island | `40.689 -74.016` | forest/plains nearctic | Tree density, no palms |
| Grand Canyon | `36.066 -112.117` | semi_arid or chaparral | Should be **no** dense oak forest |
| Naples | `40.8518 14.2681` | `mediterranean_scrub` | Same checklist as Antioch |
| Iowa | `41.586 -93.625` | plains_nearctic | Sparse oak, corn **if** on crop tag biome |

---

## Density expectations (rough)

Vanilla placed features use **low per-chunk attempt counts** — large homogeneous clones feel repetitive before variants exist.

| Feature type | Typical feel |
|--------------|--------------|
| `trees_plains` | Very sparse trees |
| `trees_savanna` / `trees_forest` | Moderate patches |
| `trees_jungle` / `trees_swamp` | Dense patches |
| Materia trees (olive, cypress, baobab) | Sparse extras on top of vanilla — easy to overlook |
| Glow lichen | Common on tree trunks — reads as “vines” |

If a biome feels **empty**: check tree feature exists for template (semi_arid has none). If it feels **monotonous**: expected until Phase 4 variant profiles.

---

## Maintenance

When changing templates or flora assignments, re-run `tools/generate_terracraft_biomes.ps1` and update this file.

Source files:

- Flora builder: `tools/build_flora_placed_features.ps1`
- Biome generator + flora rules: `tools/generate_terracraft_biomes.ps1` (`Get-FloraFeatures`, `$FloraGlobalOrder`)
- Templates: `tools/biome_templates/*.json` (vegetal step = index 9)
- Generated biomes: `1.20.1/src/main/resources/data/terracraft/worldgen/biome/`
- Placed features: `1.20.1/src/main/resources/data/terracraft/worldgen/placed_feature/flora_*.json`
- Legacy modifiers (disabled): `1.20.1/src/main/resources/data/terracraft/forge/biome_modifier/terracraft_*.json`
