# Point-in-polygon lookup for WWF ecoregions (ray casting on first ring).
param(
    [double]$Latitude,
    [double]$Longitude,
    [string]$GeoJsonPath = ""
)

$ErrorActionPreference = "Stop"

if ([string]::IsNullOrWhiteSpace($GeoJsonPath)) {
    $GeoJsonPath = Join-Path $env:APPDATA ".minecraft\terracraft\data\wwf_ecoregions.geojson"
}

if (-not (Test-Path $GeoJsonPath)) {
    throw "GeoJSON not found at $GeoJsonPath. Run tools/download_ecoregions.ps1 first."
}

$geo = Get-Content -Raw -Path $GeoJsonPath | ConvertFrom-Json

function Test-PointInRing([double]$lon, [double]$lat, $ring) {
    $inside = $false
    $j = $ring.Count - 1
    for ($i = 0; $i -lt $ring.Count; $i++) {
        $xi = [double]$ring[$i][0]
        $yi = [double]$ring[$i][1]
        $xj = [double]$ring[$j][0]
        $yj = [double]$ring[$j][1]
        $intersect = (($yi -gt $lat) -ne ($yj -gt $lat)) -and
            ($lon -lt ($xj - $xi) * ($lat - $yi) / (($yj - $yi) + 1e-12) + $xi)
        if ($intersect) { $inside = -not $inside }
        $j = $i
    }
    return $inside
}

function Test-PointInFeature([double]$lon, [double]$lat, $feature) {
    $geom = $feature.geometry
    if ($geom.type -eq "Polygon") {
        return Test-PointInRing $lon $lat $geom.coordinates[0]
    }
    if ($geom.type -eq "MultiPolygon") {
        foreach ($polygon in $geom.coordinates) {
            if (Test-PointInRing $lon $lat $polygon[0]) { return $true }
        }
    }
    return $false
}

$matches = @()
foreach ($feature in $geo.features) {
    if (Test-PointInFeature $Longitude $Latitude $feature) {
        $p = $feature.properties
        $matches += [pscustomobject]@{
            ECO_ID = $p.ECO_ID
            ECO_NAME = $p.ECO_NAME
            BIOME = $p.BIOME
            G200_BIOME = $p.G200_BIOME
            REALM = $p.REALM
        }
    }
}

Write-Host "Point lat=$Latitude lon=$Longitude"
if ($matches.Count -eq 0) {
    Write-Host "No ecoregion polygon match."
} else {
    $matches | Format-Table -AutoSize
}

$historical = Get-Content "C:\MCMods\Terracraft\1.20.1\src\main\resources\data\terracraft\ecoregion\clone_map_historical.json" -Raw | ConvertFrom-Json
foreach ($m in $matches) {
    $id = "$($m.ECO_ID)"
    if ($historical.PSObject.Properties.Name -contains $id) {
        Write-Host "Clone map[$id] = $($historical.$id)"
    } else {
        Write-Host "Clone map[$id] = (missing, fallback only)"
    }
}
