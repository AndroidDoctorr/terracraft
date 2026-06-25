package com.torr.terracraft.world.biome;

import com.torr.terracraft.config.FloraPlacementMode;
import com.torr.terracraft.config.PlanetEarthSettingsHolder;
import com.torr.terracraft.config.TerracraftConfig;
import com.torr.terracraft.geo.ecoregion.EcoregionInfo;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;

/**
 * Softens ecoregion borders with distance-weighted spillover, dedicated transition clones,
 * and rain-shadow biome nudges.
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
            FloraPlacementMode floraMode,
            double elevationMeters
    )
    {
        ResourceKey<Biome> resolved = centerBiome;

        if (TerracraftConfig.ecoregionBorderBlendEnabled.get() && centerEcoregion.ecoId() != 0)
        {
            resolved = applyBorderBlend(
                    worldSeed,
                    blockX,
                    blockZ,
                    latitude,
                    longitude,
                    centerEcoregion,
                    centerBiome,
                    floraMode
            );
        }

        return RainShadowPlacement.apply(resolved, latitude, longitude, elevationMeters);
    }

    private static ResourceKey<Biome> applyBorderBlend(
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
        EcoregionBorderSampler.BorderSample border = EcoregionBorderSampler.sample(
                blockX,
                blockZ,
                latitude,
                longitude,
                centerEcoregion
        );
        if (!border.isBorder())
        {
            return centerBiome;
        }

        ResourceKey<Biome> neighborBiome = BiomeCloneRegistry.resolvePrimary(
                border.neighborEcoregion(),
                latitude,
                longitude,
                floraMode
        );
        if (neighborBiome.equals(centerBiome))
        {
            return centerBiome;
        }

        double roll = transitionHash(worldSeed, blockX, blockZ);
        double spillChance = border.strength() * TerracraftConfig.ecoregionBorderSpillWeight.get();
        if (roll > spillChance)
        {
            return centerBiome;
        }

        if (TerracraftConfig.ecoregionBorderTransitionEnabled.get())
        {
            BiomeArchetype centerArchetype = BiomeArchetype.fromBiome(centerBiome);
            BiomeArchetype neighborArchetype = BiomeArchetype.fromBiome(neighborBiome);
            ResourceKey<Biome> transition = BiomeTransitionRegistry.resolve(centerArchetype, neighborArchetype);
            if (transition != null && TerracraftBiomes.isRegistered(transition))
            {
                double transitionRoll = transitionHash(worldSeed, blockX + 29, blockZ + 53);
                if (transitionRoll < border.strength() * 0.85D)
                {
                    return transition;
                }
            }
        }

        return neighborBiome;
    }

    static double transitionHash(long worldSeed, int blockX, int blockZ)
    {
        long bits = worldSeed ^ (long) blockX * 734287L ^ (long) blockZ * 912271L;
        bits = bits ^ (bits >>> 33);
        bits *= 0xff51afd7ed558ccdL;
        bits ^= bits >>> 33;
        return (bits & 0xFFFFL) / (double) 0xFFFFL;
    }
}
