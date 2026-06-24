package com.torr.terracraft.world.biome;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.torr.terracraft.terracraft;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public final class TerracraftBiomes
{
    private static final List<ResourceKey<Biome>> POSSIBLE_BIOMES = loadRequiredBiomesWithFallback();
    private static final Set<ResourceKey<Biome>> POSSIBLE_BIOME_SET = Set.copyOf(POSSIBLE_BIOMES);

    public static final ResourceKey<Biome> MEDITERRANEAN_SCRUB = key("mediterranean_scrub");
    public static final ResourceKey<Biome> TEMPERATE_STEPPE = key("temperate_steppe");
    public static final ResourceKey<Biome> PLAINS_PALEARCTIC = key("plains_palearctic");
    public static final ResourceKey<Biome> FOREST_PALEARCTIC = key("forest_palearctic");
    public static final ResourceKey<Biome> PLAINS_NEOTROPICAL = key("plains_neotropical");
    public static final ResourceKey<Biome> FOREST_NEOTROPICAL = key("forest_neotropical");

    private TerracraftBiomes()
    {
    }

    public static ResourceKey<Biome> key(String id)
    {
        if (id.contains(":"))
        {
            ResourceLocation location = new ResourceLocation(id);
            return ResourceKey.create(Registries.BIOME, location);
        }
        return ResourceKey.create(Registries.BIOME, new ResourceLocation(terracraft.MOD_ID, id));
    }

    public static List<ResourceKey<Biome>> allRegistered()
    {
        return POSSIBLE_BIOMES;
    }

    public static boolean isRegistered(ResourceKey<Biome> biomeKey)
    {
        return POSSIBLE_BIOME_SET.contains(biomeKey);
    }

    public static boolean isTerracraft(ResourceKey<Biome> biomeKey)
    {
        return terracraft.MOD_ID.equals(biomeKey.location().getNamespace());
    }

    private static List<ResourceKey<Biome>> loadRequiredBiomes()
    {
        String resourcePath = "/data/terracraft/ecoregion/required_biomes.txt";
        try (InputStream stream = TerracraftBiomes.class.getResourceAsStream(resourcePath))
        {
            if (stream == null)
            {
                terracraft.LOGGER.error("Missing {} — Planet Earth biomes will not resolve correctly", resourcePath);
                return List.of();
            }

            List<ResourceKey<Biome>> parsed = new ArrayList<>();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8)))
            {
                String line;
                while ((line = reader.readLine()) != null)
                {
                    String trimmed = line.trim();
                    if (trimmed.isEmpty() || trimmed.startsWith("#"))
                    {
                        continue;
                    }
                    parsed.add(key(trimmed));
                }
            }
            return Collections.unmodifiableList(parsed);
        }
        catch (Exception exception)
        {
            terracraft.LOGGER.error("Failed to load required biome list from {}: {}", resourcePath, exception.toString());
            return List.of();
        }
    }

    private static List<ResourceKey<Biome>> loadEarthTagFallback()
    {
        String resourcePath = "/data/terracraft/tags/worldgen/biome/earth.json";
        try (InputStream stream = TerracraftBiomes.class.getResourceAsStream(resourcePath))
        {
            if (stream == null)
            {
                return List.of();
            }

            JsonObject object = JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8)).getAsJsonObject();
            JsonArray values = object.getAsJsonArray("values");
            if (values == null)
            {
                return List.of();
            }

            List<ResourceKey<Biome>> parsed = new ArrayList<>(values.size());
            for (JsonElement element : values)
            {
                parsed.add(key(element.getAsString()));
            }
            terracraft.LOGGER.warn("Loaded {} Terracraft biomes from earth tag fallback", parsed.size());
            return Collections.unmodifiableList(parsed);
        }
        catch (Exception exception)
        {
            terracraft.LOGGER.error("Failed to load earth tag biome fallback: {}", exception.toString());
            return List.of();
        }
    }

    static List<ResourceKey<Biome>> loadRequiredBiomesWithFallback()
    {
        List<ResourceKey<Biome>> fromList = loadRequiredBiomes();
        if (!fromList.isEmpty())
        {
            terracraft.LOGGER.info("Loaded {} Terracraft clone biomes from required_biomes.txt", fromList.size());
            return fromList;
        }

        List<ResourceKey<Biome>> fromTag = loadEarthTagFallback();
        if (!fromTag.isEmpty())
        {
            return fromTag;
        }

        terracraft.LOGGER.error("No Terracraft clone biomes loaded — vegetation will not spawn on custom biomes");
        return List.of();
    }
}
