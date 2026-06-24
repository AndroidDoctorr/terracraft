# Generates terracraft/worldgen/placed_feature/flora_*.json with tuned densities.
$ErrorActionPreference = "Stop"
$outDir = Join-Path $PSScriptRoot "..\1.20.1\src\main\resources\data\terracraft\worldgen\placed_feature"
New-Item -ItemType Directory -Force -Path $outDir | Out-Null

$utf8NoBom = New-Object System.Text.UTF8Encoding $false
function Write-Utf8NoBom([string]$Path, [string]$Content) {
    [System.IO.File]::WriteAllText($Path, $Content, $utf8NoBom)
}
function Write-Json([string]$Path, [object]$Obj) {
    Write-Utf8NoBom $Path ($Obj | ConvertTo-Json -Depth 10 -Compress)
}

function Sapling-Survive([string]$Sapling, [hashtable]$ExtraProps = @{}) {
    $props = @{ Name = $Sapling }
    foreach ($k in $ExtraProps.Keys) { $props[$k] = $ExtraProps[$k] }
    return @{
        type = "minecraft:block_predicate_filter"
        predicate = @{
            type = "minecraft:would_survive"
            state = $props
        }
    }
}

function Surface-Placed([string]$Name, [string]$Configured, [int]$Count = 0, [int]$Rarity = 6, [string]$Heightmap = "WORLD_SURFACE_WG", [object[]]$ExtraPlacement = @()) {
    $placement = @()
    if ($Count -gt 0) { $placement += @{ type = "minecraft:count"; count = $Count } }
    if ($Rarity -gt 0) { $placement += @{ type = "minecraft:rarity_filter"; chance = $Rarity } }
    $placement += @(
        @{ type = "minecraft:in_square" }
        @{ type = "minecraft:heightmap"; heightmap = $Heightmap }
    )
    $placement += $ExtraPlacement
    $placement += @{ type = "minecraft:biome" }
    Write-Json (Join-Path $outDir "$Name.json") @{ feature = $Configured; placement = $placement }
}

# --- Ground crops & herbs (Materia) ---
Surface-Placed "flora_wild_flax" "materia:wild_flax_patch" 2 6
Surface-Placed "flora_wild_corn" "materia:wild_corn_patch" 2 5
Surface-Placed "flora_wild_peppers" "materia:wild_peppers_patch" 2 6
Surface-Placed "flora_wild_beans" "materia:wild_beans_patch" 2 6
Surface-Placed "flora_wild_squash" "materia:wild_squash_patch" 1 7
Surface-Placed "flora_wild_cotton" "materia:wild_cotton_patch" 1 8
Surface-Placed "flora_wild_rice" "materia:wild_rice_patch" 2 5 "WORLD_SURFACE_WG"
Surface-Placed "flora_tea_bush" "materia:tea_bush_patch" 1 7
Surface-Placed "flora_indigo" "materia:indigo_patch" 1 9
Surface-Placed "flora_esparto" "materia:esparto_patch" 3 4 "MOTION_BLOCKING_NO_LEAVES"

# --- Vanilla tree stand-ins ---
Surface-Placed "flora_oak_tree" "minecraft:oak" 2 5 -ExtraPlacement @(
    (Sapling-Survive "minecraft:oak_sapling" @{ stage = "0" })
)
Surface-Placed "flora_oak_sparse" "minecraft:oak" 1 9 -ExtraPlacement @(
    (Sapling-Survive "minecraft:oak_sapling" @{ stage = "0" })
)
Surface-Placed "flora_birch_tree" "minecraft:birch" 1 8 -ExtraPlacement @(
    (Sapling-Survive "minecraft:birch_sapling" @{ stage = "0" })
)
Surface-Placed "flora_spruce_tree" "minecraft:spruce" 1 9 -ExtraPlacement @(
    (Sapling-Survive "minecraft:spruce_sapling" @{ stage = "0" })
)
Surface-Placed "flora_acacia_tree" "minecraft:acacia" 2 6 -ExtraPlacement @(
    (Sapling-Survive "minecraft:acacia_sapling" @{ stage = "0" })
)

# --- Materia trees ---
Surface-Placed "flora_olive_tree" "materia:olive_tree" 2 5 -ExtraPlacement @(
    (Sapling-Survive "materia:olive_sapling" @{ stage = "0" })
)
Surface-Placed "flora_cypress_tree" "materia:cypress_tree" 0 7 -ExtraPlacement @(
    (Sapling-Survive "materia:cypress_sapling" @{})
)
Surface-Placed "flora_baobab_tree" "materia:baobab_tree" 0 10 -ExtraPlacement @(
    (Sapling-Survive "materia:baobab_sapling" @{ stage = "0" })
)
Surface-Placed "flora_palm_tree" "materia:palm_tree" 0 9 -ExtraPlacement @(
    (Sapling-Survive "materia:palm_sapling" @{})
)
Surface-Placed "flora_rubber_tree" "materia:rubber_tree" 0 10 -ExtraPlacement @(
    (Sapling-Survive "materia:rubber_tree_sapling" @{ stage = "0" })
)
Surface-Placed "flora_maple_tree" "materia:maple_tree" 1 8 -ExtraPlacement @(
    (Sapling-Survive "materia:maple_sapling" @{ stage = "0" })
)

# --- Vines ---
Surface-Placed "flora_wild_grape_vine" "materia:wild_grape_vine" 3 2
Surface-Placed "flora_wild_wisteria_vine" "materia:wild_wisteria_vine" 2 4

Write-Host "Wrote flora placed features to $outDir"
