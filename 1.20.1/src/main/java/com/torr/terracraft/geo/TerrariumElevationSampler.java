package com.torr.terracraft.geo;

import com.torr.terracraft.config.TerracraftConfig;

public final class TerrariumElevationSampler implements ElevationSampler
{
    private final DemTileCache cache;
    private final int zoom;

    public TerrariumElevationSampler(DemTileCache cache, int zoom)
    {
        this.cache = cache;
        this.zoom = zoom;
    }

    public static int baselineZoomFor(int detailZoom)
    {
        return Math.max(8, detailZoom - TerracraftConfig.demBaselineZoomOffset.get());
    }

    @Override
    public double sampleElevationMeters(double latitude, double longitude)
    {
        return cache.sample(zoom, latitude, longitude);
    }

    public double sampleBaselineMeters(double latitude, double longitude)
    {
        return cache.sample(baselineZoom(), latitude, longitude);
    }

    public int baselineZoom()
    {
        return baselineZoomFor(zoom);
    }

    public DemTileCache cache()
    {
        return cache;
    }

    public int zoom()
    {
        return zoom;
    }
}
