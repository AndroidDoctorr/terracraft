package com.torr.terracraft.geo.ecoregion;

public final class CachedEcoregionSampler implements EcoregionSampler
{
    private final EcoregionTileCache tileCache;

    public CachedEcoregionSampler(EcoregionTileCache tileCache)
    {
        this.tileCache = tileCache;
    }

    @Override
    public EcoregionInfo sample(double latitude, double longitude)
    {
        return tileCache.sample(latitude, longitude);
    }

    public EcoregionTileCache tileCache()
    {
        return tileCache;
    }
}
