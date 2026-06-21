package com.torr.terracraft.geo.ecoregion;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;

/**
 * Maps WWF TEOW biome codes (BIOME / G200_BIOME fields) to vanilla Minecraft biomes.
 * See Olson et al. 2001 biome numbering (1-14).
 */
public final class EcoregionBiomeMapper
{
    private EcoregionBiomeMapper()
    {
    }

    public static ResourceKey<Biome> map(EcoregionInfo ecoregion)
    {
        if (ecoregion == null || ecoregion.ecoId() == 0)
        {
            return Biomes.PLAINS;
        }

        int biomeCode = chooseBiomeCode(ecoregion);
        return switch (biomeCode)
        {
            case 1 -> Biomes.JUNGLE;
            case 2 -> Biomes.SPARSE_JUNGLE;
            case 3 -> Biomes.JUNGLE;
            case 4 -> Biomes.FOREST;
            case 5 -> Biomes.TAIGA;
            case 6 -> Biomes.TAIGA;
            case 7 -> Biomes.SAVANNA;
            case 8 -> Biomes.PLAINS;
            case 9 -> Biomes.SWAMP;
            case 10 -> Biomes.GROVE;
            case 11 -> Biomes.SNOWY_PLAINS;
            case 12 -> Biomes.FLOWER_FOREST;
            case 13 -> Biomes.DESERT;
            case 14 -> Biomes.MANGROVE_SWAMP;
            default -> Biomes.PLAINS;
        };
    }

    private static int chooseBiomeCode(EcoregionInfo ecoregion)
    {
        if (ecoregion.g200Biome() >= 1 && ecoregion.g200Biome() <= 14)
        {
            return ecoregion.g200Biome();
        }
        if (ecoregion.biomeCode() >= 1 && ecoregion.biomeCode() <= 14)
        {
            return ecoregion.biomeCode();
        }
        return 0;
    }
}
