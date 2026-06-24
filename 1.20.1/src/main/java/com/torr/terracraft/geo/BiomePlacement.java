package com.torr.terracraft.geo;

import com.torr.terracraft.config.FloraPlacementMode;
import com.torr.terracraft.config.FloraPlacementHolder;
import com.torr.terracraft.config.TerracraftConfig;
import com.torr.terracraft.geo.ecoregion.EcoregionInfo;
import com.torr.terracraft.geo.ecoregion.EcoregionSamplerHolder;
import com.torr.terracraft.world.biome.BiomeCloneRegistry;
import com.torr.terracraft.world.biome.GeographicBiomeFallback;
import com.torr.terracraft.world.biome.TerracraftBiomes;
import com.torr.terracraft.world.biome.TerracraftClimateMapper;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;

public final class BiomePlacement
{
    private BiomePlacement()
    {
    }

    public static ResourceKey<Biome> classify(double latitude, double longitude, double elevationMeters)
    {
        return classify(latitude, longitude, elevationMeters, FloraPlacementHolder.get());
    }

    public static ResourceKey<Biome> classify(double latitude, double longitude, double elevationMeters, FloraPlacementMode floraMode)
    {
        ResourceKey<Biome> elevationBiome = elevationOverride(latitude, longitude, elevationMeters);
        if (elevationBiome != null)
        {
            return elevationBiome;
        }

        if (TerracraftConfig.useEcoregionBiomes.get())
        {
            EcoregionInfo ecoregion = EcoregionSamplerHolder.get().sample(latitude, longitude);
            if (ecoregion.ecoId() != 0)
            {
                return BiomeCloneRegistry.resolve(
                        ecoregion,
                        latitude,
                        longitude,
                        floraMode
                );
            }
        }

        java.util.Optional<ResourceKey<Biome>> geographic = GeographicBiomeFallback.at(latitude, longitude, elevationMeters);
        if (geographic.isPresent())
        {
            return geographic.get();
        }

        if (TerracraftConfig.useClimateFallback.get())
        {
            ResourceKey<Biome> climateBiome = ClimateClassifier.classifyLand(latitude, longitude, elevationMeters);
            return TerracraftClimateMapper.toTerracraftLandBiome(climateBiome, latitude, longitude);
        }

        return TerracraftBiomes.PLAINS_PALEARCTIC;
    }

    private static ResourceKey<Biome> elevationOverride(double latitude, double longitude, double elevationMeters)
    {
        double seaLevel = TerracraftConfig.seaLevelMeters.get();
        double absLat = Math.abs(latitude);

        if (elevationMeters <= seaLevel - 200.0D)
        {
            return absLat > 70.0D ? Biomes.DEEP_FROZEN_OCEAN : Biomes.DEEP_OCEAN;
        }
        if (elevationMeters <= seaLevel - 20.0D)
        {
            if (absLat > 70.0D)
            {
                return Biomes.FROZEN_OCEAN;
            }
            return absLat < 30.0D ? Biomes.WARM_OCEAN : Biomes.OCEAN;
        }
        if (elevationMeters <= seaLevel + 4.0D)
        {
            return absLat > 60.0D ? Biomes.SNOWY_BEACH : Biomes.BEACH;
        }
        if (elevationMeters <= seaLevel + 15.0D && absLat > 55.0D)
        {
            return Biomes.STONY_SHORE;
        }
        if (elevationMeters >= 3500.0D)
        {
            return absLat > 45.0D || elevationMeters >= 5000.0D ? Biomes.JAGGED_PEAKS : Biomes.STONY_PEAKS;
        }
        if (elevationMeters >= 2200.0D)
        {
            return absLat > 40.0D ? Biomes.SNOWY_SLOPES : Biomes.GROVE;
        }

        return null;
    }
}
