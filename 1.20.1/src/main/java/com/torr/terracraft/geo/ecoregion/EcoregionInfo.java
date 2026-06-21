package com.torr.terracraft.geo.ecoregion;

public record EcoregionInfo(int ecoId, int biomeCode, int g200Biome, String realm, String name)
{
    public static final EcoregionInfo UNKNOWN = new EcoregionInfo(0, 0, 0, "", "unknown");

    public int effectiveBiomeCode()
    {
        if (g200Biome >= 1 && g200Biome <= 14)
        {
            return g200Biome;
        }
        if (biomeCode >= 1 && biomeCode <= 14)
        {
            return biomeCode;
        }
        return 0;
    }
}
