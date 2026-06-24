package com.torr.terracraft.geo;

import com.torr.terracraft.config.TerracraftConfig;

/**
 * 18×18 elevation grid (16×16 chunk plus one-block border on each side) for terrain
 * columns and simple depression / lake detection.
 */
public final class ChunkElevationField
{
    private static final int GRID_SIZE = 18;

    private final int chunkMinX;
    private final int chunkMinZ;
    private final double[][] meters;
    private final double seaLevelMeters;

    private ChunkElevationField(int chunkMinX, int chunkMinZ, double[][] meters, double seaLevelMeters)
    {
        this.chunkMinX = chunkMinX;
        this.chunkMinZ = chunkMinZ;
        this.meters = meters;
        this.seaLevelMeters = seaLevelMeters;
    }

    public static ChunkElevationField sample(int chunkMinX, int chunkMinZ)
    {
        double[][] grid = new double[GRID_SIZE][GRID_SIZE];
        ElevationSampler sampler = ElevationSamplerHolder.get();
        double seaLevelMeters = TerracraftConfig.seaLevelMeters.get();

        for (int gridX = 0; gridX < GRID_SIZE; gridX++)
        {
            for (int gridZ = 0; gridZ < GRID_SIZE; gridZ++)
            {
                int blockX = chunkMinX - 1 + gridX;
                int blockZ = chunkMinZ - 1 + gridZ;
                double latitude = EarthProjection.blockZToLatitude(blockZ);
                double longitude = EarthProjection.blockXToLongitude(blockX);
                grid[gridX][gridZ] = sampler.sampleElevationMeters(latitude, longitude);
            }
        }

        return new ChunkElevationField(chunkMinX, chunkMinZ, grid, seaLevelMeters);
    }

    public double centerMeters(int localX, int localZ)
    {
        return meters[localX + 1][localZ + 1];
    }

    public int surfaceBlockY(int localX, int localZ)
    {
        double rawMeters = centerMeters(localX, localZ);
        double latitude = EarthProjection.blockZToLatitude(chunkMinZ + localZ);
        double longitude = EarthProjection.blockXToLongitude(chunkMinX + localX);
        return TerrainElevationMapper.rawSampleToBlockY(latitude, longitude, rawMeters);
    }

    public double spillMeters(int localX, int localZ)
    {
        int gridX = localX + 1;
        int gridZ = localZ + 1;
        double spill = Double.POSITIVE_INFINITY;
        spill = Math.min(spill, meters[gridX - 1][gridZ]);
        spill = Math.min(spill, meters[gridX + 1][gridZ]);
        spill = Math.min(spill, meters[gridX][gridZ - 1]);
        spill = Math.min(spill, meters[gridX][gridZ + 1]);
        return spill;
    }

    public boolean isInlandDepression(int localX, int localZ)
    {
        double center = centerMeters(localX, localZ);
        double spill = spillMeters(localX, localZ);
        double minDepth = TerracraftConfig.depressionMinDepthMeters.get();
        return spill > seaLevelMeters + 0.05D
                && center + minDepth < spill - 0.05D;
    }

    public int lakeSurfaceBlockY(int localX, int localZ)
    {
        if (!isInlandDepression(localX, localZ))
        {
            return Integer.MIN_VALUE;
        }

        double spillRawMeters = spillMeters(localX, localZ);
        double latitude = EarthProjection.blockZToLatitude(chunkMinZ + localZ);
        double longitude = EarthProjection.blockXToLongitude(chunkMinX + localX);
        return TerrainElevationMapper.rawSampleToBlockY(latitude, longitude, spillRawMeters);
    }
}
