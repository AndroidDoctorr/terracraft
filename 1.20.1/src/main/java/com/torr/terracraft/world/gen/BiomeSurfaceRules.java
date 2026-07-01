package com.torr.terracraft.world.gen;

import com.torr.terracraft.integration.MateriaIntegration;
import com.torr.terracraft.terracraft;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public final class BiomeSurfaceRules
{
    private static final BlockState PLAYA_FALLBACK = Blocks.SANDSTONE.defaultBlockState();
    private static final double PLAYA_SALT_MAX_ELEVATION_M = 1340.0D;

    private BiomeSurfaceRules()
    {
    }

    public static BlockState surfaceBlock(
            Holder<Biome> biome,
            ChunkSurfaceSpillField spillField,
            int localX,
            int localZ,
            double latitude,
            double longitude,
            double elevationMeters,
            double seaLevelMeters,
            boolean underwater,
            int maxSlopeBlocks
    )
    {
        if (underwater)
        {
            return Blocks.SAND.defaultBlockState();
        }

        BlockState spilled = spilledSurface(spillField, localX, localZ);
        if (spilled != null)
        {
            return spilled;
        }

        ResourceKey<Biome> key = biome.unwrapKey().orElse(Biomes.PLAINS);
        if (isTerracraft(key, "playa_salt"))
        {
            return MateriaIntegration.saltBlockOr(PLAYA_FALLBACK);
        }
        if (isTerracraft(key, "desert_arid"))
        {
            return Blocks.SAND.defaultBlockState();
        }
        if (isTerracraft(key, "semi_arid_scrub"))
        {
            return semiAridSurface(elevationMeters, maxSlopeBlocks);
        }
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

    public static BlockState subsurfaceBlock(
            Holder<Biome> biome,
            ChunkSurfaceSpillField spillField,
            int localX,
            int localZ,
            double latitude,
            double longitude,
            int y,
            int surfaceY,
            double elevationMeters,
            int maxSlopeBlocks
    )
    {
        BlockState spilled = spilledSubsurface(spillField, localX, localZ, y, surfaceY);
        if (spilled != null)
        {
            return spilled;
        }

        ResourceKey<Biome> key = biome.unwrapKey().orElse(Biomes.PLAINS);
        if (isTerracraft(key, "playa_salt"))
        {
            if (y >= surfaceY - 4)
            {
                return MateriaIntegration.saltBlockOr(PLAYA_FALLBACK);
            }
            return PLAYA_FALLBACK;
        }
        if (isTerracraft(key, "desert_arid"))
        {
            if (y >= surfaceY - 5)
            {
                return Blocks.SAND.defaultBlockState();
            }
            return Blocks.SANDSTONE.defaultBlockState();
        }
        if (isTerracraft(key, "semi_arid_scrub"))
        {
            return semiAridSubsurface(y, surfaceY, elevationMeters, maxSlopeBlocks);
        }
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

    private static BlockState spilledSurface(ChunkSurfaceSpillField spillField, int localX, int localZ)
    {
        if (spillField == null)
        {
            return null;
        }

        ChunkSurfaceSpillField.Cover cover = spillField.cover(localX, localZ);
        return ChunkSurfaceSpillField.blockFor(cover, localX, localZ);
    }

    private static BlockState spilledSubsurface(
            ChunkSurfaceSpillField spillField,
            int localX,
            int localZ,
            int y,
            int surfaceY
    )
    {
        if (spillField == null)
        {
            return null;
        }

        ChunkSurfaceSpillField.Cover cover = spillField.cover(localX, localZ);
        if (cover == ChunkSurfaceSpillField.Cover.NONE)
        {
            return null;
        }

        if (y >= surfaceY - 4)
        {
            BlockState surface = ChunkSurfaceSpillField.blockFor(cover, localX, localZ);
            if (surface != null)
            {
                return surface;
            }
        }

        return switch (cover)
        {
            case SALT -> y >= surfaceY - 4
                    ? MateriaIntegration.saltBlockOr(PLAYA_FALLBACK)
                    : PLAYA_FALLBACK;
            case SAND, RED_SAND -> Blocks.SAND.defaultBlockState();
            case SANDSTONE -> y >= surfaceY - 3
                    ? Blocks.SANDSTONE.defaultBlockState()
                    : Blocks.SAND.defaultBlockState();
            case TERRACOTTA -> y == surfaceY
                    ? ChunkSurfaceSpillField.blockFor(cover, localX, localZ)
                    : Blocks.RED_SAND.defaultBlockState();
            case STONE -> y >= surfaceY - 2 ? Blocks.GRAVEL.defaultBlockState() : Blocks.STONE.defaultBlockState();
            case COARSE_DIRT -> y >= surfaceY - 3 ? Blocks.DIRT.defaultBlockState() : Blocks.STONE.defaultBlockState();
            case BASALT -> y >= surfaceY - 2
                    ? net.minecraft.world.level.block.Blocks.BASALT.defaultBlockState()
                    : net.minecraft.world.level.block.Blocks.TUFF.defaultBlockState();
            case TUFF -> y >= surfaceY - 3
                    ? net.minecraft.world.level.block.Blocks.TUFF.defaultBlockState()
                    : net.minecraft.world.level.block.Blocks.STONE.defaultBlockState();
            case PODZOL -> y >= surfaceY - 4
                    ? net.minecraft.world.level.block.Blocks.PODZOL.defaultBlockState()
                    : Blocks.DIRT.defaultBlockState();
            case GRAVEL -> y >= surfaceY - 2
                    ? net.minecraft.world.level.block.Blocks.GRAVEL.defaultBlockState()
                    : net.minecraft.world.level.block.Blocks.STONE.defaultBlockState();
            case NONE -> null;
        };
    }

    private static BlockState semiAridSurface(double elevationMeters, int maxSlopeBlocks)
    {
        if (maxSlopeBlocks >= 18 || elevationMeters > 2800.0D)
        {
            return Blocks.STONE.defaultBlockState();
        }
        if (maxSlopeBlocks >= 10)
        {
            return Blocks.COARSE_DIRT.defaultBlockState();
        }
        return Blocks.GRASS_BLOCK.defaultBlockState();
    }

    private static BlockState semiAridSubsurface(int y, int surfaceY, double elevationMeters, int maxSlopeBlocks)
    {
        if (maxSlopeBlocks >= 18 || elevationMeters > 2800.0D)
        {
            return y >= surfaceY - 2 ? Blocks.GRAVEL.defaultBlockState() : Blocks.STONE.defaultBlockState();
        }
        if (y >= surfaceY - 4)
        {
            return Blocks.DIRT.defaultBlockState();
        }
        return Blocks.STONE.defaultBlockState();
    }

    private static boolean isTerracraft(ResourceKey<Biome> key, String path)
    {
        return terracraft.MOD_ID.equals(key.location().getNamespace())
                && path.equals(key.location().getPath());
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
