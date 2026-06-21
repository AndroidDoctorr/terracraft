package com.torr.terracraft.geo.ecoregion;

public final class EcoregionSamplerHolder
{
    private static EcoregionSampler sampler = StubEcoregionSampler.INSTANCE;

    private EcoregionSamplerHolder()
    {
    }

    public static EcoregionSampler get()
    {
        return sampler;
    }

    public static void set(EcoregionSampler replacement)
    {
        sampler = replacement;
    }

    public static void resetToStub()
    {
        sampler = StubEcoregionSampler.INSTANCE;
    }
}
