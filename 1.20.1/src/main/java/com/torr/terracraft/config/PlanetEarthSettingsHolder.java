package com.torr.terracraft.config;

/**
 * Per-world Planet Earth settings, set from the world preset biome source codec.
 * Falls back to {@link TerracraftConfig} when unset.
 */
public final class PlanetEarthSettingsHolder
{
    private static Double horizontalScale;
    private static ElevationMappingMode elevationMapping;
    private static FloraPlacementMode floraPlacement;
    private static Integer ecoregionBorderBlendBlocks;

    private PlanetEarthSettingsHolder()
    {
    }

    public static void apply(
            double horizontalScaleBlocksPerDegree,
            ElevationMappingMode elevationMappingMode,
            FloraPlacementMode floraPlacementMode,
            int borderBlendBlocks
    )
    {
        horizontalScale = horizontalScaleBlocksPerDegree;
        elevationMapping = elevationMappingMode;
        floraPlacement = floraPlacementMode;
        ecoregionBorderBlendBlocks = borderBlendBlocks;
        FloraPlacementHolder.set(floraPlacementMode);
    }

    public static void clear()
    {
        horizontalScale = null;
        elevationMapping = null;
        floraPlacement = null;
        ecoregionBorderBlendBlocks = null;
    }

    public static double horizontalScale()
    {
        return horizontalScale != null ? horizontalScale : TerracraftConfig.horizontalScale.get();
    }

    public static ElevationMappingMode elevationMapping()
    {
        return elevationMapping != null ? elevationMapping : TerracraftConfig.elevationMappingMode();
    }

    public static FloraPlacementMode floraPlacement()
    {
        return floraPlacement != null ? floraPlacement : FloraPlacementMode.defaultMode();
    }

    public static int ecoregionBorderBlendBlocks()
    {
        if (ecoregionBorderBlendBlocks != null)
        {
            return ecoregionBorderBlendBlocks;
        }
        return TerracraftConfig.ecoregionBorderBlendBlocks.get();
    }
}
