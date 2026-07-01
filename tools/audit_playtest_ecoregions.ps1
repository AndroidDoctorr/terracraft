# Audits playtest /tpll coordinates against WWF ecoregions and Terracraft clone maps.
param(
    [string]$GeoJsonPath = "",
    [string]$CloneMapPath = "",
    [string]$EcoOverridesPath = ""
)

$ErrorActionPreference = "Stop"

if ([string]::IsNullOrWhiteSpace($GeoJsonPath)) {
    $GeoJsonPath = Join-Path $PSScriptRoot "wwf_ecoregions.geojson"
}
if ([string]::IsNullOrWhiteSpace($CloneMapPath)) {
    $CloneMapPath = Join-Path $PSScriptRoot "..\1.20.1\src\main\resources\data\terracraft\ecoregion\clone_map_historical.json"
}
if ([string]::IsNullOrWhiteSpace($EcoOverridesPath)) {
    $EcoOverridesPath = Join-Path $PSScriptRoot "..\1.20.1\src\main\resources\data\terracraft\ecoregion\eco_overrides.json"
}

$spots = @(
    @{ name = "Antioch"; lat = 36.2017; lon = 36.1600 }
    @{ name = "Grand Canyon"; lat = 36.066; lon = -112.117 }
    @{ name = "Lake Merritt"; lat = 37.8014; lon = -122.2585 }
    @{ name = "Chicago/Navy Pier"; lat = 41.8919; lon = -87.6100 }
    @{ name = "Giza"; lat = 29.976; lon = 31.134 }
    @{ name = "Bonneville salt flats (core)"; lat = 40.758; lon = -113.578 }
    @{ name = "Bonneville salt flats (north)"; lat = 41.001; lon = -113.551 }
    @{ name = "Great Basin scrub (GSL fringe)"; lat = 41.116; lon = -112.477 }
    @{ name = "Dover"; lat = 51.128; lon = 1.338 }
    @{ name = "Sahara dunes (center)"; lat = 25.0; lon = 0.0 }
    @{ name = "Sahara dunes (Libya)"; lat = 28.7; lon = 14.9 }
)

function Test-PointInRing([double]$lon, [double]$lat, $ring) {
    $inside = $false
    $j = $ring.Count - 1
    for ($i = 0; $i -lt $ring.Count; $i++) {
        $xi = [double]$ring[$i][0]
        $yi = [double]$ring[$i][1]
        $xj = [double]$ring[$j][0]
        $yj = [double]$ring[$j][1]
        if (($yi -gt $lat) -ne ($yj -gt $lat)) {
            if ($lon -lt ($xj - $xi) * ($lat - $yi) / ($yj - $yi + 1e-12) + $xi) {
                $inside = -not $inside
            }
        }
        $j = $i
    }
    return $inside
}

function Find-Ecoregion([double]$lat, [double]$lon, $features) {
    foreach ($feature in $features) {
        $geometry = $feature.geometry
        if ($geometry.type -eq "Polygon") {
            if (Test-PointInRing $lon $lat $geometry.coordinates[0]) {
                return $feature.properties
            }
        }
        elseif ($geometry.type -eq "MultiPolygon") {
            foreach ($polygon in $geometry.coordinates) {
                if (Test-PointInRing $lon $lat $polygon[0]) {
                    return $feature.properties
                }
            }
        }
    }
    return $null
}

function Get-ExpectedOverride([string]$ecoName, [int]$ecoId, [double]$lat, [double]$lon, $ecoOverrides) {
    if ($lat -ge 40.50 -and $lat -le 41.20 -and $lon -ge -114.10 -and $lon -le -113.10) {
        return "terracraft:playa_salt (geo box)"
    }
    if ($lat -ge 29.90 -and $lat -le 30.10 -and $lon -ge 31.00 -and $lon -le 31.35) {
        return "terracraft:desert_arid (geo box)"
    }
    if ($lat -ge 35.95 -and $lat -le 36.25 -and $lon -ge -112.35 -and $lon -le -111.85) {
        return "terracraft:semi_arid_scrub (geo box)"
    }

    $pin = $ecoOverrides."$ecoId"
    if ($pin) {
        return "terracraft:$pin (eco pin)"
    }

    $lower = $ecoName.ToLower()
    if ($lower -match "playa|salt flat|salt pan|saline|bonneville|alkali flat|salar") {
        return "terracraft:playa_salt (name)"
    }
    if ($lower -match "sahara|erg|hamada|rub al khali|namib|gobi|atacama|arabian desert|libyan desert|north saharan|saharan desert|hyper-arid|hyper arid|desert|dune|xeric|arid land") {
        return "terracraft:desert_arid (name)"
    }
    if ($lower -match "mediterranean") {
        return "terracraft:mediterranean_scrub (name)"
    }
    if ($lower -match "chaparral") {
        return "terracraft:chaparral_nearctic (name)"
    }

    return $null
}

if (-not (Test-Path $GeoJsonPath)) {
    throw "GeoJSON not found at $GeoJsonPath"
}

Write-Host "Loading $GeoJsonPath ..."
$geo = Get-Content -Raw -Path $GeoJsonPath | ConvertFrom-Json
$cloneMap = Get-Content -Raw -Path $CloneMapPath | ConvertFrom-Json
$ecoOverrides = @{}
if (Test-Path $EcoOverridesPath) {
    $ecoOverrides = Get-Content -Raw -Path $EcoOverridesPath | ConvertFrom-Json
}

Write-Host ""
Write-Host ("{0,-24} {1,-8} {2,-42} {3,-28} {4}" -f "Location", "ECO_ID", "ECO_NAME", "CLONE_MAP", "EXPECTED_OVERRIDE")
Write-Host ("-" * 130)

foreach ($spot in $spots) {
    $props = Find-Ecoregion $spot.lat $spot.lon $geo.features
    if (-not $props) {
        Write-Host ("{0,-24} {1}" -f $spot.name, "NO MATCH")
        continue
    }

    $ecoId = [int]$props.ECO_ID
    $ecoName = [string]$props.ECO_NAME
    $mapped = [string]$cloneMap."$ecoId"
    $expected = Get-ExpectedOverride $ecoName $ecoId $spot.lat $spot.lon $ecoOverrides
    if (-not $expected) {
        $expected = $mapped
    }

    Write-Host ("{0,-24} {1,-8} {2,-42} {3,-28} {4}" -f $spot.name, $ecoId, $ecoName.Substring(0, [Math]::Min(42, $ecoName.Length)), $mapped, $expected)
}

Write-Host ""
Write-Host "Override wins when EXPECTED_OVERRIDE differs from CLONE_MAP."
