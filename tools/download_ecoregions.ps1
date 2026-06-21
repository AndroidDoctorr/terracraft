# Downloads WWF Terrestrial Ecoregions GeoJSON from ArcGIS into .minecraft/terracraft/data/
param(
    [string]$OutputPath = "",
    [string]$QueryUrlTemplate = "https://services8.arcgis.com/7L75T5PDROpCazRR/arcgis/rest/services/WWF_ecoregions/FeatureServer/0/query?where=1%3D1&outFields=ECO_ID,ECO_NAME,BIOME,G200_BIOME,REALM,eco_code&outSR=4326&f=geojson&resultRecordCount=2000&resultOffset={0}"
)

$ErrorActionPreference = "Stop"

if ([string]::IsNullOrWhiteSpace($OutputPath)) {
    $minecraft = Join-Path $env:APPDATA ".minecraft"
    $OutputPath = Join-Path $minecraft "terracraft\data\wwf_ecoregions.geojson"
}

$outputDirectory = Split-Path -Parent $OutputPath
if (-not (Test-Path $outputDirectory)) {
    New-Item -ItemType Directory -Path $outputDirectory -Force | Out-Null
}

Write-Host "Downloading WWF ecoregions to $OutputPath"

$mergedFeatures = New-Object System.Collections.Generic.List[object]
$offset = 0
$page = 0

while ($true) {
    $url = $QueryUrlTemplate -f $offset
    $page++
    Write-Host "Fetching page $page (offset $offset)..."

    $response = Invoke-RestMethod -Uri $url -Method Get -Headers @{ "User-Agent" = "Terracraft/download_ecoregions.ps1" }
    if (-not $response.features -or $response.features.Count -eq 0) {
        break
    }

    foreach ($feature in $response.features) {
        [void]$mergedFeatures.Add($feature)
    }

    Write-Host "  $($response.features.Count) features (total $($mergedFeatures.Count))"

    if ($response.features.Count -lt 2000) {
        break
    }

    $offset += 2000
}

if ($mergedFeatures.Count -eq 0) {
    throw "No features returned from ArcGIS query."
}

$collection = [ordered]@{
    type = "FeatureCollection"
    features = $mergedFeatures
}

$collection | ConvertTo-Json -Depth 100 -Compress | Set-Content -Path $OutputPath -Encoding UTF8
Write-Host "Saved $($mergedFeatures.Count) features to $OutputPath"
