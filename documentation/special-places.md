# Special places catalog

Places where **ecoregion polygons + DEM alone** are not enough — they need curated biome zones, directional surface spill, custom vegetation, or (eventually) landmark surface rules. Use this as a backlog for `/tpll` playtests and GeoJSON/OSM imports.

**Pipeline today:** `regional/biome_zones/*.geojson` → `RegionalBiomeZones` → `EcoregionBiomeOverrides` → `ChunkSurfaceSpillField` / `BiomeSurfaceRules`. See [regional-biome-zones.md](regional-biome-zones.md) and [landmark-features-plan.md](landmark-features-plan.md).

**Legend**

| Tag | Meaning |
|-----|---------|
| **Shipped** | Bundled zone or spill logic in the mod today |
| **Started** | Partial treatment; needs polish or expansion |
| **Planned** | On the list; no dedicated work yet |
| **Idea** | Worth doing; needs scoping or reference gathering |

**Effort (rough)**

| Level | Typical work |
|-------|----------------|
| **S** | GeoJSON polygon + biome pin + test coords |
| **M** | Surface spill palette, vegetation tuning, elevation caps |
| **L** | Multi-zone system, seasonal logic, custom blocks/features, structures |

---

## Priority (distinct / famous / weird vs default generation)

Tier **A** — do first; wrong without curation, lots of people will `/tpll` here:

| Place | Why tier A |
|-------|------------|
| Bonneville / playa family | Salt + sand spill; WWF lumps with scrub |
| Nile Delta / Cairo edge | Desert ↔ gallery forest; hard borders |
| Sedona / Monument Valley | Badlands surfaces; not semi-arid grass |
| Sahara / Arabian Empty Quarter | Hyper-arid; raster bleeds savanna/trees |
| Grand Canyon | Rim vs river corridor vegetation |
| Dover chalk cliffs | Iconic white cliff geology |
| Salar de Uyuni | Second playa template; famous |

Tier **B** — famous or visually distinctive:

Chicago / NYC / Bay Area coasts, Niagara / Angel Falls, Iceland volcanics, Australian shield mesas, White Sands, Pamukkale, Wadi Rum, Okavango, Vesuvius / Hawaii volcanoes.

Tier **C** — long tail (still worth it): everything else in this doc.

---

## Where the data comes from (you don't hand-draw Earth)

You should **not** be the sole author of a global biome map. Stack sources by scale:

| Scale | Source | Use for |
|-------|--------|---------|
| **Global coarse** | WWF Terrestrial Ecoregions (already in mod) | Default biome clone pins — 867 regions, ~14 BIOME codes (too coarse alone) |
| **Regional polygons** | [RESOLVE Ecoregions](https://ecoregions.apps.allenai.org/), EPA Level III/IV (US), Copernicus LC100 | Finer ecological polygons; export GeoJSON |
| **Land cover / soil** | ESA WorldCover, OpenLandMap soil/texture, USGS NLCD | Sand vs rock vs salt vs forest *surfaces* inside polygons |
| **Named features** | **OpenStreetMap** Overpass (`natural=salt_pan`, `wetland`, relations) | Playas, deltas, parks — `tools/import_osm_biome_zone.ps1` |
| **US public lands** | ArcGIS Living Atlas, BLM, NPS boundaries | Bonneville-scale playas, monuments |
| **Elevation** | Copernicus DEM / SRTM (already in mod) | Spill direction, riparian corridors, volcano peaks |

**Workflow:** WWF picks the *region*, OSM/ArcGIS draws the *feature polygon*, RESOLVE or land-cover refines *vegetation*, DEM drives *spill/slope*. Drop polygons in `.minecraft/terracraft/data/regional/biome_zones/` — no Java required for new sites once spill rules exist.

Google Maps polygons are not directly exportable; trace from satellite into OSM or QGIS, or find an existing OSM relation.

---

## Your list (consolidated)

| Place | Region (approx.) | What to replicate | Tags | Effort |
|-------|------------------|-------------------|------|--------|
| **Sedona red rock** | 34.87°N, 111.76°W | Terracotta / red sand spill from mesa rims into desert scrub | Shipped | M |
| **Dover white cliffs** | 51.13°N, 1.34°E | Chalk faces above Channel; cliff band by slope + lat/lon box | Planned | M |
| **Salt flats (family)** | See [Salt flats](#salt-flats-playas) below | Flat white salt, sand aprons, no vegetation; Materia `salt_block` | Started | M–L |
| **Nile Delta** | ~30.5°N, 31.2°E | Sharp desert ↔ gallery forest edge; sand fade east, floodplain west | Started | M |
| **Chicago coast** | 41.89°N, 87.61°W | Lake Michigan beach, dunes, marsh pockets; urban skipped | Planned | M |
| **NYC coast** | 40.71°N, 74.01°W | Harbor, sandy outer barrier islands, pre-colonial marsh/forest mix | Planned | M–L |
| **Bay Area coast** | 37.8°N, 122.4°W | Fog belt, chaparral hills, bay mudflats, redwood fog drip (north) | Planned | M–L |
| **Rainbow eucalyptus** | Hawaii, Mindanao, etc. | Terracraft easter-egg patches; multicolor bark (not Materia crop) | Idea | L |
| **Niagara Falls** | 43.08°N, 79.07°W | High-flow waterfall on existing gorge topography | Idea | L |
| **Angel Falls** | 5.97°N, 62.54°W | Tepui cliff + world's highest free-fall (table mountain rim) | Idea | L |
| **Australian mesa country** | See [Australian shield](#australian-shield-mesas) | Flat topped ranges, red ironstone, sparse mulga, sudden escarpments | Planned | M–L |
| **Volcanoes (family)** | See [Volcanic](#volcanic--geothermal) | Basalt, tuff, obsidian, ash; Iceland rift + Hawaii shield | Idea | L |

---

## Salt flats (playas)

Same *gameplay* biome (`playa_salt`) can cover many real sites; each needs its **own polygon** and often different elevation caps / sand spill width.

| Site | Location | Notes | Status |
|------|----------|-------|--------|
| **Bonneville** | Utah, USA | Reference implementation; downhill sand spill from mountains | Shipped |
| **Great Salt Lake west desert** | Utah | Mud/salt mix north of I-80; separate OSM polygon from Bonneville | Planned |
| **Salar de Uyuni** | Bolivia | Vast flat white; cactus islands (Incahuasi); altiplano ring | Planned |
| **Chott el Djerid** | Tunisia | Sahara edge playa; erg ↔ salt transition | Planned |
| **Lake Eyre / Kati Thanda** | Australia | Ephemeral salt lake; red desert spill in | Planned |
| **Rann of Kutch** | Gujarat, India | Seasonal wet/dry — future: wet pass vs salt pan | Idea |
| **Tuz Gölü** | Central Turkey | Large shallow salt lake; semi-arid surround | Planned |
| **Ustyurt / Aral margins** | Uzbekistan, Kazakhstan | Cold desert + salt crust; flat steppe edge | Idea |
| **Dasht-e Kavir / Lut** | Iran | Hyper-arid; white salt / dark lava margins in Lut | Idea |
| **Badwater Basin** | Death Valley | Lowest US elevation; salt polygon below sea level | Planned |
| **Salton Sea** | California | Accidental sea; salty shore, not classic playa | Idea |

**Replication hooks:** `playa_salt` biome, geographic spill (salt only in deepest basin), OSM `natural=salt_pan`, `max_elevation_m` to exclude adjacent peaks.

---

## Coasts & lakefronts

Urban build style is out of scope; **natural** pre-settlement coast matters.

| Place | Why it's special | Replication hooks |
|-------|------------------|-------------------|
| **Chicago / Lake Michigan** | Long sandy beach, dune ridges, inland marsh and oak openings | Shoreline bands, `floodplain_meadow` pockets, winter-hardy lakeshore flora |
| **NYC / Long Island / NJ barrier** | Outer beaches vs inner estuary mud vs Hudson highlands forest | Multiple tight polygons; strong biome contrast over short distance |
| **San Francisco Bay** | Golden Gate fog, serpentine/chaparral hills, tidal mud, kelp coast | `chaparral_nearctic`, salt marsh surface, optional redwood belt north |
| **Outer Banks / Cape Cod** | Thin barrier islands, dune grass, pine behind dunes | Low elevation cap, sand spill from ocean side only |
| **Miami / Florida Keys** | Limestone, mangrove, reef shelf | `mangrove_coastal`, warm shallow water tint |
| **Norwegian fjords** | Steep walls, deep water, birch treeline | Mostly DEM; optional cliff grass + snowline |
| **Cliffs of Moher** | Atlantic chalk/sandstone vertical coast | Dover-like surface rule on west Ireland box |
| **Twelve Apostles** | Limestone stacks | Structure/schematic fantasy; low priority |
| **Great Barrier Reef edge** | Turquoise shelf, dry mainland behind | Water color + seagrass; `mangrove_coastal` / savanna inland |

---

## Rivers, deltas & wetlands

| Place | Why it's special | Replication hooks |
|-------|------------------|-------------------|
| **Nile Delta** | Desert plateau ↔ irrigated green delta; Cairo west desert margin | Shipped spill + `cairo_western_desert`; widen delta polygon |
| **Okavango Delta** | Inland delta that never reaches the sea | Basin depression + permanent water grid; papyrus/reeds |
| **Pantanal** | Seasonal flood savanna | Riparian strength + wetland variants |
| **Everglades** | River of grass, limestone shelf | Flat saw-grass biome, cypress dome patches |
| **Mekong Delta** | Rice/wet palm mosaic | `floodplain_meadow` + palm features |
| **Amazon confluence** | “Meeting of waters” color line | Ambitious: two water tints by river polygon |
| **Mississippi Delta** | Mud mouth, marsh, chenier ridges | Louisiana marsh + gulf sand spill |
| **Iguazu** | Wide curtain falls in subtropical forest | Falls structure + `jungle_neotropical` |

---

## Waterfalls

Topography often works; **water placement** and mist/atmosphere are the gap.

| Falls | Test coords (approx.) | Notes |
|-------|----------------------|-------|
| **Niagara** | 43.08°N, 79.07°W | Horseshoe + American; gorge already in DEM |
| **Angel Falls** | 5.97°N, 62.54°W | Tepui plateau edge; thin stream off huge cliff |
| **Victoria Falls** | 17.92°S, 25.86°E | Basalt gorge, rainforest spray belt |
| **Yosemite Falls** | 37.77°N, 119.59°W | Hanging valley; seasonal flow tiers |
| **Plitvice** | 44.88°N, 15.62°E | Travertine cascade lakes (turquoise pools) |
| **Gullfoss** | 64.33°N, 20.12°W | Iceland canyon slot falls |

---

## Red rock, mesas & painted deserts

| Place | Location | What to replicate | Status |
|-------|----------|-------------------|--------|
| **Sedona** | Arizona | Terracotta spill → sand → scrub | Shipped |
| **Grand Canyon** | Arizona | Rim pine vs inner desert; already strong DEM | Started |
| **Monument Valley** | Utah/Arizona | Buttes, flat floor, sparse sage | Planned |
| **Antelope Canyon** | Arizona | Slot canyon — narrow depth pass (hard) | Idea |
| **Bryce Canyon** | Utah | Hoodoo amphitheater — eroded pillars | Idea |
| **Zhangye Danxia** | China | Rainbow striped hills — layered terracotta | Idea |
| **Bungle Bungles** | Australia | Beehive banded domes | Idea |
| **Wave / Coyote Buttes** | Arizona | Cross-bedded sandstone ripples | Idea |
| **Valley of Fire** | Nevada | Red Aztec sandstone | Planned |
| **Wadi Rum** | Jordan | Sandstone towers + red sand desert | Planned |

**Shared pattern:** `ChunkSurfaceSpillField` mesa spill (high relief emits rock palette, bleeds downhill).

---

## Australian shield & mesas

| Place | Why it's special | Replication hooks |
|-------|------------------|-------------------|
| **Uluru / Kata Tjuta** | Monoliths, red iron oxide, spinifex | Red sandstone surface + `semi_arid_scrub` / esparto-like spinifex |
| **Kimberley plateau** | Flat top, sudden escarpments, boab gorges | Escarpment spill; `baobab` / dry savanna |
| **Hamersley Range** | Band iron formations | Layered red/grey bands on slopes |
| **Nullarbor** | Treeless karst plain | Flat limestone surface, zero trees variant |
| **Flinders Ranges** | Folded red ranges above flat outback | Mesa spill + semi-arid |
| **MacDonnell Ranges** | West-east ridges, gorges | Similar to Sedona logic in southern hemisphere |

---

## Volcanic & geothermal

| Place | Why it's special | Block palette |
|-------|------------------|---------------|
| **Iceland** | Rift valley, basalt columns, moss lava fields, hot springs | Basalt, tuff, obsidian, mossy blocks; low trees |
| **Hawaii** | Shield volcanoes, rain forest windward, lava fields leeward | Basalt + jungle + coastal palm |
| **Yellowstone** | Geysers, silica sinter, lodgepole pine | Future hot spring blocks; taiga |
| **Mount Etna / Vesuvius** | Mediterranean volcano flank | Basalt + `mediterranean_scrub` |
| **Mount Fuji** | Symmetric cone above flat plain | DEM often good; snow cap + forest belt |
| **Atacama + Altiplano** | Driest desert, salt, lichen, geoglyphs | `desert_arid` + playa + very sparse flora |
| **Giant's Causeway** | Columnar basalt coast | Basalt pillars on north Ireland polygon |
| **Deception Island** | Antarctic volcano caldera | Niche; snow + ash + steam |

**Replication hooks:** elevation + slope volcanic source in spill field; geo boxes for “recent lava” = basalt surface; optional structure for caldera rim.

---

## Unique forests & vegetation patches

| Place | Why it's special | Scope |
|-------|------------------|-------|
| **Rainbow eucalyptus** | Ornamental multicolor bark | Terracraft easter egg; small patches only |
| **Socotra** | Dragon blood trees, bottle trees | Afrotropical + weird sparse trees (Materia?) |
| **Baobab alley** | Madagascar | Row of baobabs — placed feature hot spot |
| **Redwood / sequoia belt** | California coast range | Tall trees fog belt north of Bay Area |
| **Joshua Tree** | Mojave | Yucca/joshua patches in desert |
| **Bamboo sea** | Sichuan etc. | Dense bamboo biome variant |
| **Deadvlei** | Namibia | White pan + dead acacia + orange dunes behind |
| **Olive / cork montados** | Iberia | Already partly `mediterranean_scrub` |
| **Laurisilva** | Madeira, Azores | Cloud forest laurel — warm wet evergreen |

---

## Chalk, limestone & white landscapes

| Place | Why it's special | Surface |
|-------|------------------|---------|
| **White Cliffs of Dover** | Chalk English Channel | White concrete powder / calcite on cliff slopes |
| **Pamukkale** | Turkey | White travertine terraces + blue pools |
| **Karst China (Guilin)** | Limestone towers | DEM pillars + bamboo; classic karst |
| **Tsingy de Bemaraha** | Madagascar | Sharp limestone needles | Idea |
| **White Sands** | New Mexico | Gypsum dunes — white sand, not playa salt |

---

## Human-scale landmarks (optional, later)

Recognizable **without** rebuilding cities — geology + silhouette only.

| Site | Approach |
|------|----------|
| **Giza pyramids** | Desert plateau polygon | Shipped |
| **Machu Picchu** | Andean terrace + cloud forest edge |
| **Banaue rice terraces** | Slope stepped surface illusion (hard) |
| **Petra** | Sandstone gorge + facade schematics |
| **Stonehenge** | Salisbury plain chalk grass |
| **Easter Island** | Coastal grass + sparse statues (easter egg) |

---

## Already strong from DEM alone (lower priority)

These often look good with topography only; note for regression tests, not heavy curation:

- Grand Canyon, Banff/Rockies, Alps, Himalaya foothills, Norwegian fjords (depth), Scottish Highlands, Patagonian peaks, Ethiopian Simien escarpments, Zhangjiajie pillars (if DEM resolution holds).

---

## Suggested next tranche (after current playtest fixes)

1. **Dover chalk** — small polygon + cliff-slope white surface (validates “landmark surface” pattern).
2. **Salar de Uyuni** — second playa template + cactus “islands.”
3. **Monument Valley** — mesa spill tuning on a famous `/tpll`.
4. **Chicago lakefront** — shoreline + dune band without urban blocks.
5. **Iceland volcanic palette** — basalt/tuff spill from volcanic geo boxes.

---

## Workflow per place

1. Pick `/tpll` lat/lon from Google Maps / Street View.
2. Run `tools/audit_playtest_ecoregions.ps1` (add row) — note WWF eco vs expected.
3. Import or hand-draw GeoJSON (`tools/import_osm_biome_zone.ps1` when OSM has a relation).
4. Tune surface spill constants in `ChunkSurfaceSpillField` / zone-specific helper.
5. Screenshot → `reference/` folder; link in this doc.

---

## Related docs

- [landmark-features-plan.md](landmark-features-plan.md) — implementation sketch
- [regional-biome-zones.md](regional-biome-zones.md) — GeoJSON format & OSM import
- [variation-smoothing-detail-plan.md](variation-smoothing-detail-plan.md) — biome variants inside regions
