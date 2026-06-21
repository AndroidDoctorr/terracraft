package com.torr.terracraft.geo;

public final class ElevationSamplerHolder
{
    private static ElevationSampler sampler = new StubElevationSampler();

    private ElevationSamplerHolder()
    {
    }

    public static ElevationSampler get()
    {
        return sampler;
    }

    public static void set(ElevationSampler replacement)
    {
        sampler = replacement;
    }
}
