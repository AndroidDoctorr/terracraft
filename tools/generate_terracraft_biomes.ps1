# Generates terracraft worldgen/biome JSON from templates + required_biomes.txt
$ErrorActionPreference = "Stop"
$root = Join-Path $PSScriptRoot ".."
$required = Join-Path $root "1.20.1\src\main\resources\data\terracraft\ecoregion\required_biomes.txt"
$biomeOut = Join-Path $root "1.20.1\src\main\resources\data\terracraft\worldgen\biome"
$tagOut = Join-Path $root "1.20.1\src\main\resources\data\terracraft\tags\worldgen\biome"
$templateDir = Join-Path $PSScriptRoot "biome_templates"

New-Item -ItemType Directory -Force -Path $biomeOut, $tagOut | Out-Null

$utf8NoBom = New-Object System.Text.UTF8Encoding $false

function Write-Utf8NoBom([string]$Path, [string]$Content) {
    [System.IO.File]::WriteAllText($Path, $Content, $utf8NoBom)
}

function Pick-Template([string]$id) {
    if ($id -match "jungle") { return "jungle" }
    if ($id -match "mangrove") { return "mangrove_swamp" }
    if ($id -match "floodplain") { return "swamp" }
    if ($id -match "tundra") { return "snowy_plains" }
    if ($id -match "taiga") { return "taiga" }
    if ($id -match "savanna") { return "savanna" }
    if ($id -match "semi_arid|chaparral") { return "badlands" }
    if ($id -match "mediterranean") { return "savanna" }
    if ($id -match "tropical_dry") { return "sparse_jungle" }
    if ($id -match "montane_meadow") { return "meadow" }
    if ($id -match "forest") { return "forest" }
    if ($id -match "steppe|plains") { return "plains" }
    return "plains"
}

function Tune-Biome([object]$json, [string]$id) {
    if ($id -match "mediterranean") { $json.temperature = 0.95; $json.downfall = 0.35 }
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
    $json | ConvertTo-Json -Depth 20 -Compress | ForEach-Object {
        Write-Utf8NoBom (Join-Path $biomeOut "$short.json") $_
    }

    $earthTag += $full
    if ($short -match "mediterranean") { $regionMediterranean += $full; $regionMediterraneanFlora += $full; $coastalWarm += $full }
    if ($short -match "_afrotropical" -and $short -notmatch "_bf$") { $regionAfrotropical += $full }
    if ($short -match "_indomalayan" -and $short -notmatch "_bf$") { $regionIndomalayan += $full }
    if ($short -match "floodplain|mangrove|jungle_indomalayan") { $climateRiver += $full }
    if ($short -match "mangrove|mediterranean|jungle_|savanna_") { $coastalWarm += $full }
    if ($short -match "semi_arid|chaparral") { $climateSemiArid += $full }
    if ($short -match "_nearctic|_neotropical" -and $short -notmatch "_bf$") {
        if ($short -match "plains_|forest_|savanna_|steppe|chaparral|temperate_steppe") { $cropNewWorld += $full }
    }
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
