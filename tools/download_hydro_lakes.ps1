# Downloads Natural Earth 10m lakes GeoJSON into .minecraft/terracraft/data/
param(
    [string]$OutputPath = "",
    [string]$DownloadUrl = "https://raw.githubusercontent.com/nvkelso/natural-earth-vector/master/geojson/ne_10m_lakes.geojson"
)

$ErrorActionPreference = "Stop"

if ([string]::IsNullOrWhiteSpace($OutputPath)) {
    $minecraft = Join-Path $env:APPDATA ".minecraft"
    $OutputPath = Join-Path $minecraft "terracraft\data\hydro_lakes.geojson"
}

$outputDirectory = Split-Path -Parent $OutputPath
if (-not (Test-Path $outputDirectory)) {
    New-Item -ItemType Directory -Path $outputDirectory -Force | Out-Null
}

Write-Host "Downloading Natural Earth lakes to $OutputPath"
Invoke-WebRequest -Uri $DownloadUrl -OutFile $OutputPath -Headers @{ "User-Agent" = "Terracraft/download_hydro_lakes.ps1" }
$size = (Get-Item $OutputPath).Length
Write-Host "Saved $size bytes to $OutputPath"

$json = Get-Content $OutputPath -Raw | ConvertFrom-Json
Write-Host "Feature count: $($json.features.Count)"
