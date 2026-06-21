package com.torr.terracraft.geo;

import com.torr.terracraft.config.TerracraftConfig;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;

/**
 * Approximate biome placement from latitude, pseudo-rainfall, and real elevation.
 * Not as accurate as Terra 1:1's soil/climate datasets, but captures major global patterns.
 */
public final class ClimateClassifier
{
    private ClimateClassifier()
    {
    }

    public static ResourceKey<Biome> classify(double latitude, double longitude, double elevationMeters)
    {
        return BiomePlacement.classify(latitude, longitude, elevationMeters);
    }

    public static ResourceKey<Biome> classifyLand(double latitude, double longitude, double elevationMeters)
    {
        double absLat = Math.abs(latitude);
        double temperature = estimateTemperature(absLat, elevationMeters);
        double rainfall = estimateRainfall(latitude, longitude, elevationMeters, absLat);

        if (elevationMeters >= 1400.0D && temperature < 0.35D)
        {
            return Biomes.GROVE;
        }

        if (temperature < 0.12D)
        {
            return elevationMeters > 300.0D ? Biomes.ICE_SPIKES : Biomes.SNOWY_PLAINS;
        }
        if (temperature < 0.25D)
        {
            if (rainfall > 0.45D)
            {
                return elevationMeters > 600.0D ? Biomes.SNOWY_TAIGA : Biomes.TAIGA;
            }
            return Biomes.SNOWY_PLAINS;
        }

        if (temperature > 0.72D)
        {
            if (rainfall > 0.65D)
            {
                if (rainfall > 0.82D)
                {
                    return pseudoRandom(latitude, longitude) > 0.92D ? Biomes.BAMBOO_JUNGLE : Biomes.JUNGLE;
                }
                return Biomes.SPARSE_JUNGLE;
            }
            if (rainfall < 0.35D)
            {
                return pseudoRandom(latitude, longitude) > 0.97D ? Biomes.BADLANDS : Biomes.DESERT;
            }
            return rainfall > 0.5D ? Biomes.SAVANNA : Biomes.PLAINS;
        }

        if (rainfall < 0.28D)
        {
            return absLat > 20.0D ? Biomes.BADLANDS : Biomes.DESERT;
        }
        if (rainfall > 0.72D && elevationMeters < 220.0D && absLat < 55.0D)
        {
            return Biomes.SWAMP;
        }
        if (rainfall > 0.58D)
        {
            double roll = pseudoRandom(latitude, longitude);
            if (roll > 0.82D)
            {
                return Biomes.FLOWER_FOREST;
            }
            if (roll > 0.65D)
            {
                return Biomes.BIRCH_FOREST;
            }
            if (roll > 0.45D && temperature < 0.55D)
            {
                return Biomes.DARK_FOREST;
            }
            return Biomes.FOREST;
        }

        return pseudoRandom(latitude, longitude) > 0.7D ? Biomes.SUNFLOWER_PLAINS : Biomes.PLAINS;
    }

    private static double estimateTemperature(double absLat, double elevationMeters)
    {
        double latFactor = 1.0D - Math.min(1.0D, absLat / 90.0D);
        double lapse = Math.max(0.0D, elevationMeters - TerracraftConfig.seaLevelMeters.get()) / 9000.0D;
        return Math.max(0.0D, Math.min(1.0D, latFactor - lapse));
    }

    private static double estimateRainfall(double latitude, double longitude, double elevationMeters, double absLat)
    {
        double coastalBoost = elevationMeters < 300.0D ? 0.12D : 0.0D;
        double orographic = elevationMeters > 800.0D ? 0.18D : 0.0D;
        double rainShadow = elevationMeters > 1200.0D ? -0.12D : 0.0D;
        double seasonalBand = 0.5D + 0.22D * Math.sin(Math.toRadians(longitude * 1.7D + latitude * 0.9D));
        double subtropicalDry = absLat > 18.0D && absLat < 35.0D ? -0.18D : 0.0D;
        double noise = 0.12D * Math.sin(Math.toRadians(longitude * 3.1D)) * Math.cos(Math.toRadians(latitude * 2.4D));
        return Math.max(0.05D, Math.min(0.95D, seasonalBand + coastalBoost + orographic + rainShadow + subtropicalDry + noise));
    }

    private static double pseudoRandom(double latitude, double longitude)
    {
        long bits = Double.doubleToLongBits(latitude * 9973.0D + longitude * 7919.0D);
        bits = bits ^ (bits >>> 33);
        bits *= 0xff51afd7ed558ccdL;
        bits ^= bits >>> 33;
        return (bits & 0xFFFFL) / (double) 0xFFFFL;
    }
}
