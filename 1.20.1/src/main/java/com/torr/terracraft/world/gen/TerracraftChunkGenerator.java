package com.torr.terracraft.world.gen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.torr.terracraft.config.TerracraftConfig;
import com.torr.terracraft.geo.ChunkElevationField;
import com.torr.terracraft.geo.ChunkGeoPrefetch;
import com.torr.terracraft.geo.EarthProjection;
import com.torr.terracraft.geo.ElevationSamplerHolder;
import com.torr.terracraft.geo.TerrainElevationMapper;
import com.torr.terracraft.world.PlanetEarthSettingsHelper;
import com.torr.terracraft.world.biome.BiomeVariantDecorator;
import com.torr.terracraft.world.gen.ShorelineSurface;
import com.torr.terracraft.world.gen.TerracraftBiomeSource;
import com.torr.terracraft.world.gen.WaterColumnPlan;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
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

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class TerracraftChunkGenerator extends ChunkGenerator
{
    private static final Set<Heightmap.Types> TERRAIN_HEIGHTMAPS = EnumSet.of(
            Heightmap.Types.OCEAN_FLOOR,
            Heightmap.Types.OCEAN_FLOOR_WG,
            Heightmap.Types.WORLD_SURFACE,
            Heightmap.Types.WORLD_SURFACE_WG,
            Heightmap.Types.MOTION_BLOCKING,
            Heightmap.Types.MOTION_BLOCKING_NO_LEAVES
    );

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
        primeTerrainHeightmaps(chunk);
    }

    @Override
    public void applyBiomeDecoration(WorldGenLevel level, ChunkAccess chunk, StructureManager structureManager)
    {
        syncWorldSettings();
        primeTerrainHeightmaps(chunk);
        super.applyBiomeDecoration(level, chunk, structureManager);
        long worldSeed = level.getSeed();
        BiomeVariantDecorator.apply(level, chunk, worldSeed, level.getLevel().getChunkSource().getGenerator());
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
            syncWorldSettings();
            ChunkGeoPrefetch.prefetch(chunk.getPos().getMinBlockX(), chunk.getPos().getMinBlockZ());
            fillTerrain(chunk, randomState);
            primeTerrainHeightmaps(chunk);
            return chunk;
        }, executor);
    }

    private void syncWorldSettings()
    {
        if (getBiomeSource() instanceof TerracraftBiomeSource source)
        {
            PlanetEarthSettingsHelper.syncFromBiomeSource(source);
        }
    }

    private static void primeTerrainHeightmaps(ChunkAccess chunk)
    {
        Heightmap.primeHeightmaps(chunk, TERRAIN_HEIGHTMAPS);
    }

    private void fillTerrain(ChunkAccess chunk, RandomState randomState)
    {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        int chunkMinX = chunk.getPos().getMinBlockX();
        int chunkMinZ = chunk.getPos().getMinBlockZ();
        int minY = chunk.getMinBuildHeight();
        int maxY = chunk.getMaxBuildHeight() - 1;
        double seaLevelMeters = TerracraftConfig.seaLevelMeters.get();
        ChunkElevationField field = ChunkElevationField.sample(chunkMinX, chunkMinZ);
        ChunkSurfaceSpillField spillField = ChunkSurfaceSpillField.build(field, chunkMinX, chunkMinZ);

        for (int localX = 0; localX < 16; localX++)
        {
            for (int localZ = 0; localZ < 16; localZ++)
            {
                int worldX = chunkMinX + localX;
                int worldZ = chunkMinZ + localZ;
                double elevationMeters = field.centerMeters(localX, localZ);
                double latitude = EarthProjection.blockZToLatitude(worldZ);
                double longitude = EarthProjection.blockXToLongitude(worldX);
                WaterColumnPlan waterPlan = field.waterPlan(localX, localZ);
                int floorY = waterPlan.floorY();
                int waterTopY = waterPlan.waterTopY();
                int maxSlopeBlocks = field.maxNeighborSlopeBlocks(localX, localZ);
                boolean coastalShore = field.adjacentToWater(localX, localZ);
                boolean underwater = waterPlan.hasWater()
                        || elevationMeters <= seaLevelMeters;
                Holder<Biome> biome = getBiomeSource().getNoiseBiome(
                        QuartPos.fromBlock(worldX),
                        QuartPos.fromBlock(floorY),
                        QuartPos.fromBlock(worldZ),
                        randomState.sampler()
                );

                for (int y = minY; y <= maxY; y++)
                {
                    pos.set(worldX, y, worldZ);
                    BlockState state = terrainBlock(
                            biome,
                            spillField,
                            localX,
                            localZ,
                            latitude,
                            longitude,
                            elevationMeters,
                            seaLevelMeters,
                            y,
                            floorY,
                            waterTopY,
                            maxSlopeBlocks,
                            underwater,
                            coastalShore
                    );
                    boolean notify = y == floorY;
                    chunk.setBlockState(pos, state, notify);
                }
            }
        }
    }

    private static BlockState terrainBlock(
            Holder<Biome> biome,
            ChunkSurfaceSpillField spillField,
            int localX,
            int localZ,
            double latitude,
            double longitude,
            double elevationMeters,
            double seaLevelMeters,
            int y,
            int floorY,
            int waterTopY,
            int maxSlopeBlocks,
            boolean underwater,
            boolean coastalShore
    )
    {
        if (y > floorY)
        {
            if (waterTopY != Integer.MIN_VALUE && y <= waterTopY)
            {
                return Blocks.WATER.defaultBlockState();
            }
            return Blocks.AIR.defaultBlockState();
        }
        if (y == floorY)
        {
            boolean seafloor = waterTopY != Integer.MIN_VALUE && waterTopY > floorY;
            return ShorelineSurface.surfaceBlock(
                    biome,
                    spillField,
                    localX,
                    localZ,
                    latitude,
                    longitude,
                    elevationMeters,
                    seaLevelMeters,
                    floorY,
                    maxSlopeBlocks,
                    seafloor || underwater,
                    coastalShore
            );
        }
        return ShorelineSurface.subsurfaceBlock(
                biome,
                spillField,
                localX,
                localZ,
                latitude,
                longitude,
                y,
                floorY,
                elevationMeters,
                seaLevelMeters,
                maxSlopeBlocks,
                underwater,
                coastalShore
        );
    }

    @Override
    public int getBaseHeight(int x, int z, Heightmap.Types type, LevelHeightAccessor level, RandomState randomState)
    {
        double latitude = EarthProjection.blockZToLatitude(z);
        double longitude = EarthProjection.blockXToLongitude(x);
        double elevationMeters = ElevationSamplerHolder.get().sampleElevationMeters(latitude, longitude);
        return TerrainElevationMapper.rawSampleToBlockY(latitude, longitude, elevationMeters);
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
