package com.torr.terracraft.world.biome;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.torr.terracraft.config.FloraPlacementMode;
import com.torr.terracraft.geo.ecoregion.EcoregionInfo;
import com.torr.terracraft.terracraft;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public final class BiomeCloneRegistry
{
    private static Map<Integer, ResourceKey<Biome>> historicalByEcoId = Map.of();
    private static Map<Integer, ResourceKey<Biome>> biomeFloraByEcoId = Map.of();
    private static boolean loaded;

    private BiomeCloneRegistry()
    {
    }

    public static void load()
    {
        if (loaded)
        {
            return;
        }

        historicalByEcoId = loadMap("ecoregion/clone_map_historical.json");
        biomeFloraByEcoId = loadMap("ecoregion/clone_map_biome.json");
        loaded = true;
        terracraft.LOGGER.info("Loaded Terracraft biome clone maps ({} historical, {} biome-flora ECO_ID entries)",
                historicalByEcoId.size(), biomeFloraByEcoId.size());
    }

    public static ResourceKey<Biome> resolve(
            EcoregionInfo ecoregion,
            double latitude,
            double longitude,
            FloraPlacementMode mode,
            double elevationMeters
    )
    {
        load();

        java.util.Optional<ResourceKey<Biome>> override = EcoregionBiomeOverrides.resolve(
                ecoregion,
                latitude,
                longitude,
                elevationMeters
        );
        if (override.isPresent())
        {
            return override.get();
        }

        Map<Integer, ResourceKey<Biome>> activeMap = mode == FloraPlacementMode.BIOME ? biomeFloraByEcoId : historicalByEcoId;
        if (ecoregion.ecoId() != 0)
        {
            ResourceKey<Biome> mapped = activeMap.get(ecoregion.ecoId());
            if (mapped != null)
            {
                return mapped;
            }
        }

        return fallbackClone(ecoregion, latitude, longitude);
    }

    static ResourceKey<Biome> resolvePrimary(
            EcoregionInfo ecoregion,
            double latitude,
            double longitude,
            FloraPlacementMode mode,
            double elevationMeters
    )
    {
        return resolve(ecoregion, latitude, longitude, mode, elevationMeters);
    }

    private static ResourceKey<Biome> fallbackClone(EcoregionInfo ecoregion, double latitude, double longitude)
    {
        String realm = normalizeRealm(ecoregion.realm(), latitude, longitude);
        int biomeCode = ecoregion.effectiveBiomeCode();

        return switch (biomeCode)
        {
            case 12 -> TerracraftBiomes.MEDITERRANEAN_SCRUB;
            case 8 -> TerracraftBiomes.TEMPERATE_STEPPE;
            case 4 -> TerracraftBiomes.key("forest_" + realm);
            case 1, 2, 3 -> TerracraftBiomes.key("jungle_" + realm);
            case 7 -> TerracraftBiomes.key("savanna_" + realm);
            case 5, 6 -> TerracraftBiomes.key("taiga_" + realm);
            case 11 -> TerracraftBiomes.key("tundra_" + realm);
            case 9 -> TerracraftBiomes.key("floodplain_meadow");
            case 10 -> TerracraftBiomes.key("montane_meadow");
            case 13 -> TerracraftBiomes.key("desert_arid");
            case 14 -> TerracraftBiomes.key("mangrove_coastal");
            default -> realm.equals("neotropical") || realm.equals("nearctic")
                    ? TerracraftBiomes.key("plains_" + realm)
                    : TerracraftBiomes.PLAINS_PALEARCTIC;
        };
    }

    static String normalizeRealm(String realm, double latitude, double longitude)
    {
        if (realm != null && !realm.isBlank())
        {
            return switch (realm.trim())
            {
                case "Nearctic", "NA" -> "nearctic";
                case "Neotropical", "NT" -> "neotropical";
                case "Afrotropical", "AT" -> "afrotropical";
                case "Indo-Malayan", "IM" -> "indomalayan";
                case "Australasian", "AA" -> "australasian";
                case "Oceania", "OC" -> "oceania";
                case "Antarctic", "AN" -> "antarctic";
                case "Palearctic", "PA" -> "palearctic";
                default -> "palearctic";
            };
        }

        if (latitude < -60.0D)
        {
            return "antarctic";
        }
        if (longitude >= -170.0D && longitude < -30.0D)
        {
            return latitude >= 23.5D ? "nearctic" : "neotropical";
        }
        if (latitude >= 35.0D && latitude <= 72.0D && longitude >= -25.0D && longitude < 45.0D)
        {
            return "palearctic";
        }
        if (latitude >= -35.0D && latitude < 35.0D && longitude >= -20.0D && longitude < 55.0D)
        {
            return "afrotropical";
        }
        if (latitude >= -50.0D && latitude < 15.0D && longitude >= 110.0D && longitude < 180.0D)
        {
            return "australasian";
        }
        if (latitude >= -15.0D && latitude < 30.0D && longitude >= 60.0D && longitude < 150.0D)
        {
            return "indomalayan";
        }
        return "palearctic";
    }

    private static Map<Integer, ResourceKey<Biome>> loadMap(String resourcePath)
    {
        String fullPath = "/data/terracraft/" + resourcePath;
        try (InputStream stream = BiomeCloneRegistry.class.getResourceAsStream(fullPath))
        {
            if (stream == null)
            {
                terracraft.LOGGER.warn("Missing biome clone map {}", fullPath);
                return Map.of();
            }

            JsonObject object = JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8)).getAsJsonObject();
            Map<Integer, ResourceKey<Biome>> parsed = new HashMap<>(object.size());
            for (Map.Entry<String, JsonElement> entry : object.entrySet())
            {
                parsed.put(Integer.parseInt(entry.getKey()), TerracraftBiomes.key(entry.getValue().getAsString()));
            }
            return Map.copyOf(parsed);
        }
        catch (Exception exception)
        {
            terracraft.LOGGER.warn("Failed to load biome clone map {}: {}", fullPath, exception.toString());
            return Map.of();
        }
    }
}
