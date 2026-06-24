package com.torr.terracraft.world.biome;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;

/**
 * Maps vanilla heuristic biomes and climate fallbacks to Terracraft clone biomes on Planet Earth.
 */
public final class TerracraftClimateMapper
{
    private TerracraftClimateMapper()
    {
    }

    public static ResourceKey<Biome> toTerracraftLandBiome(
            ResourceKey<Biome> biomeKey,
            double latitude,
            double longitude
    )
    {
        if (TerracraftBiomes.isTerracraft(biomeKey) || isVanillaOceanOrAlpine(biomeKey))
        {
            return biomeKey;
        }

        String realm = BiomeCloneRegistry.normalizeRealm(null, latitude, longitude);
        return switch (biomeKey.location().getPath())
        {
            case "swamp", "mangrove_swamp" -> TerracraftBiomes.key("floodplain_meadow");
            case "desert", "badlands" -> TerracraftBiomes.key("semi_arid_scrub");
            case "flower_forest" -> inMediterraneanBasin(latitude, longitude)
                    ? TerracraftBiomes.MEDITERRANEAN_SCRUB
                    : TerracraftBiomes.key("forest_" + realm);
            case "forest", "birch_forest", "dark_forest", "windswept_forest", "old_growth_birch_forest",
                 "old_growth_spruce_taiga", "old_growth_pine_taiga" -> inSouthwestUs(latitude, longitude)
                    ? TerracraftBiomes.key("chaparral_nearctic")
                    : inMediterraneanBasin(latitude, longitude)
                    ? TerracraftBiomes.MEDITERRANEAN_SCRUB
                    : TerracraftBiomes.key("forest_" + realm);
            case "taiga", "snowy_taiga" -> TerracraftBiomes.key("taiga_" + realm);
            case "snowy_plains", "ice_spikes" -> TerracraftBiomes.key("tundra_" + realm);
            case "savanna", "savanna_plateau", "windswept_savanna" -> TerracraftBiomes.key("savanna_" + realm);
            case "jungle", "sparse_jungle", "bamboo_jungle" -> TerracraftBiomes.key("jungle_" + realm);
            case "grove", "meadow", "cherry_grove" -> TerracraftBiomes.key("montane_meadow");
            case "sunflower_plains", "plains" -> inSouthwestUs(latitude, longitude)
                    ? TerracraftBiomes.key("semi_arid_scrub")
                    : inMediterraneanBasin(latitude, longitude)
                    ? TerracraftBiomes.MEDITERRANEAN_SCRUB
                    : TerracraftBiomes.key("plains_" + realm);
            default -> TerracraftBiomes.key("plains_" + realm);
        };
    }

    public static boolean isVanillaOceanOrAlpine(ResourceKey<Biome> biomeKey)
    {
        return biomeKey.equals(Biomes.OCEAN)
                || biomeKey.equals(Biomes.DEEP_OCEAN)
                || biomeKey.equals(Biomes.WARM_OCEAN)
                || biomeKey.equals(Biomes.FROZEN_OCEAN)
                || biomeKey.equals(Biomes.DEEP_FROZEN_OCEAN)
                || biomeKey.equals(Biomes.BEACH)
                || biomeKey.equals(Biomes.SNOWY_BEACH)
                || biomeKey.equals(Biomes.STONY_SHORE)
                || biomeKey.equals(Biomes.JAGGED_PEAKS)
                || biomeKey.equals(Biomes.STONY_PEAKS)
                || biomeKey.equals(Biomes.FROZEN_PEAKS)
                || biomeKey.equals(Biomes.SNOWY_SLOPES);
    }

    static boolean inMediterraneanBasin(double latitude, double longitude)
    {
        double absLat = Math.abs(latitude);
        return absLat >= 30.0D && absLat <= 46.0D && longitude >= -10.0D && longitude <= 40.0D;
    }

    static boolean inSouthwestUs(double latitude, double longitude)
    {
        return latitude >= 28.0D && latitude <= 42.0D && longitude >= -125.0D && longitude <= -102.0D;
    }
}
