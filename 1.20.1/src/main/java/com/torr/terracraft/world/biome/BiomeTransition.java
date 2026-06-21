package com.torr.terracraft.world.biome;

import com.torr.terracraft.config.FloraPlacementMode;
import com.torr.terracraft.config.PlanetEarthSettingsHolder;
import com.torr.terracraft.geo.EarthProjection;
import com.torr.terracraft.geo.ecoregion.EcoregionInfo;
import com.torr.terracraft.geo.ecoregion.EcoregionSamplerHolder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;

/**
 * Vegetation spillover at ecoregion borders — picks a neighbor clone inside a buffer band
 * so biome edges soften without hard walls.
 */
public final class BiomeTransition
{
    private BiomeTransition()
    {
    }

    public static ResourceKey<Biome> apply(
            long worldSeed,
            int blockX,
            int blockZ,
            double latitude,
            double longitude,
            EcoregionInfo centerEcoregion,
            ResourceKey<Biome> centerBiome,
            FloraPlacementMode floraMode
    )
    {
        int blendBlocks = PlanetEarthSettingsHolder.ecoregionBorderBlendBlocks();
        if (blendBlocks <= 0 || centerEcoregion.ecoId() == 0)
        {
            return centerBiome;
        }

        double degreeStep = blendBlocks / EarthProjection.blocksPerDegree();
        EcoregionInfo north = EcoregionSamplerHolder.get().sample(latitude + degreeStep, longitude);
        EcoregionInfo south = EcoregionSamplerHolder.get().sample(latitude - degreeStep, longitude);
        EcoregionInfo east = EcoregionSamplerHolder.get().sample(latitude, longitude + degreeStep);
        EcoregionInfo west = EcoregionSamplerHolder.get().sample(latitude, longitude - degreeStep);

        EcoregionInfo spillSource = pickSpillSource(worldSeed, blockX, blockZ, centerEcoregion, north, south, east, west);
        if (spillSource == null)
        {
            return centerBiome;
        }

        double spillRoll = transitionHash(worldSeed, blockX, blockZ);
        if (spillRoll > 0.42D)
        {
            return centerBiome;
        }

        ResourceKey<Biome> neighborBiome = BiomeCloneRegistry.resolvePrimary(spillSource, latitude, longitude, floraMode);
        return neighborBiome.equals(centerBiome) ? centerBiome : neighborBiome;
    }

    private static EcoregionInfo pickSpillSource(
            long worldSeed,
            int blockX,
            int blockZ,
            EcoregionInfo center,
            EcoregionInfo north,
            EcoregionInfo south,
            EcoregionInfo east,
            EcoregionInfo west
    )
    {
        EcoregionInfo[] candidates = {north, south, east, west};
        int count = 0;
        for (EcoregionInfo sample : candidates)
        {
            if (sample.ecoId() != 0 && sample.ecoId() != center.ecoId())
            {
                count++;
            }
        }
        if (count == 0)
        {
            return null;
        }

        double pick = transitionHash(worldSeed, blockX + 17, blockZ + 31);
        int target = (int) (pick * count);
        for (EcoregionInfo sample : candidates)
        {
            if (sample.ecoId() != 0 && sample.ecoId() != center.ecoId())
            {
                if (target == 0)
                {
                    return sample;
                }
                target--;
            }
        }
        return null;
    }

    private static double transitionHash(long worldSeed, int blockX, int blockZ)
    {
        long bits = worldSeed ^ (long) blockX * 734287L ^ (long) blockZ * 912271L;
        bits = bits ^ (bits >>> 33);
        bits *= 0xff51afd7ed558ccdL;
        bits ^= bits >>> 33;
        return (bits & 0xFFFFL) / (double) 0xFFFFL;
    }
}
