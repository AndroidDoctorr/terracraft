# Fetch an OSM relation/way polygon and write a Terracraft regional biome zone GeoJSON.
param(
    [Parameter(Mandatory = $true)]
    [string]$Name,
    [Parameter(Mandatory = $true)]
    [string]$Biome,
    [double]$MaxElevationM = 9000,
    [double]$MinElevationM = -500,
    [string]$OutputDir = ""
)

$ErrorActionPreference = "Stop"

if ([string]::IsNullOrWhiteSpace($OutputDir)) {
    $OutputDir = Join-Path $env:APPDATA ".minecraft\terracraft\data\regional\biome_zones"
}

New-Item -ItemType Directory -Force -Path $OutputDir | Out-Null

$query = @"
[out:json][timeout:60];
(
  relation["name"="$Name"];
  way["name"="$Name"];
);
out geom;
"@

$encoded = [uri]::EscapeDataString($query)
$url = "https://overpass-api.de/api/interpreter?data=$encoded"
Write-Host "Querying Overpass for '$Name'..."
$response = Invoke-RestMethod -Uri $url -Method Get

if (-not $response.elements -or $response.elements.Count -eq 0) {
    throw "No OSM features found for name '$Name'. Try a different spelling or add a manual polygon."
}

$element = $response.elements | Select-Object -First 1
$coords = @()

if ($element.type -eq "way" -and $element.geometry) {
    foreach ($pt in $element.geometry) {
        $coords += ,@([double]$pt.lon, [double]$pt.lat)
    }
}
elseif ($element.type -eq "relation" -and $element.members) {
    $outerWay = $element.members | Where-Object { $_.role -eq "outer" -and $_.type -eq "way" } | Select-Object -First 1
    if ($outerWay -and $outerWay.geometry) {
        foreach ($pt in $outerWay.geometry) {
            $coords += ,@([double]$pt.lon, [double]$pt.lat)
        }
    }
}

if ($coords.Count -lt 4) {
    throw "Could not extract a polygon ring from OSM response. Trace manually in geojson.io instead."
}

$first = $coords[0]
$last = $coords[$coords.Count - 1]
if ($first[0] -ne $last[0] -or $first[1] -ne $last[1]) {
    $coords += ,@($first[0], $first[1])
}

$slug = ($Name.ToLower() -replace '[^a-z0-9]+', '_').Trim('_')
$outPath = Join-Path $OutputDir "$slug.geojson"

$feature = @{
    type = "FeatureCollection"
    metadata = @{
        source = "OpenStreetMap via Overpass"
        osm_name = $Name
        imported = (Get-Date).ToString("o")
    }
    features = @(
        @{
            type = "Feature"
            properties = @{
                biome = $Biome
                max_elevation_m = $MaxElevationM
                min_elevation_m = $MinElevationM
                name = $Name
            }
            geometry = @{
                type = "Polygon"
                coordinates = @(,$coords)
            }
        }
    )
}

$json = $feature | ConvertTo-Json -Depth 10
[System.IO.File]::WriteAllText($outPath, $json)
Write-Host "Wrote $outPath"
Write-Host "Restart Minecraft or reload the world. Regenerate chunks in the target area."
