package com.torr.terracraft.geo;

import com.torr.terracraft.config.TerracraftConfig;
import com.torr.terracraft.world.gen.WaterColumnPlan;
import com.torr.terracraft.world.gen.WaterColumnPlanner;
import com.torr.terracraft.world.gen.LakeDepthMapper;

/**
 * 18×18 elevation grid (16×16 chunk plus one-block border on each side) for terrain
 * columns, shoreline slope, and inland lake basins.
 */
public final class ChunkElevationField
{
    private static final int GRID_SIZE = 18;
    private static final int[][] NEIGHBOR_OFFSETS = {
            {-1, 0}, {1, 0}, {0, -1}, {0, 1},
            {-1, -1}, {-1, 1}, {1, -1}, {1, 1}
    };

    private final double[][] meters;
    private final int[][] surfaceBlockY;
    private final boolean[][] touchesOcean;
    private final WaterColumnPlan[][] waterPlans;
    private final double seaLevelMeters;

    private ChunkElevationField(
            double[][] meters,
            int[][] surfaceBlockY,
            boolean[][] touchesOcean,
            WaterColumnPlan[][] waterPlans,
            double seaLevelMeters
    )
    {
        this.meters = meters;
        this.surfaceBlockY = surfaceBlockY;
        this.touchesOcean = touchesOcean;
        this.waterPlans = waterPlans;
        this.seaLevelMeters = seaLevelMeters;
    }

    public static ChunkElevationField sample(int chunkMinX, int chunkMinZ)
    {
        double[][] grid = new double[GRID_SIZE][GRID_SIZE];
        int[][] surfaceGrid = new int[GRID_SIZE][GRID_SIZE];
        ElevationSampler sampler = ElevationSamplerHolder.get();
        double seaLevelMeters = TerracraftConfig.seaLevelMeters.get();
        double oceanThreshold = TerracraftConfig.oceanSurfaceThresholdMeters.get();

        for (int gridX = 0; gridX < GRID_SIZE; gridX++)
        {
            for (int gridZ = 0; gridZ < GRID_SIZE; gridZ++)
            {
                int blockX = chunkMinX - 1 + gridX;
                int blockZ = chunkMinZ - 1 + gridZ;
                double latitude = EarthProjection.blockZToLatitude(blockZ);
                double longitude = EarthProjection.blockXToLongitude(blockX);
                double rawMeters = sampler.sampleElevationMeters(latitude, longitude);
                grid[gridX][gridZ] = rawMeters;
                surfaceGrid[gridX][gridZ] = TerrainElevationMapper.rawSampleToBlockY(latitude, longitude, rawMeters);
            }
        }

        boolean[][] oceanTouch = new boolean[GRID_SIZE][GRID_SIZE];
        for (int gridX = 0; gridX < GRID_SIZE; gridX++)
        {
            for (int gridZ = 0; gridZ < GRID_SIZE; gridZ++)
            {
                oceanTouch[gridX][gridZ] = isOceanCell(grid[gridX][gridZ], seaLevelMeters, oceanThreshold);
            }
        }

        boolean[][] touchesOcean = new boolean[GRID_SIZE][GRID_SIZE];
        for (int gridX = 0; gridX < GRID_SIZE; gridX++)
        {
            for (int gridZ = 0; gridZ < GRID_SIZE; gridZ++)
            {
                touchesOcean[gridX][gridZ] = touchesOceanGrid(oceanTouch, gridX, gridZ);
            }
        }

        int seaLevelBlockY = TerracraftConfig.seaLevelBlockY.get();
        WaterColumnPlan[][] plans = new WaterColumnPlan[GRID_SIZE][GRID_SIZE];
        for (int gridX = 0; gridX < GRID_SIZE; gridX++)
        {
            for (int gridZ = 0; gridZ < GRID_SIZE; gridZ++)
            {
                boolean inlandLake = isInlandDepressionGrid(
                        surfaceGrid,
                        grid,
                        gridX,
                        gridZ,
                        seaLevelMeters,
                        seaLevelBlockY
                );
                int lakeSurfaceY = inlandLake
                        ? LakeDepthMapper.lakeSpillBlockY(
                                grid[gridX][gridZ],
                                spillMetersGrid(grid, gridX, gridZ),
                                surfaceGrid[gridX][gridZ],
                                spillSurfaceBlockYGrid(surfaceGrid, gridX, gridZ)
                        )
                        : Integer.MIN_VALUE;
                plans[gridX][gridZ] = WaterColumnPlanner.plan(
                        grid[gridX][gridZ],
                        surfaceGrid[gridX][gridZ],
                        seaLevelBlockY,
                        seaLevelMeters,
                        inlandLake,
                        lakeSurfaceY,
                        touchesOcean[gridX][gridZ]
                );
            }
        }

        return new ChunkElevationField(grid, surfaceGrid, touchesOcean, plans, seaLevelMeters);
    }

    private static boolean isOceanCell(double rawMeters, double seaLevelMeters, double oceanThreshold)
    {
        return rawMeters <= seaLevelMeters + oceanThreshold;
    }

    private static boolean touchesOceanGrid(boolean[][] oceanTouch, int gridX, int gridZ)
    {
        if (oceanTouch[gridX][gridZ])
        {
            return true;
        }

        for (int[] offset : NEIGHBOR_OFFSETS)
        {
            int nx = gridX + offset[0];
            int nz = gridZ + offset[1];
            if (inGrid(nx, nz) && oceanTouch[nx][nz])
            {
                return true;
            }
        }

        return false;
    }

    private static boolean inGrid(int gridX, int gridZ)
    {
        return gridX >= 0 && gridX < GRID_SIZE && gridZ >= 0 && gridZ < GRID_SIZE;
    }

    public double centerMeters(int localX, int localZ)
    {
        return meters[localX + 1][localZ + 1];
    }

    public int surfaceBlockY(int localX, int localZ)
    {
        return surfaceBlockY[localX + 1][localZ + 1];
    }

    public WaterColumnPlan waterPlan(int localX, int localZ)
    {
        return waterPlans[localX + 1][localZ + 1];
    }

    public boolean adjacentToWater(int localX, int localZ)
    {
        int gridX = localX + 1;
        int gridZ = localZ + 1;

        if (waterPlans[gridX][gridZ].hasWater())
        {
            return true;
        }

        for (int[] offset : NEIGHBOR_OFFSETS)
        {
            int nx = gridX + offset[0];
            int nz = gridZ + offset[1];
            if (inGrid(nx, nz) && waterPlans[nx][nz].hasWater())
            {
                return true;
            }
        }

        return false;
    }

    public int maxNeighborSlopeBlocks(int localX, int localZ)
    {
        int gridX = localX + 1;
        int gridZ = localZ + 1;
        int centerY = surfaceBlockY[gridX][gridZ];
        int maxSlope = 0;

        for (int[] offset : NEIGHBOR_OFFSETS)
        {
            int nx = gridX + offset[0];
            int nz = gridZ + offset[1];
            if (!inGrid(nx, nz))
            {
                continue;
            }
            int neighborY = surfaceBlockY[nx][nz];
            maxSlope = Math.max(maxSlope, Math.abs(centerY - neighborY));
        }

        return maxSlope;
    }

    public double spillMeters(int localX, int localZ)
    {
        return spillMetersGrid(meters, localX + 1, localZ + 1);
    }

    public int spillSurfaceBlockY(int localX, int localZ)
    {
        return spillSurfaceBlockYGrid(surfaceBlockY, localX + 1, localZ + 1);
    }

    private static int spillSurfaceBlockYGrid(int[][] surfaceGrid, int gridX, int gridZ)
    {
        int spill = Integer.MAX_VALUE;

        for (int[] offset : NEIGHBOR_OFFSETS)
        {
            int nx = gridX + offset[0];
            int nz = gridZ + offset[1];
            if (inGrid(nx, nz))
            {
                spill = Math.min(spill, surfaceGrid[nx][nz]);
            }
        }

        return spill;
    }

    public boolean isInlandDepression(int localX, int localZ)
    {
        return isInlandDepressionGrid(
                surfaceBlockY,
                meters,
                localX + 1,
                localZ + 1,
                seaLevelMeters,
                TerracraftConfig.seaLevelBlockY.get()
        );
    }

    private static boolean isInlandDepressionGrid(
            int[][] surfaceGrid,
            double[][] meterGrid,
            int gridX,
            int gridZ,
            double seaLevelMeters,
            int seaBlockY
    )
    {
        int centerY = surfaceGrid[gridX][gridZ];
        int spillY = spillSurfaceBlockYGrid(surfaceGrid, gridX, gridZ);
        int minDepthBlocks = TerracraftConfig.depressionMinDepthBlocks.get();

        if (spillY != Integer.MAX_VALUE && spillY > seaBlockY && centerY + minDepthBlocks <= spillY)
        {
            return true;
        }

        double center = meterGrid[gridX][gridZ];
        double spill = spillMetersGrid(meterGrid, gridX, gridZ);
        if (spill == Double.POSITIVE_INFINITY)
        {
            return false;
        }

        double minDepthMeters = TerracraftConfig.depressionMinDepthMeters.get();
        return spill > seaLevelMeters + 0.05D
                && center + minDepthMeters < spill - 0.05D;
    }

    private static double spillMetersGrid(double[][] meterGrid, int gridX, int gridZ)
    {
        double spill = Double.POSITIVE_INFINITY;

        for (int[] offset : NEIGHBOR_OFFSETS)
        {
            int nx = gridX + offset[0];
            int nz = gridZ + offset[1];
            if (inGrid(nx, nz))
            {
                spill = Math.min(spill, meterGrid[nx][nz]);
            }
        }

        return spill;
    }

    public int lakeSurfaceBlockY(int localX, int localZ)
    {
        if (!isInlandDepression(localX, localZ))
        {
            return Integer.MIN_VALUE;
        }

        return spillSurfaceBlockY(localX, localZ);
    }
}
