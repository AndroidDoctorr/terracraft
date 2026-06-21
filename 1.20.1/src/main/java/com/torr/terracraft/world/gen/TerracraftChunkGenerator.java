package com.torr.terracraft.world.gen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.torr.terracraft.config.TerracraftConfig;
import com.torr.terracraft.geo.EarthProjection;
import com.torr.terracraft.geo.ElevationSamplerHolder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.blending.Blender;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class TerracraftChunkGenerator extends ChunkGenerator
{
    public static final Codec<TerracraftChunkGenerator> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    BiomeSource.CODEC.fieldOf("biome_source").forGetter(ChunkGenerator::getBiomeSource)
            ).apply(instance, TerracraftChunkGenerator::new)
    );

    public TerracraftChunkGenerator(BiomeSource biomeSource)
    {
        super(biomeSource);
    }

    @Override
    protected Codec<? extends ChunkGenerator> codec()
    {
        return CODEC;
    }

    @Override
    public void buildSurface(WorldGenRegion region, StructureManager structureManager, RandomState randomState, ChunkAccess chunk)
    {
    }

    @Override
    public void applyCarvers(WorldGenRegion region, long seed, RandomState randomState, BiomeManager biomeManager,
                             StructureManager structureManager, ChunkAccess chunk, GenerationStep.Carving step)
    {
    }

    @Override
    public CompletableFuture<ChunkAccess> fillFromNoise(Executor executor, Blender blender, RandomState randomState,
                                                        StructureManager structureManager, ChunkAccess chunk)
    {
        return CompletableFuture.supplyAsync(() -> {
            fillTerrain(chunk, randomState);
            return chunk;
        }, executor);
    }

    private void fillTerrain(ChunkAccess chunk, RandomState randomState)
    {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        int chunkMinX = chunk.getPos().getMinBlockX();
        int chunkMinZ = chunk.getPos().getMinBlockZ();
        int minY = chunk.getMinBuildHeight();
        int maxY = chunk.getMaxBuildHeight() - 1;
        double seaLevelMeters = TerracraftConfig.seaLevelMeters.get();
        int seaLevelBlockY = TerracraftConfig.seaLevelBlockY.get();

        for (int localX = 0; localX < 16; localX++)
        {
            for (int localZ = 0; localZ < 16; localZ++)
            {
                int worldX = chunkMinX + localX;
                int worldZ = chunkMinZ + localZ;
                double latitude = EarthProjection.blockZToLatitude(worldZ);
                double longitude = EarthProjection.blockXToLongitude(worldX);
                double elevationMeters = ElevationSamplerHolder.get().sampleElevationMeters(latitude, longitude);
                int surfaceY = EarthProjection.elevationMetersToBlockY(elevationMeters);
                Holder<Biome> biome = getBiomeSource().getNoiseBiome(
                        QuartPos.fromBlock(worldX),
                        QuartPos.fromBlock(surfaceY),
                        QuartPos.fromBlock(worldZ),
                        randomState.sampler()
                );

                for (int y = minY; y <= maxY; y++)
                {
                    pos.set(worldX, y, worldZ);
                    chunk.setBlockState(pos, terrainBlock(biome, elevationMeters, seaLevelMeters, seaLevelBlockY, y, surfaceY), false);
                }
            }
        }
    }

    private static BlockState terrainBlock(Holder<Biome> biome, double elevationMeters, double seaLevelMeters,
                                           int seaLevelBlockY, int y, int surfaceY)
    {
        if (y > surfaceY)
        {
            return y <= seaLevelBlockY && elevationMeters <= seaLevelMeters
                    ? Blocks.WATER.defaultBlockState()
                    : Blocks.AIR.defaultBlockState();
        }
        if (y == surfaceY)
        {
            return BiomeSurfaceRules.surfaceBlock(
                    biome,
                    elevationMeters,
                    seaLevelMeters,
                    elevationMeters <= seaLevelMeters
            );
        }
        return BiomeSurfaceRules.subsurfaceBlock(biome, y, surfaceY);
    }

    @Override
    public int getBaseHeight(int x, int z, Heightmap.Types type, LevelHeightAccessor level, RandomState randomState)
    {
        double latitude = EarthProjection.blockZToLatitude(z);
        double longitude = EarthProjection.blockXToLongitude(x);
        double elevationMeters = ElevationSamplerHolder.get().sampleElevationMeters(latitude, longitude);
        return EarthProjection.elevationMetersToBlockY(elevationMeters);
    }

    @Override
    public NoiseColumn getBaseColumn(int x, int z, LevelHeightAccessor level, RandomState randomState)
    {
        int surfaceY = getBaseHeight(x, z, Heightmap.Types.WORLD_SURFACE_WG, level, randomState);
        int minY = level.getMinBuildHeight();
        BlockState[] states = new BlockState[surfaceY - minY + 1];
        for (int y = minY; y <= surfaceY; y++)
        {
            states[y - minY] = y == surfaceY ? Blocks.GRASS_BLOCK.defaultBlockState() : Blocks.STONE.defaultBlockState();
        }
        return new NoiseColumn(minY, states);
    }

    @Override
    public void addDebugScreenInfo(List<String> list, RandomState randomState, BlockPos pos)
    {
        double latitude = EarthProjection.blockZToLatitude(pos.getZ());
        double longitude = EarthProjection.blockXToLongitude(pos.getX());
        double elevationMeters = ElevationSamplerHolder.get().sampleElevationMeters(latitude, longitude);
        list.add(String.format("lat %.4f lon %.4f elev %.1f m", latitude, longitude, elevationMeters));
    }

    @Override
    public void spawnOriginalMobs(WorldGenRegion region)
    {
    }

    @Override
    public int getSeaLevel()
    {
        return TerracraftConfig.seaLevelBlockY.get();
    }

    @Override
    public int getMinY()
    {
        return TerracraftConfig.minWorldY.get();
    }

    @Override
    public int getGenDepth()
    {
        return TerracraftConfig.maxWorldY.get() - TerracraftConfig.minWorldY.get() + 1;
    }
}
