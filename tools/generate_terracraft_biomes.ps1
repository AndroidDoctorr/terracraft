# Generates terracraft worldgen/biome JSON from templates + required_biomes.txt
$ErrorActionPreference = "Stop"
$root = Join-Path $PSScriptRoot ".."
$required = Join-Path $root "1.20.1\src\main\resources\data\terracraft\ecoregion\required_biomes.txt"
$biomeOut = Join-Path $root "1.20.1\src\main\resources\data\terracraft\worldgen\biome"
$tagOut = Join-Path $root "1.20.1\src\main\resources\data\terracraft\tags\worldgen\biome"
$templateDir = Join-Path $PSScriptRoot "biome_templates"

& (Join-Path $PSScriptRoot "build_flora_placed_features.ps1")

New-Item -ItemType Directory -Force -Path $biomeOut, $tagOut | Out-Null

$utf8NoBom = New-Object System.Text.UTF8Encoding $false

function Write-Utf8NoBom([string]$Path, [string]$Content) {
    [System.IO.File]::WriteAllText($Path, $Content, $utf8NoBom)
}

# Global order prevents FeatureSorter cycles across Planet Earth biomes.
$script:FloraGlobalOrder = @(
    "terracraft:flora_wild_rice",
    "terracraft:flora_wild_flax",
    "terracraft:flora_wild_corn",
    "terracraft:flora_wild_peppers",
    "terracraft:flora_wild_beans",
    "terracraft:flora_wild_squash",
    "terracraft:flora_wild_cotton",
    "terracraft:flora_tea_bush",
    "terracraft:flora_indigo",
    "terracraft:flora_esparto",
    "terracraft:flora_oak_tree",
    "terracraft:flora_oak_sparse",
    "terracraft:flora_birch_tree",
    "terracraft:flora_acacia_tree",
    "terracraft:flora_spruce_tree",
    "terracraft:flora_olive_tree",
    "terracraft:flora_cypress_tree",
    "terracraft:flora_maple_tree",
    "terracraft:flora_baobab_tree",
    "terracraft:flora_palm_tree",
    "terracraft:flora_rubber_tree",
    "terracraft:flora_wild_grape_vine",
    "terracraft:flora_wild_wisteria_vine"
)

# Vanilla vegetal features in a single order shared by every clone (and compatible with
# alpine vanilla biomes: glow_lichen before patch_pumpkin).
$script:VegetationGlobalOrder = @(
    "minecraft:glow_lichen",
    "minecraft:patch_tall_grass",
    "minecraft:patch_tall_grass_2",
    "minecraft:forest_flowers",
    "minecraft:trees_jungle",
    "minecraft:trees_mangrove",
    "minecraft:trees_swamp",
    "minecraft:trees_savanna",
    "minecraft:trees_plains",
    "minecraft:trees_birch_and_oak",
    "minecraft:trees_taiga",
    "minecraft:trees_snowy",
    "minecraft:trees_sparse_jungle",
    "minecraft:trees_meadow",
    "minecraft:flower_warm",
    "minecraft:flower_default",
    "minecraft:flower_plains",
    "minecraft:flower_meadow",
    "minecraft:flower_swamp",
    "minecraft:patch_grass_savanna",
    "minecraft:patch_grass_forest",
    "minecraft:patch_grass_plain",
    "minecraft:patch_grass_jungle",
    "minecraft:patch_grass_taiga_2",
    "minecraft:patch_grass_badlands",
    "minecraft:patch_grass_normal",
    "minecraft:bamboo_light",
    "minecraft:vines",
    "minecraft:patch_large_fern",
    "minecraft:patch_berry_common",
    "minecraft:patch_melon",
    "minecraft:patch_melon_sparse",
    "minecraft:patch_dead_bush",
    "minecraft:patch_dead_bush_badlands",
    "minecraft:patch_cactus_decorated",
    "minecraft:brown_mushroom_normal",
    "minecraft:brown_mushroom_swamp",
    "minecraft:brown_mushroom_taiga",
    "minecraft:red_mushroom_normal",
    "minecraft:red_mushroom_swamp",
    "minecraft:red_mushroom_taiga",
    "minecraft:patch_waterlily",
    "minecraft:seagrass_swamp",
    "minecraft:patch_sugar_cane",
    "minecraft:patch_sugar_cane_swamp",
    "minecraft:patch_sugar_cane_badlands",
    "minecraft:patch_pumpkin"
) + $script:FloraGlobalOrder

function Sort-VegetationStep([string[]]$features) {
    $present = [System.Collections.Generic.HashSet[string]]::new()
    foreach ($f in $features) { [void]$present.Add($f) }
    $sorted = [System.Collections.Generic.List[string]]::new()
    foreach ($id in $script:VegetationGlobalOrder) {
        if ($present.Contains($id)) {
            [void]$sorted.Add($id)
            [void]$present.Remove($id)
        }
    }
    foreach ($f in $features) {
        if ($present.Contains($f)) {
            [void]$sorted.Add($f)
            [void]$present.Remove($f)
        }
    }
    return $sorted.ToArray()
}

function Test-OldWorld([string]$short) {
    return $short -match "mediterranean|steppe|_palearctic|_afrotropical|_indomalayan|_australasian"
}

function Test-NewWorld([string]$short) {
    return $short -match "_nearctic|_neotropical"
}

function Test-CoastalWarm([string]$short) {
    return $short -match "^jungle_|^savanna_|^mangrove"
}

function Add-FloraId([System.Collections.Generic.List[string]]$target, [string]$id) {
    if (-not $target.Contains($id)) { [void]$target.Add($id) }
}

function Get-FloraFeatures([string]$short) {
    $template = Pick-Template $short
    $picked = [System.Collections.Generic.List[string]]::new()

    if ($short -eq "mediterranean_scrub") {
        Add-FloraId $picked "terracraft:flora_wild_flax"
        Add-FloraId $picked "terracraft:flora_esparto"
        Add-FloraId $picked "terracraft:flora_oak_tree"
        Add-FloraId $picked "terracraft:flora_spruce_tree"
        Add-FloraId $picked "terracraft:flora_olive_tree"
        Add-FloraId $picked "terracraft:flora_cypress_tree"
        Add-FloraId $picked "terracraft:flora_wild_grape_vine"
    }
    elseif ($template -eq "plains") {
        if (Test-OldWorld $short) { Add-FloraId $picked "terracraft:flora_wild_flax" }
        if (Test-NewWorld $short) {
            Add-FloraId $picked "terracraft:flora_wild_corn"
            Add-FloraId $picked "terracraft:flora_wild_peppers"
            Add-FloraId $picked "terracraft:flora_wild_beans"
            Add-FloraId $picked "terracraft:flora_wild_squash"
            Add-FloraId $picked "terracraft:flora_wild_cotton"
        }
        Add-FloraId $picked "terracraft:flora_oak_tree"
        if ($short -match "_nearctic") { Add-FloraId $picked "terracraft:flora_maple_tree" }
    }
    elseif ($template -eq "forest") {
        if (Test-OldWorld $short) {
            Add-FloraId $picked "terracraft:flora_wild_flax"
            Add-FloraId $picked "terracraft:flora_wild_grape_vine"
        }
        if (Test-NewWorld $short) {
            Add-FloraId $picked "terracraft:flora_wild_corn"
            Add-FloraId $picked "terracraft:flora_wild_peppers"
            Add-FloraId $picked "terracraft:flora_wild_beans"
            Add-FloraId $picked "terracraft:flora_wild_squash"
            Add-FloraId $picked "terracraft:flora_wild_cotton"
            Add-FloraId $picked "terracraft:flora_maple_tree"
        }
        if ($short -match "_indomalayan") {
            Add-FloraId $picked "terracraft:flora_tea_bush"
            Add-FloraId $picked "terracraft:flora_wild_wisteria_vine"
            Add-FloraId $picked "terracraft:flora_rubber_tree"
        }
        if ($short -match "_neotropical") {
            Add-FloraId $picked "terracraft:flora_rubber_tree"
            Add-FloraId $picked "terracraft:flora_indigo"
        }
        if ($short -match "_palearctic") { Add-FloraId $picked "terracraft:flora_birch_tree" }
        if ($short -match "_palearctic|_nearctic") { Add-FloraId $picked "terracraft:flora_oak_tree" }
        if ($short -match "_afrotropical") { Add-FloraId $picked "terracraft:flora_baobab_tree" }
    }
    elseif ($template -eq "savanna") {
        Add-FloraId $picked "terracraft:flora_acacia_tree"
        if ($short -match "_afrotropical") { Add-FloraId $picked "terracraft:flora_baobab_tree" }
        if (Test-CoastalWarm $short) { Add-FloraId $picked "terracraft:flora_palm_tree" }
        if (Test-NewWorld $short) {
            Add-FloraId $picked "terracraft:flora_wild_corn"
            Add-FloraId $picked "terracraft:flora_wild_peppers"
            Add-FloraId $picked "terracraft:flora_wild_beans"
            Add-FloraId $picked "terracraft:flora_wild_squash"
            Add-FloraId $picked "terracraft:flora_wild_cotton"
        }
        if (Test-OldWorld $short) { Add-FloraId $picked "terracraft:flora_wild_flax" }
    }
    elseif ($template -eq "jungle") {
        if ($short -match "_indomalayan") {
            Add-FloraId $picked "terracraft:flora_wild_rice"
            Add-FloraId $picked "terracraft:flora_tea_bush"
            Add-FloraId $picked "terracraft:flora_rubber_tree"
            Add-FloraId $picked "terracraft:flora_wild_wisteria_vine"
        }
        if ($short -match "_neotropical|_nearctic") {
            Add-FloraId $picked "terracraft:flora_rubber_tree"
            Add-FloraId $picked "terracraft:flora_indigo"
        }
        if ($short -match "_afrotropical") {
            Add-FloraId $picked "terracraft:flora_baobab_tree"
            Add-FloraId $picked "terracraft:flora_rubber_tree"
        }
        if (Test-CoastalWarm $short) { Add-FloraId $picked "terracraft:flora_palm_tree" }
    }
    elseif ($template -eq "sparse_jungle") {
        Add-FloraId $picked "terracraft:flora_oak_sparse"
        if ($short -match "_neotropical|_afrotropical|_australasian") {
            Add-FloraId $picked "terracraft:flora_rubber_tree"
            Add-FloraId $picked "terracraft:flora_indigo"
        }
        if (Test-CoastalWarm $short) { Add-FloraId $picked "terracraft:flora_palm_tree" }
    }
    elseif ($template -eq "taiga") {
        if (Test-OldWorld $short) { Add-FloraId $picked "terracraft:flora_wild_flax" }
        Add-FloraId $picked "terracraft:flora_spruce_tree"
    }
    elseif ($template -eq "snowy_plains") {
        Add-FloraId $picked "terracraft:flora_spruce_tree"
    }
    elseif ($template -eq "meadow") {
        if (Test-OldWorld $short) { Add-FloraId $picked "terracraft:flora_wild_flax" }
        Add-FloraId $picked "terracraft:flora_oak_sparse"
        Add-FloraId $picked "terracraft:flora_birch_tree"
    }
    elseif ($template -eq "swamp") {
        if ($short -match "_indomalayan|floodplain") { Add-FloraId $picked "terracraft:flora_wild_rice" }
        if (Test-OldWorld $short) { Add-FloraId $picked "terracraft:flora_wild_flax" }
        Add-FloraId $picked "terracraft:flora_oak_sparse"
    }
    elseif ($template -eq "mangrove_swamp") {
        Add-FloraId $picked "terracraft:flora_palm_tree"
        if ($short -match "_indomalayan|_afrotropical|_neotropical") { Add-FloraId $picked "terracraft:flora_wild_rice" }
    }
    elseif ($template -eq "badlands") {
        if ($short -match "chaparral") {
            Add-FloraId $picked "terracraft:flora_oak_tree"
            Add-FloraId $picked "terracraft:flora_wild_grape_vine"
            Add-FloraId $picked "terracraft:flora_wild_corn"
            Add-FloraId $picked "terracraft:flora_wild_peppers"
        }
        else {
            Add-FloraId $picked "terracraft:flora_oak_sparse"
        }
        if ($short -eq "semi_arid_scrub" -and (Test-OldWorld $short)) {
            Add-FloraId $picked "terracraft:flora_wild_flax"
        }
    }

    $ordered = @()
    foreach ($id in $script:FloraGlobalOrder) {
        if ($picked.Contains($id)) { $ordered += $id }
    }
    return $ordered
}

function Apply-Flora([object]$json, [string]$short) {
    $extra = Get-FloraFeatures $short
    $all = [System.Collections.Generic.List[string]]::new()
    foreach ($existing in $json.features[9]) {
        if ($existing -notlike "terracraft:flora_*") {
            [void]$all.Add($existing)
        }
    }
    foreach ($id in $extra) {
        if (-not $all.Contains($id)) { [void]$all.Add($id) }
    }
    if ($extra.Count -gt 0 -and -not $all.Contains("minecraft:glow_lichen")) {
        [void]$all.Add("minecraft:glow_lichen")
    }
    $json.features[9] = Sort-VegetationStep $all.ToArray()
    return $json
}

function Pick-Template([string]$id) {
    if ($id -match "jungle") { return "jungle" }
    if ($id -match "mangrove") { return "mangrove_swamp" }
    if ($id -match "floodplain") { return "swamp" }
    if ($id -match "tundra") { return "snowy_plains" }
    if ($id -match "taiga") { return "taiga" }
    if ($id -match "savanna") { return "savanna" }
    if ($id -match "semi_arid|chaparral") { return "badlands" }
    if ($id -match "mediterranean") { return "mediterranean_scrub" }
    if ($id -match "tropical_dry") { return "sparse_jungle" }
    if ($id -match "montane_meadow") { return "meadow" }
    if ($id -match "forest") { return "forest" }
    if ($id -match "steppe|plains") { return "plains" }
    return "plains"
}

function Tune-Biome([object]$json, [string]$id) {
    if ($id -match "mediterranean") {
        $json.temperature = 0.95
        $json.downfall = 0.45
        $json.has_precipitation = $true
    }
    if ($id -match "semi_arid|chaparral") { $json.temperature = 0.9; $json.downfall = 0.2 }
    if ($id -match "tundra") { $json.temperature = 0.0; $json.downfall = 0.5 }
    if ($id -match "tropical_dry") { $json.temperature = 0.9; $json.downfall = 0.35 }
    if ($id -match "montane_meadow") { $json.temperature = 0.35; $json.downfall = 0.45 }
    if ($id -match "steppe") { $json.temperature = 0.65; $json.downfall = 0.25 }
    return $json
}

$earthTag = @()
$cropNewWorld = @()
$cropOldWorld = @()
$regionMediterranean = @()
$regionAfrotropical = @()
$regionIndomalayan = @()
$climateGrassy = @()
$climateTemperate = @()
$climateTropical = @()
$climateTemperateForest = @()
$regionMediterraneanFlora = @()
$climateRiver = @()
$coastalWarm = @()
$climateSemiArid = @()

foreach ($line in Get-Content $required) {
    $full = $line.Trim()
    if ([string]::IsNullOrWhiteSpace($full)) { continue }
    $short = $full -replace "^terracraft:", ""
    $templateName = Pick-Template $short
    $templatePath = Join-Path $templateDir "$templateName.json"
    if (-not (Test-Path $templatePath)) { throw "Missing template $templatePath" }
    $json = Get-Content -Raw $templatePath | ConvertFrom-Json
    $json = Tune-Biome $json $short
    $json = Apply-Flora $json $short
    $json | ConvertTo-Json -Depth 20 -Compress | ForEach-Object {
        Write-Utf8NoBom (Join-Path $biomeOut "$short.json") $_
    }

    $earthTag += $full
    if ($short -match "mediterranean") { $regionMediterranean += $full; $regionMediterraneanFlora += $full }
    if ($short -match "_afrotropical" -and $short -notmatch "_bf$") { $regionAfrotropical += $full }
    if ($short -match "_indomalayan" -and $short -notmatch "_bf$") { $regionIndomalayan += $full }
    if ($short -match "floodplain|mangrove|jungle_indomalayan") { $climateRiver += $full }
    if ($short -match "mangrove|jungle_|savanna_") { $coastalWarm += $full }
    if ($short -match "semi_arid|chaparral") { $climateSemiArid += $full }
    if ($short -match "_nearctic|_neotropical" -and $short -notmatch "_bf$") {
        if ($short -match "plains_|forest_|savanna_|steppe|chaparral|temperate_steppe") { $cropNewWorld += $full }
    }
    if ($short -match "mediterranean") { $cropOldWorld += $full }
    if ($short -match "_palearctic|_afrotropical|_indomalayan|_australasian" -and $short -notmatch "_bf$") {
        if ($short -match "plains_|forest_|steppe|mediterranean") { $cropOldWorld += $full }
    }
    if ($short -match "_bf$" -or $short -eq "terracraft:temperate_steppe_bf") { $climateGrassy += $full }
    if ($short -match "taiga_palearctic|chaparral|forest_palearctic_bf|forest_neotropical_bf") { $climateTemperate += $full }
    if ($short -match "jungle_|savanna_|mangrove|tropical_dry") { $climateTropical += $full }
    if ($short -match "forest_") { $climateTemperateForest += $full; $regionMediterraneanFlora += $full }
}

function Write-Tag([string]$name, [string[]]$values) {
    $obj = @{ replace = $false; values = ($values | Sort-Object -Unique) }
    Write-Utf8NoBom (Join-Path $tagOut "$name.json") ($obj | ConvertTo-Json -Compress)
}

Write-Tag "earth" $earthTag
Write-Tag "crop_new_world" $cropNewWorld
Write-Tag "crop_old_world" $cropOldWorld
Write-Tag "region_mediterranean" $regionMediterranean
Write-Tag "region_afrotropical" $regionAfrotropical
Write-Tag "region_indomalayan" $regionIndomalayan
Write-Tag "region_mediterranean_flora" $regionMediterraneanFlora
Write-Tag "climate_grassy" $climateGrassy
Write-Tag "climate_temperate" $climateTemperate
Write-Tag "climate_tropical" $climateTropical
Write-Tag "climate_temperate_forest" $climateTemperateForest
Write-Tag "climate_river" $climateRiver
Write-Tag "coastal_warm" $coastalWarm
Write-Tag "climate_semi_arid" $climateSemiArid

Write-Host "Generated $($earthTag.Count) biome JSON files"

# Fail fast if any clone pair still disagrees on shared feature order.
$conflicts = [System.Collections.Generic.List[string]]::new()
$names = $earthTag | ForEach-Object { $_ -replace "^terracraft:", "" } | Sort-Object
$featureLists = @{}
foreach ($name in $names) {
    $path = Join-Path $biomeOut "$name.json"
    $featureLists[$name] = @((Get-Content $path | ConvertFrom-Json).features[9])
}
for ($i = 0; $i -lt $names.Count; $i++) {
    for ($j = $i + 1; $j -lt $names.Count; $j++) {
        $a = $names[$i]; $b = $names[$j]
        $shared = $featureLists[$a] | Where-Object { $featureLists[$b] -contains $_ }
        foreach ($f1 in $shared) {
            foreach ($f2 in $shared) {
                if ($f1 -eq $f2) { continue }
                $a1 = [array]::IndexOf($featureLists[$a], $f1)
                $a2 = [array]::IndexOf($featureLists[$a], $f2)
                $b1 = [array]::IndexOf($featureLists[$b], $f1)
                $b2 = [array]::IndexOf($featureLists[$b], $f2)
                if (($a1 -lt $a2) -ne ($b1 -lt $b2)) {
                    [void]$conflicts.Add("$a vs $b : $f1 before $f2")
                }
            }
        }
    }
}
if ($conflicts.Count -gt 0) {
    $conflicts | Select-Object -First 10 | ForEach-Object { Write-Host $_ }
    throw "Feature order conflicts remain after sorting ($($conflicts.Count) pairs)"
}
Write-Host "Feature order validation passed"
