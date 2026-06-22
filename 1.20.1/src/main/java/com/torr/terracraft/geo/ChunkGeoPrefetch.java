package com.torr.terracraft.geo;

import com.torr.terracraft.config.PlanetEarthSettingsHolder;
import com.torr.terracraft.geo.ecoregion.CachedEcoregionSampler;
import com.torr.terracraft.geo.ecoregion.EcoregionSampler;
import com.torr.terracraft.geo.ecoregion.EcoregionSamplerHolder;

/**
 * Loads DEM and ecoregion tiles for a chunk footprint before per-column sampling so
 * first-visit generation waits once per tile instead of once per column.
 */
public final class ChunkGeoPrefetch
{
    private ChunkGeoPrefetch()
    {
    }

    public static void prefetch(int chunkMinX, int chunkMinZ)
    {
        int chunkMaxX = chunkMinX + 15;
        int chunkMaxZ = chunkMinZ + 15;
        int margin = PlanetEarthSettingsHolder.ecoregionBorderBlendBlocks() + 16;

        double northLat = EarthProjection.blockZToLatitude(chunkMinZ - margin);
        double southLat = EarthProjection.blockZToLatitude(chunkMaxZ + margin);
        double westLon = EarthProjection.blockXToLongitude(chunkMinX - margin);
        double eastLon = EarthProjection.blockXToLongitude(chunkMaxX + margin);

        double minLat = Math.min(northLat, southLat);
        double maxLat = Math.max(northLat, southLat);
        double minLon = Math.min(westLon, eastLon);
        double maxLon = Math.max(westLon, eastLon);

        prefetchElevation(minLat, maxLat, minLon, maxLon);
        prefetchEcoregions(minLat, maxLat, minLon, maxLon);
    }

    private static void prefetchElevation(double minLat, double maxLat, double minLon, double maxLon)
    {
        ElevationSampler sampler = ElevationSamplerHolder.get();
        if (sampler instanceof TerrariumElevationSampler terrarium)
        {
            terrarium.cache().prefetchArea(terrarium.zoom(), minLat, maxLat, minLon, maxLon);
        }
    }

    private static void prefetchEcoregions(double minLat, double maxLat, double minLon, double maxLon)
    {
        EcoregionSampler sampler = EcoregionSamplerHolder.get();
        if (sampler instanceof CachedEcoregionSampler cached)
        {
            cached.tileCache().prefetchArea(minLat, maxLat, minLon, maxLon);
        }
    }
}
