package com.torr.terracraft.geo;

import com.torr.terracraft.config.TerracraftConfig;
import com.torr.terracraft.geo.hydro.GreatLakesPlateauWater;
import com.torr.terracraft.geo.hydro.HydroLakeSamplerHolder;
import com.torr.terracraft.geo.hydro.RegionalWaterSamplerHolder;
import com.torr.terracraft.world.gen.LakeDepthMapper;
import com.torr.terracraft.world.gen.WaterColumnPlan;
import com.torr.terracraft.world.gen.WaterColumnPlanner;

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

    /** Exposed for downhill surface spill BFS. */
    public static final int[][] NEIGHBOR_OFFSETS_FOR_SPILL = NEIGHBOR_OFFSETS;

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
        boolean[][] regionalWaterGrid = new boolean[GRID_SIZE][GRID_SIZE];
        boolean[][] plateauLakeGrid = new boolean[GRID_SIZE][GRID_SIZE];

        for (int gridX = 0; gridX < GRID_SIZE; gridX++)
        {
            for (int gridZ = 0; gridZ < GRID_SIZE; gridZ++)
            {
                int blockX = chunkMinX - 1 + gridX;
                int blockZ = chunkMinZ - 1 + gridZ;
                double latitude = EarthProjection.blockZToLatitude(blockZ);
                double longitude = EarthProjection.blockXToLongitude(blockX);
                regionalWaterGrid[gridX][gridZ] = isRegionalWaterCell(
                        latitude,
                        longitude,
                        grid[gridX][gridZ],
                        seaLevelMeters,
                        oceanThreshold,
                        oceanTouch[gridX][gridZ]
                );
                plateauLakeGrid[gridX][gridZ] = GreatLakesPlateauWater.isWaterCell(
                        latitude,
                        longitude,
                        grid[gridX][gridZ]
                );
            }
        }

        int regionalFlatSurfaceY = computeRegionalFlatSurfaceY(regionalWaterGrid, grid);
        int plateauFlatSurfaceY = computePlateauFlatSurfaceY(plateauLakeGrid, grid, surfaceGrid);

        WaterColumnPlan[][] plans = new WaterColumnPlan[GRID_SIZE][GRID_SIZE];
        for (int gridX = 0; gridX < GRID_SIZE; gridX++)
        {
            for (int gridZ = 0; gridZ < GRID_SIZE; gridZ++)
            {
                int blockX = chunkMinX - 1 + gridX;
                int blockZ = chunkMinZ - 1 + gridZ;
                double latitude = EarthProjection.blockZToLatitude(blockZ);
                double longitude = EarthProjection.blockXToLongitude(blockX);
                double spillMeters = spillMetersGrid(grid, gridX, gridZ);
                boolean regionalWater = regionalWaterGrid[gridX][gridZ];
                boolean plateauLake = plateauLakeGrid[gridX][gridZ];
                boolean demLake = isInlandDepressionGrid(
                        surfaceGrid,
                        grid,
                        gridX,
                        gridZ,
                        seaLevelMeters,
                        seaLevelBlockY
                );
                boolean hydroVectorLake = isVectorWaterCell(
                        HydroLakeSamplerHolder.get(),
                        TerracraftConfig.hydroLakeSupplementEnabled.get()
                                || TerracraftConfig.useHydroLakePolygons.get(),
                        latitude,
                        longitude,
                        grid[gridX][gridZ],
                        spillMeters,
                        seaLevelMeters,
                        oceanThreshold,
                        oceanTouch[gridX][gridZ],
                        TerracraftConfig.hydroLakeShoreToleranceMeters.get()
                );
                boolean inlandLake = demLake || regionalWater || hydroVectorLake || plateauLake;
                int lakeSurfaceY = inlandLake
                        ? lakeSurfaceBlockY(
                                regionalWater,
                                regionalFlatSurfaceY,
                                plateauLake,
                                plateauFlatSurfaceY,
                                grid[gridX][gridZ],
                                spillMeters,
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
                        touchesOcean[gridX][gridZ],
                        regionalWater
                );
            }
        }

        return new ChunkElevationField(grid, surfaceGrid, touchesOcean, plans, seaLevelMeters);
    }

    private static boolean isOceanCell(double rawMeters, double seaLevelMeters, double oceanThreshold)
    {
        return rawMeters <= seaLevelMeters + oceanThreshold;
    }

    private static boolean isRegionalWaterCell(
            double latitude,
            double longitude,
            double rawMeters,
            double seaLevelMeters,
            double oceanThreshold,
            boolean oceanCell
    )
    {
        if (!TerracraftConfig.regionalWaterEnabled.get()
                || RegionalWaterSamplerHolder.get() instanceof com.torr.terracraft.geo.hydro.StubHydroLakeSampler
                || oceanCell)
        {
            return false;
        }

        if (rawMeters <= seaLevelMeters + oceanThreshold)
        {
            return false;
        }

        return RegionalWaterSamplerHolder.get().sample(latitude, longitude).isLake();
    }

    private static int computeRegionalFlatSurfaceY(boolean[][] regionalWaterGrid, double[][] meterGrid)
    {
        int minSpillY = Integer.MAX_VALUE;

        for (int gridX = 0; gridX < GRID_SIZE; gridX++)
        {
            for (int gridZ = 0; gridZ < GRID_SIZE; gridZ++)
            {
                if (!regionalWaterGrid[gridX][gridZ])
                {
                    continue;
                }

                double spillMeters = spillMetersGrid(meterGrid, gridX, gridZ);
                int spillY = spillMeters == Double.POSITIVE_INFINITY
                        ? Integer.MAX_VALUE
                        : ElevationScale.metersToBlockY(spillMeters);
                minSpillY = Math.min(minSpillY, spillY);
            }
        }

        return minSpillY == Integer.MAX_VALUE ? Integer.MIN_VALUE : minSpillY;
    }

    private static int computePlateauFlatSurfaceY(
            boolean[][] plateauLakeGrid,
            double[][] meterGrid,
            int[][] surfaceGrid
    )
    {
        double minSpillMeters = Double.POSITIVE_INFINITY;

        for (int gridX = 0; gridX < GRID_SIZE; gridX++)
        {
            for (int gridZ = 0; gridZ < GRID_SIZE; gridZ++)
            {
                if (!plateauLakeGrid[gridX][gridZ])
                {
                    continue;
                }

                double spillMeters = spillMetersGrid(meterGrid, gridX, gridZ);
                if (spillMeters != Double.POSITIVE_INFINITY)
                {
                    minSpillMeters = Math.min(minSpillMeters, spillMeters);
                }
            }
        }

        if (minSpillMeters == Double.POSITIVE_INFINITY)
        {
            return Integer.MIN_VALUE;
        }

        double surfaceMeters = GreatLakesPlateauWater.lakeSurfaceMeters(minSpillMeters);
        int spillBlockY = ElevationScale.metersToBlockY(surfaceMeters);
        return LakeDepthMapper.lakeSpillBlockY(
                surfaceMeters,
                surfaceMeters,
                surfaceGrid[GRID_SIZE / 2][GRID_SIZE / 2],
                spillBlockY
        );
    }

    private static int lakeSurfaceBlockY(
            boolean regionalWater,
            int regionalFlatSurfaceY,
            boolean plateauLake,
            int plateauFlatSurfaceY,
            double centerMeters,
            double spillMeters,
            int mappedFloorY,
            int blockSpillY
    )
    {
        if (regionalWater && regionalFlatSurfaceY != Integer.MIN_VALUE)
        {
            return regionalFlatSurfaceY;
        }

        if (plateauLake && plateauFlatSurfaceY != Integer.MIN_VALUE)
        {
            return plateauFlatSurfaceY;
        }

        return LakeDepthMapper.lakeSpillBlockY(centerMeters, spillMeters, mappedFloorY, blockSpillY);
    }

    private static boolean isVectorWaterCell(
            com.torr.terracraft.geo.hydro.HydroLakeSampler sampler,
            boolean enabled,
            double latitude,
            double longitude,
            double rawMeters,
            double spillMeters,
            double seaLevelMeters,
            double oceanThreshold,
            boolean oceanCell,
            double toleranceMeters
    )
    {
        if (!enabled || sampler instanceof com.torr.terracraft.geo.hydro.StubHydroLakeSampler || oceanCell)
        {
            return false;
        }

        if (rawMeters <= seaLevelMeters + oceanThreshold)
        {
            return false;
        }

        if (!sampler.sample(latitude, longitude).isLake())
        {
            return false;
        }

        if (spillMeters == Double.POSITIVE_INFINITY)
        {
            return true;
        }

        return rawMeters <= spillMeters + toleranceMeters;
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

    /** Highest step up to a neighbor (positive elevation change only). */
    public int maxNeighborRiseBlocks(int localX, int localZ)
    {
        int gridX = localX + 1;
        int gridZ = localZ + 1;
        int centerY = surfaceBlockY[gridX][gridZ];
        int maxRise = 0;

        for (int[] offset : NEIGHBOR_OFFSETS)
        {
            int nx = gridX + offset[0];
            int nz = gridZ + offset[1];
            if (!inGrid(nx, nz))
            {
                continue;
            }
            int neighborY = surfaceBlockY[nx][nz];
            maxRise = Math.max(maxRise, neighborY - centerY);
        }

        return maxRise;
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
