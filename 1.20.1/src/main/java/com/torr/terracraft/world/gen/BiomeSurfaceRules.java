package com.torr.terracraft.world.gen;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public final class BiomeSurfaceRules
{
    private BiomeSurfaceRules()
    {
    }

    public static BlockState surfaceBlock(Holder<Biome> biome, double elevationMeters, double seaLevelMeters, boolean underwater)
    {
        if (underwater)
        {
            return Blocks.SAND.defaultBlockState();
        }

        ResourceKey<Biome> key = biome.unwrapKey().orElse(Biomes.PLAINS);
        if (is(key, Biomes.SNOWY_PLAINS, Biomes.SNOWY_TAIGA, Biomes.ICE_SPIKES, Biomes.SNOWY_SLOPES, Biomes.GROVE))
        {
            return Blocks.SNOW_BLOCK.defaultBlockState();
        }
        if (is(key, Biomes.JAGGED_PEAKS, Biomes.STONY_PEAKS, Biomes.STONY_SHORE))
        {
            return Blocks.STONE.defaultBlockState();
        }
        if (is(key, Biomes.DESERT))
        {
            return Blocks.SAND.defaultBlockState();
        }
        if (is(key, Biomes.BADLANDS))
        {
            return Blocks.RED_SAND.defaultBlockState();
        }
        if (is(key, Biomes.BEACH, Biomes.SNOWY_BEACH))
        {
            return Blocks.SAND.defaultBlockState();
        }
        if (is(key, Biomes.SWAMP))
        {
            return Blocks.GRASS_BLOCK.defaultBlockState();
        }
        return Blocks.GRASS_BLOCK.defaultBlockState();
    }

    public static BlockState subsurfaceBlock(Holder<Biome> biome, int y, int surfaceY)
    {
        ResourceKey<Biome> key = biome.unwrapKey().orElse(Biomes.PLAINS);
        if (is(key, Biomes.DESERT, Biomes.BEACH, Biomes.BADLANDS))
        {
            return Blocks.SAND.defaultBlockState();
        }
        if (is(key, Biomes.SNOWY_PLAINS, Biomes.SNOWY_TAIGA, Biomes.ICE_SPIKES, Biomes.SNOWY_SLOPES, Biomes.GROVE, Biomes.JAGGED_PEAKS))
        {
            return y == surfaceY ? Blocks.SNOW_BLOCK.defaultBlockState() : Blocks.STONE.defaultBlockState();
        }
        if (y >= surfaceY - 3)
        {
            return Blocks.DIRT.defaultBlockState();
        }
        return y < 0 ? Blocks.DEEPSLATE.defaultBlockState() : Blocks.STONE.defaultBlockState();
    }

    private static boolean is(ResourceKey<Biome> key, ResourceKey<Biome>... candidates)
    {
        for (ResourceKey<Biome> candidate : candidates)
        {
            if (candidate.equals(key))
            {
                return true;
            }
        }
        return false;
    }
}
