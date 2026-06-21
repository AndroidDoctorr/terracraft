# Builds terracraft/ecoregion clone maps from WWF GeoJSON.
param(
    [string]$GeoJsonPath = "",
    [string]$OutputDir = ""
)

$ErrorActionPreference = "Stop"

if ([string]::IsNullOrWhiteSpace($GeoJsonPath)) {
    $GeoJsonPath = Join-Path $env:APPDATA ".minecraft\terracraft\data\wwf_ecoregions.geojson"
}

if ([string]::IsNullOrWhiteSpace($OutputDir)) {
    $OutputDir = Join-Path $PSScriptRoot "..\1.20.1\src\main\resources\data\terracraft\ecoregion"
}

if (-not (Test-Path $GeoJsonPath)) {
    throw "GeoJSON not found at $GeoJsonPath. Run tools/download_ecoregions.ps1 first."
}

function Normalize-Realm([string]$realm) {
    if ([string]::IsNullOrWhiteSpace($realm)) { return "palearctic" }
    switch ($realm.Trim()) {
        "Nearctic" { return "nearctic" }
        "Palearctic" { return "palearctic" }
        "Neotropical" { return "neotropical" }
        "Afrotropical" { return "afrotropical" }
        "Indo-Malayan" { return "indomalayan" }
        "Australasian" { return "australasian" }
        "Oceania" { return "oceania" }
        "Antarctic" { return "antarctic" }
        default { return ($realm.ToLower() -replace '[^a-z]', '') }
    }
}

function Get-HistoricalClone([int]$biomeCode, [string]$realmKey, [string]$ecoName) {
    $name = $ecoName.ToLower()
    switch ($biomeCode) {
        12 { return "terracraft:mediterranean_scrub" }
        13 { return "terracraft:semi_arid_scrub" }
        2 { return "terracraft:tropical_dry_forest" }
        3 { return "terracraft:tropical_dry_forest" }
        10 { return "terracraft:montane_meadow" }
        9 { return "terracraft:floodplain_meadow" }
        11 { return "terracraft:tundra_$realmKey" }
        14 { return "terracraft:mangrove_coastal" }
        1 { return "terracraft:jungle_$realmKey" }
        4 { return "terracraft:forest_$realmKey" }
        5 { return "terracraft:taiga_$realmKey" }
        6 { return "terracraft:taiga_$realmKey" }
        7 { return "terracraft:savanna_$realmKey" }
        8 {
            if ($name -match "steppe|grassland|prairie|pampas") { return "terracraft:temperate_steppe" }
            return "terracraft:plains_$realmKey"
        }
        default { return "terracraft:plains_$realmKey" }
    }
}

function Infer-RealmFromCenter([double]$lon, [double]$lat) {
    if ($lat -lt -60) { return "antarctic" }
    if ($lon -ge -170 -and $lon -lt -30) {
        if ($lat -ge 23.5) { return "nearctic" }
        if ($lat -ge -55) { return "neotropical" }
    }
    if ($lat -ge -35 -and $lat -lt 35 -and $lon -ge -20 -and $lon -lt 55) { return "afrotropical" }
    if ($lat -ge -50 -and $lat -lt 15 -and $lon -ge 110 -and $lon -lt 180) { return "australasian" }
    if ($lat -ge -15 -and $lat -lt 30 -and $lon -ge 60 -and $lon -lt 150) { return "indomalayan" }
    if ($lat -gt -30 -and $lat -lt 30 -and $lon -ge -130 -and $lon -lt -60) { return "oceania" }
    return "palearctic"
}

function Get-FeatureCenter($feature) {
    try {
        $point = $feature.geometry.coordinates[0][0][0]
        return @([double]$point[0], [double]$point[1])
    }
    catch {
        return @(0.0, 0.0)
    }
}

function Get-BiomeFloraClone([string]$historicalClone) {
    if ($historicalClone -match ":plains_" -or $historicalClone -match ":forest_" -or $historicalClone -eq "terracraft:temperate_steppe") {
        return "${historicalClone}_bf"
    }
    return $historicalClone
}

$geo = Get-Content -Raw -Path $GeoJsonPath | ConvertFrom-Json
$historical = @{}
$biomeFlora = @{}
$nameOverrides = @(
    @{ substring = "California interior chaparral"; clone = "terracraft:chaparral_nearctic" }
    @{ substring = "Mediterranean"; clone = "terracraft:mediterranean_scrub" }
)

foreach ($feature in $geo.features) {
    $props = $feature.properties
    $ecoId = [int]$props.ECO_ID
    if ($ecoId -le 0) { continue }

    $biomeCode = [int]$props.G200_BIOME
    if ($biomeCode -lt 1 -or $biomeCode -gt 14) { $biomeCode = [int]$props.BIOME }
    $realmKey = Normalize-Realm ([string]$props.REALM)
    if ([string]::IsNullOrWhiteSpace([string]$props.REALM)) {
        $center = Get-FeatureCenter $feature
        $realmKey = Infer-RealmFromCenter ([double]$center[0]) ([double]$center[1])
    }
    $ecoName = [string]$props.ECO_NAME

    $clone = Get-HistoricalClone $biomeCode $realmKey $ecoName
    foreach ($override in $nameOverrides) {
        if ($ecoName -like "*$($override.substring)*") {
            $clone = $override.clone
            break
        }
    }

    $historical["$ecoId"] = $clone
    $biomeFlora["$ecoId"] = Get-BiomeFloraClone $clone
}

New-Item -ItemType Directory -Force -Path $OutputDir | Out-Null
$utf8NoBom = New-Object System.Text.UTF8Encoding $false
function Write-Utf8NoBom([string]$Path, [string]$Content) {
    [System.IO.File]::WriteAllText($Path, $Content, $utf8NoBom)
}

Write-Utf8NoBom (Join-Path $OutputDir "clone_map_historical.json") ($historical | ConvertTo-Json -Compress)
Write-Utf8NoBom (Join-Path $OutputDir "clone_map_biome.json") ($biomeFlora | ConvertTo-Json -Compress)

$unique = @{}
foreach ($v in $historical.Values) { $unique[$v] = $true }
foreach ($v in $biomeFlora.Values) { $unique[$v] = $true }
Write-Utf8NoBom (Join-Path $OutputDir "required_biomes.txt") (($unique.Keys | Sort-Object) -join "`n")

Write-Host "Wrote $($historical.Count) ECO_ID mappings to $OutputDir"
Write-Host "Unique biomes required: $($unique.Count)"
