param(
    [string]$InputPath = "chicago_osm_water.json",
    [string]$OutputPath = "regional_chicago_water.geojson"
)

$ErrorActionPreference = "Stop"
$root = Split-Path -Parent $MyInvocation.MyCommand.Path
$inputFile = Join-Path $root $InputPath
$outputFile = Join-Path $root $OutputPath

$osm = Get-Content $inputFile -Raw | ConvertFrom-Json
$features = New-Object System.Collections.Generic.List[object]

foreach ($element in $osm.elements) {
    if ($null -eq $element.geometry -or $element.geometry.Count -lt 2) {
        continue
    }

    $name = $element.tags.name
    if (-not $name) { $name = $element.tags.waterway }
    if (-not $name) { $name = $element.tags.natural }
    if (-not $name) { $name = "water" }

    $isRiver = $element.tags.waterway -and -not $element.tags.natural
    if ($isRiver) {
        $line = @()
        foreach ($node in $element.geometry) {
            $line += ,@([double]$node.lon, [double]$node.lat)
        }

        $features.Add([ordered]@{
            type = "Feature"
            properties = [ordered]@{
                featurecla = "Water"
                name_en = [string]$name
                osm_id = [long]$element.id
            }
            geometry = [ordered]@{
                type = "LineString"
                coordinates = $line
            }
        })
        continue
    }

    if ($element.geometry.Count -lt 3) {
        continue
    }

    $ring = @()
    foreach ($node in $element.geometry) {
        $ring += ,@([double]$node.lon, [double]$node.lat)
    }

    $first = $ring[0]
    $last = $ring[$ring.Count - 1]
    if ($first[0] -ne $last[0] -or $first[1] -ne $last[1]) {
        $ring += ,@($first[0], $first[1])
    }

    $features.Add([ordered]@{
        type = "Feature"
        properties = [ordered]@{
            featurecla = "Water"
            name_en = [string]$name
            osm_id = [long]$element.id
        }
        geometry = [ordered]@{
            type = "Polygon"
            coordinates = @($ring)
        }
    })
}

$collection = [ordered]@{
    type = "FeatureCollection"
    features = $features
}

$collection | ConvertTo-Json -Depth 20 -Compress | Set-Content -Path $outputFile -Encoding UTF8
Write-Host "Wrote $($features.Count) water features to $outputFile ($((Get-Item $outputFile).Length) bytes)"
