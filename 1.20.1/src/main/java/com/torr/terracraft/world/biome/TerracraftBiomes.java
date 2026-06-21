package com.torr.terracraft.world.biome;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class TerracraftBiomes
{
    private static final List<ResourceKey<Biome>> ALL = new ArrayList<>();

    public static final ResourceKey<Biome> MEDITERRANEAN_SCRUB = register("mediterranean_scrub");
    public static final ResourceKey<Biome> TEMPERATE_STEPPE = register("temperate_steppe");
    public static final ResourceKey<Biome> PLAINS_PALEARCTIC = register("plains_palearctic");
    public static final ResourceKey<Biome> FOREST_PALEARCTIC = register("forest_palearctic");
    public static final ResourceKey<Biome> PLAINS_NEOTROPICAL = register("plains_neotropical");
    public static final ResourceKey<Biome> FOREST_NEOTROPICAL = register("forest_neotropical");

    private TerracraftBiomes()
    {
    }

    public static ResourceKey<Biome> register(String path)
    {
        ResourceKey<Biome> key = ResourceKey.create(Registries.BIOME, new ResourceLocation("terracraft", path));
        registerKey(key);
        return key;
    }

    public static ResourceKey<Biome> key(String id)
    {
        if (id.contains(":"))
        {
            ResourceLocation location = new ResourceLocation(id);
            ResourceKey<Biome> key = ResourceKey.create(Registries.BIOME, location);
            registerKey(key);
            return key;
        }
        return register(id);
    }

    private static void registerKey(ResourceKey<Biome> key)
    {
        if (!ALL.contains(key))
        {
            ALL.add(key);
        }
    }

    public static List<ResourceKey<Biome>> allRegistered()
    {
        return Collections.unmodifiableList(ALL);
    }

    public static boolean isTerracraft(ResourceKey<Biome> biomeKey)
    {
        return "terracraft".equals(biomeKey.location().getNamespace());
    }
}
