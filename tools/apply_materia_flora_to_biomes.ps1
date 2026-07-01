# Adds Materia flora placed features to Terracraft biome JSONs (vegetation step).
$ErrorActionPreference = "Stop"
$biomeDir = Join-Path $PSScriptRoot "..\1.20.1\src\main\resources\data\terracraft\worldgen\biome"

function Resolve-FloraAdditions {
    param([string]$Name)

    switch -Regex ($Name) {
        "^mediterranean_scrub$" {
            return @(
                "flora_fig_tree", "flora_indigo", "flora_marigold"
            )
        }
        "^semi_arid_scrub$" {
            return @(
                "flora_yucca", "flora_agave", "flora_plantain",
                "flora_bluebonnet", "flora_marigold"
            )
        }
        "^desert_arid$" {
            return @(
                "flora_agave", "flora_yucca", "flora_marigold"
            )
        }
        "^chaparral_nearctic$" {
            return @(
                "flora_esparto", "flora_yucca", "flora_agave", "flora_plantain",
                "flora_bluebonnet", "flora_purple_coneflower", "flora_marigold"
            )
        }
        "^floodplain_meadow$" {
            return @(
                "flora_reeds", "flora_lotus", "flora_wild_taro", "flora_wild_cotton",
                "flora_hibiscus", "flora_white_lily"
            )
        }
        "^mangrove_coastal$" {
            return @(
                "flora_reeds", "flora_lotus", "flora_hibiscus", "flora_white_lily"
            )
        }
        "^montane_meadow$" {
            return @(
                "flora_cedar_mega_tree", "flora_cedar_tree",
                "flora_purple_coneflower", "flora_fuchsia"
            )
        }
        "^taiga_nearctic$|^taiga_palearctic$" {
            return @("flora_cedar_tree")
        }
        "^taiga_australasian$" {
            return @("flora_eucalyptus_grove", "flora_eucalyptus_tree")
        }
        "^forest_nearctic" {
            return @(
                "flora_cedar_tree", "flora_purple_coneflower", "flora_fuchsia"
            )
        }
        "^forest_palearctic" {
            return @(
                "flora_cedar_tree", "flora_fig_tree", "flora_indigo", "flora_fuchsia"
            )
        }
        "^forest_neotropical" {
            return @(
                "flora_fig_tree", "flora_hibiscus", "flora_fuchsia"
            )
        }
        "^forest_indomalayan" {
            return @(
                "flora_fig_tree", "flora_hibiscus"
            )
        }
        "^forest_australasian" {
            return @(
                "flora_eucalyptus_grove", "flora_eucalyptus_tree", "flora_fuchsia",
                "flora_rainbow_eucalyptus_grove"
            )
        }
        "^forest_afrotropical" {
            return @(
                "flora_fig_tree", "flora_hibiscus", "flora_marigold"
            )
        }
        "^jungle_afrotropical$" {
            return @(
                "flora_wild_taro", "flora_hibiscus", "flora_wild_cotton"
            )
        }
        "^jungle_australasian$" {
            return @(
                "flora_wild_taro", "flora_hibiscus", "flora_rubber_tree",
                "flora_eucalyptus_grove"
            )
        }
        "^jungle_indomalayan$" {
            return @(
                "flora_wild_taro", "flora_hibiscus", "flora_wild_cotton"
            )
        }
        "^jungle_nearctic$|^jungle_neotropical$|^jungle_palearctic$" {
            return @(
                "flora_wild_taro", "flora_hibiscus"
            )
        }
        "^savanna_afrotropical$" {
            return @("flora_marigold")
        }
        "^savanna_nearctic$" {
            return @(
                "flora_bluebonnet", "flora_purple_coneflower"
            )
        }
        "^savanna_neotropical$" {
            return @(
                "flora_marigold", "flora_hibiscus"
            )
        }
        "^savanna_australasian$|^savanna_indomalayan$" {
            return @("flora_marigold")
        }
        "^plains_nearctic" {
            return @(
                "flora_bluebonnet", "flora_purple_coneflower"
            )
        }
        "^plains_neotropical" {
            return @(
                "flora_marigold", "flora_hibiscus"
            )
        }
        "^plains_palearctic" {
            return @("flora_indigo")
        }
        "^plains_indomalayan" {
            return @(
                "flora_tea_bush", "flora_wild_taro"
            )
        }
        "^plains_australasian" {
            return @("flora_eucalyptus_tree")
        }
        "^plains_afrotropical$" {
            return @("flora_marigold")
        }
        "^temperate_steppe" {
            return @(
                "flora_indigo", "flora_purple_coneflower"
            )
        }
        "^tropical_dry_forest$" {
            return @(
                "flora_fig_tree", "flora_marigold", "flora_agave", "flora_baobab_tree"
            )
        }
        default {
            return @()
        }
    }
}

$updated = 0
Get-ChildItem (Join-Path $biomeDir "*.json") | ForEach-Object {
    $name = $_.BaseName
    $additions = Resolve-FloraAdditions -Name $name
    if ($additions.Count -eq 0) { return }

    $json = Get-Content $_.FullName -Raw | ConvertFrom-Json
    $step = $json.features[9]
    if (-not $step) {
        Write-Warning "No vegetation step in $name"
        return
    }

    $changed = $false
    foreach ($flora in $additions) {
        $ref = "terracraft:$flora"
        if ($step -notcontains $ref) {
            $step += $ref
            $changed = $true
        }
    }

    if ($changed) {
        $json.features[9] = $step
        $json | ConvertTo-Json -Depth 100 -Compress | Set-Content $_.FullName -Encoding UTF8
        $updated++
        Write-Host "Updated $name (+$($additions.Count) flora entries, deduped)"
    }
}

Write-Host "Done. Updated $updated biome files."
