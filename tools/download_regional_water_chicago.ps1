param(
    [string]$OutputJson = "chicago_osm_water.json"
)

$ErrorActionPreference = "Stop"
$root = Split-Path -Parent $MyInvocation.MyCommand.Path
$outputFile = Join-Path $root $OutputJson

# Chicago metro + Lake Michigan shoreline (south shore through downtown)
$bbox = "41.55,42.05,-87.95,-87.45"
$query = @"
[out:json][timeout:180];
(
  relation["natural"="water"]["name"~"Lake Michigan",i]($bbox);
  relation["type"="multipolygon"]["natural"="water"]($bbox);
  way["natural"="water"]($bbox);
  way["waterway"~"river|canal|stream"]($bbox);
);
out geom;
"@

Write-Host "Querying Overpass for regional water in bbox $bbox ..."
Invoke-RestMethod -Uri "https://overpass-api.de/api/interpreter" -Method Post -Body @{ data = $query } -OutFile $outputFile
Write-Host "Saved $($outputFile) ($((Get-Item $outputFile).Length) bytes)"

& (Join-Path $root "convert_osm_water_to_geojson.ps1") -InputPath $OutputJson -OutputPath "regional_chicago_water.geojson"

$dest = Join-Path $root "..\1.20.1\src\main\resources\data\terracraft\regional\chicago_water.geojson"
Copy-Item -Path (Join-Path $root "regional_chicago_water.geojson") -Destination $dest -Force
Write-Host "Copied to $dest"
