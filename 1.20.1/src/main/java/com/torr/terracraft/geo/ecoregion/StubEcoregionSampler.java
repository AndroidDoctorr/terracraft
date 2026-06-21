package com.torr.terracraft.geo.ecoregion;

enum StubEcoregionSampler implements EcoregionSampler
{
    INSTANCE;

    @Override
    public EcoregionInfo sample(double latitude, double longitude)
    {
        return EcoregionInfo.UNKNOWN;
    }
}
