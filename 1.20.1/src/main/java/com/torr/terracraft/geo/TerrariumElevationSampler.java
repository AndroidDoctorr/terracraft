package com.torr.terracraft.geo;

public final class TerrariumElevationSampler implements ElevationSampler
{
    private final DemTileCache cache;
    private final int zoom;

    public TerrariumElevationSampler(DemTileCache cache, int zoom)
    {
        this.cache = cache;
        this.zoom = zoom;
    }

    @Override
    public double sampleElevationMeters(double latitude, double longitude)
    {
        return cache.sample(zoom, latitude, longitude);
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
