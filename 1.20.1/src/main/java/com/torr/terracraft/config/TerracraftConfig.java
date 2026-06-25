package com.torr.terracraft.config;

import net.minecraftforge.common.ForgeConfigSpec;

public final class TerracraftConfig
{
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    public static ElevationMappingMode elevationMappingMode()
    {
        return ElevationMappingMode.fromConfig(elevationMapping.get());
    }

    public static final ForgeConfigSpec.DoubleValue horizontalScale = BUILDER
            .comment("Blocks per degree of latitude/longitude. Terra 1:1 used 100000 (about one meter per block at the equator).")
            .defineInRange("horizontalScale", 100_000.0D, 1.0D, 1_000_000.0D);

    public static final ForgeConfigSpec.DoubleValue seaLevelMeters = BUILDER
            .comment("Real-world elevation in meters treated as sea level.")
            .defineInRange("seaLevelMeters", 0.0D, -500.0D, 500.0D);

    public static final ForgeConfigSpec.IntValue seaLevelBlockY = BUILDER
            .comment("Minecraft Y level used for sea level when vertical compression is enabled.")
            .defineInRange("seaLevelBlockY", 63, -64, 320);

    public static final ForgeConfigSpec.IntValue waterSurfaceBlockOffset = BUILDER
            .comment("Water surfaces (ocean, estuary, lakes) fill to seaLevelBlockY plus this offset. Default 1 raises water one block above the terrain reference sea level.")
            .defineInRange("waterSurfaceBlockOffset", 1, 0, 16);

    public static int waterSurfaceBlockY()
    {
        return Math.min(
                seaLevelBlockY.get() + waterSurfaceBlockOffset.get(),
                maxWorldY.get()
        );
    }

    public static final ForgeConfigSpec.ConfigValue<String> elevationMapping = BUILDER
            .comment("Elevation mapping mode: linear (constant scale) or coastal_log (steep near sea level, compresses mountains).")
            .define("elevationMapping", "coastal_log");

    public static final ForgeConfigSpec.DoubleValue verticalScale = BUILDER
            .comment("Blocks per real-world meter when elevationMapping=linear. 0.05 means one block per 20 m (very flat).")
            .defineInRange("verticalScale", 0.05D, 0.001D, 2.0D);

    public static final ForgeConfigSpec.DoubleValue coastalVerticalScale = BUILDER
            .comment("Blocks per meter at sea level when elevationMapping=coastal_log. Higher = more recognizable hills/coasts.")
            .defineInRange("coastalVerticalScale", 0.25D, 0.01D, 2.0D);

    public static final ForgeConfigSpec.DoubleValue elevationCompressionMeters = BUILDER
            .comment("Knee height (meters) for coastal_log compression. Larger = taller terrain before mountains flatten.")
            .defineInRange("elevationCompressionMeters", 250.0D, 50.0D, 2000.0D);

    public static final ForgeConfigSpec.IntValue minWorldY = BUILDER
            .comment("Lowest Y the generator will place blocks. Match dimension_type min_y if you raise world height.")
            .defineInRange("minWorldY", -64, -2032, 2031);

    public static final ForgeConfigSpec.IntValue maxWorldY = BUILDER
            .comment("Highest Y the generator will place blocks. Match dimension_type min_y + height - 1 if extended.")
            .defineInRange("maxWorldY", 320, -2032, 2031);

    public static final ForgeConfigSpec.BooleanValue useStubElevation = BUILDER
            .comment("DEM settings")
            .comment("Use procedural fake terrain instead of downloading real DEM tiles (useful offline or for debugging).")
            .define("useStubElevation", false);

    public static final ForgeConfigSpec.IntValue demZoom = BUILDER
            .comment("Terrarium tile zoom level (8-15). Higher is sharper but more downloads. 12 is ~40 m per pixel at the equator.")
            .defineInRange("demZoom", 12, 8, 15);

    public static final ForgeConfigSpec.ConfigValue<String> demTileUrlTemplate = BUILDER
            .comment("URL template for Terrarium PNG tiles. Must contain %d placeholders for zoom, tile X, and tile Y.")
            .define("demTileUrlTemplate", "https://s3.amazonaws.com/elevation-tiles-prod/terrarium/%d/%d/%d.png");

    public static final ForgeConfigSpec.IntValue demDownloadThreads = BUILDER
            .comment("Maximum concurrent DEM tile downloads.")
            .defineInRange("demDownloadThreads", 4, 1, 16);

    public static final ForgeConfigSpec.BooleanValue demBilinearSampling = BUILDER
            .comment("Smooth terrain by bilinear-interpolating DEM pixels instead of nearest-neighbor column sampling.")
            .define("demBilinearSampling", true);

    public static final ForgeConfigSpec.BooleanValue elevationHighPassEnabled = BUILDER
            .comment("Terrain height mapping")
            .comment("Split DEM into regional baseline (coarser zoom, coastal_log) + local detail (near-real vertical scale).")
            .define("elevationHighPassEnabled", true);

    public static final ForgeConfigSpec.IntValue demBaselineZoomOffset = BUILDER
            .comment("Baseline DEM is sampled at demZoom minus this offset (min zoom 8). 2 ≈ 4× coarser regional trend.")
            .defineInRange("demBaselineZoomOffset", 2, 1, 4);

    public static final ForgeConfigSpec.DoubleValue elevationDetailVerticalScale = BUILDER
            .comment("Blocks per meter applied to local elevation detail (raw minus baseline). Default matches coastalVerticalScale.")
            .defineInRange("elevationDetailVerticalScale", 0.25D, 0.0D, 2.0D);

    public static final ForgeConfigSpec.DoubleValue elevationDetailMaxAbsMeters = BUILDER
            .comment("Clamp |raw - baseline| before detail scaling (0 = disabled). Reduces spikes from noisy DEM.")
            .defineInRange("elevationDetailMaxAbsMeters", 0.0D, 0.0D, 2000.0D);

    public static final ForgeConfigSpec.DoubleValue depressionMinDepthMeters = BUILDER
            .comment("Water and shoreline")
            .comment("Minimum real-world depth (meters) below the local spill elevation to classify a cell as an inland lake basin.")
            .defineInRange("depressionMinDepthMeters", 0.5D, 0.1D, 50.0D);

    public static final ForgeConfigSpec.IntValue depressionMinDepthBlocks = BUILDER
            .comment("Minimum terrain depth (blocks) below the 8-neighbor spill height to classify an inland lake basin.")
            .defineInRange("depressionMinDepthBlocks", 2, 1, 32);

    public static final ForgeConfigSpec.BooleanValue shorelineBandsEnabled = BUILDER
            .comment("Place sand/gravel beaches and stone cliffs near sea level based on local slope.")
            .define("shorelineBandsEnabled", true);

    public static final ForgeConfigSpec.DoubleValue shorelineFeatherMeters = BUILDER
            .comment("Real-world elevation band above sea level that receives shoreline surface blocks.")
            .defineInRange("shorelineFeatherMeters", 12.0D, 0.0D, 100.0D);

    public static final ForgeConfigSpec.IntValue shorelineFeatherBlocks = BUILDER
            .comment("Minecraft Y band around seaLevelBlockY that receives shoreline surface blocks.")
            .defineInRange("shorelineFeatherBlocks", 4, 0, 32);

    public static final ForgeConfigSpec.IntValue shorelineCliffSlopeBlocks = BUILDER
            .comment("Max neighbor height difference (blocks) below which a near-shore cell uses sand instead of stone.")
            .defineInRange("shorelineCliffSlopeBlocks", 4, 1, 32);

    public static final ForgeConfigSpec.BooleanValue coastalInundationEnabled = BUILDER
            .comment("Fill terrain below seaLevelBlockY with water when elevation is within shorelineFeatherMeters of sea level (estuaries/bays).")
            .define("coastalInundationEnabled", true);

    public static final ForgeConfigSpec.DoubleValue oceanSurfaceThresholdMeters = BUILDER
            .comment("Raw DEM elevation at or below sea level plus this value is treated as open ocean (water to seaLevelBlockY).")
            .defineInRange("oceanSurfaceThresholdMeters", 0.5D, 0.0D, 10.0D);

    public static final ForgeConfigSpec.DoubleValue estuaryMaxElevationMeters = BUILDER
            .comment("Low land within this many meters above sea level that touches ocean gets filled to seaLevelBlockY (harbors, tidal flats).")
            .defineInRange("estuaryMaxElevationMeters", 25.0D, 0.0D, 100.0D);

    public static final ForgeConfigSpec.BooleanValue coastalTerrainClampEnabled = BUILDER
            .comment("Cap high-pass mapped height to direct coastal_log mapping for low elevations so cities and bays stay near sea level.")
            .define("coastalTerrainClampEnabled", true);

    public static final ForgeConfigSpec.DoubleValue coastalTerrainClampBelowMeters = BUILDER
            .comment("Apply coastal terrain clamp when raw elevation is at or below sea level plus this value.")
            .defineInRange("coastalTerrainClampBelowMeters", 75.0D, 0.0D, 500.0D);

    public static final ForgeConfigSpec.IntValue coastalTerrainClampBlockMargin = BUILDER
            .comment("Extra blocks allowed above direct coastal_log mapping when clamping low elevations.")
            .defineInRange("coastalTerrainClampBlockMargin", 3, 0, 32);

    public static final ForgeConfigSpec.BooleanValue biomeVariantsEnabled = BUILDER
            .comment("Biome variation")
            .comment("Enable patch-scale biome variant profiles (tree density, clearings, wetland pockets).")
            .define("biomeVariantsEnabled", true);

    public static final ForgeConfigSpec.IntValue variationPatchScaleBlocks = BUILDER
            .comment("Size of variant patches in blocks (power of two recommended, default 128).")
            .defineInRange("variationPatchScaleBlocks", 128, 32, 512);

    public static final ForgeConfigSpec.DoubleValue variantElevationFalloffMeters = BUILDER
            .comment("Real elevation span over which tree density falls from lowland boost to upland reduction.")
            .defineInRange("variantElevationFalloffMeters", 700.0D, 50.0D, 3000.0D);

    public static final ForgeConfigSpec.DoubleValue variantLowElevationTreeBoost = BUILDER
            .comment("Tree density multiplier in valleys and low coastal land.")
            .defineInRange("variantLowElevationTreeBoost", 1.15D, 0.1D, 3.0D);

    public static final ForgeConfigSpec.DoubleValue variantHighElevationTreeScale = BUILDER
            .comment("Tree density multiplier on high slopes within the falloff band.")
            .defineInRange("variantHighElevationTreeScale", 0.35D, 0.0D, 2.0D);

    public static final ForgeConfigSpec.BooleanValue useEcoregionBiomes = BUILDER
            .comment("Ecoregion settings")
            .comment("Use WWF Terrestrial Ecoregions (TEOW) polygons for land biomes instead of the climate heuristic.")
            .define("useEcoregionBiomes", true);

    public static final ForgeConfigSpec.BooleanValue useClimateFallback = BUILDER
            .comment("When ecoregion data is missing for a coordinate, fall back to the latitude/rainfall climate classifier.")
            .define("useClimateFallback", true);

    public static final ForgeConfigSpec.BooleanValue autoDownloadEcoregionData = BUILDER
            .comment("Download WWF ecoregion GeoJSON from ArcGIS on first run if the data file is missing.")
            .define("autoDownloadEcoregionData", true);

    public static final ForgeConfigSpec.ConfigValue<String> ecoregionDataFile = BUILDER
            .comment("Path to wwf_ecoregions.geojson relative to .minecraft/terracraft/data/, or an absolute path.")
            .define("ecoregionDataFile", "wwf_ecoregions.geojson");

    public static final ForgeConfigSpec.ConfigValue<String> ecoregionQueryUrlTemplate = BUILDER
            .comment("ArcGIS FeatureServer query URL template. Must contain one %d placeholder for resultOffset pagination.")
            .define("ecoregionQueryUrlTemplate",
                    "https://services8.arcgis.com/7L75T5PDROpCazRR/arcgis/rest/services/WWF_ecoregions/FeatureServer/0/query"
                            + "?where=1%3D1&outFields=ECO_ID,ECO_NAME,BIOME,G200_BIOME,REALM,eco_code&outSR=4326&f=geojson"
                            + "&resultRecordCount=2000&resultOffset=%d");

    public static final ForgeConfigSpec.IntValue ecoregionZoom = BUILDER
            .comment("Web Mercator tile zoom for cached ecoregion raster tiles. Lower is faster to build; 5 is ~5 km per pixel at the equator.")
            .defineInRange("ecoregionZoom", 5, 3, 10);

    public static final ForgeConfigSpec.IntValue ecoregionBorderBlendBlocks = BUILDER
            .comment("Width in blocks of ecoregion border buffer where neighbor vegetation can spill over (0 disables).")
            .defineInRange("ecoregionBorderBlendBlocks", 96, 0, 512);

    public static final ForgeConfigSpec.BooleanValue ecoregionBorderBlendEnabled = BUILDER
            .comment("Ecoregion border blending")
            .comment("Enable distance-weighted neighbor biome spillover at ecoregion edges.")
            .define("ecoregionBorderBlendEnabled", true);

    public static final ForgeConfigSpec.BooleanValue ecoregionBorderTransitionEnabled = BUILDER
            .comment("Use dedicated transition clones (montane_meadow, chaparral, etc.) in border bands.")
            .define("ecoregionBorderTransitionEnabled", true);

    public static final ForgeConfigSpec.DoubleValue ecoregionBorderSpillWeight = BUILDER
            .comment("Multiplier for border spill probability (higher = wider soft edges).")
            .defineInRange("ecoregionBorderSpillWeight", 1.0D, 0.1D, 2.0D);

    public static final ForgeConfigSpec.BooleanValue rainShadowEnabled = BUILDER
            .comment("Nudge leeward basins toward semi-arid/chaparral clones when upwind terrain is high.")
            .define("rainShadowEnabled", true);

    public static final ForgeConfigSpec.DoubleValue rainShadowSampleDegrees = BUILDER
            .comment("Longitude offset (degrees) for rain-shadow upwind/downwind elevation samples.")
            .defineInRange("rainShadowSampleDegrees", 0.08D, 0.01D, 1.0D);

    public static final ForgeConfigSpec.DoubleValue rainShadowMinUpwindMeters = BUILDER
            .comment("Minimum upwind elevation excess (meters) to classify rain shadow.")
            .defineInRange("rainShadowMinUpwindMeters", 350.0D, 50.0D, 2000.0D);

    public static final ForgeConfigSpec.DoubleValue rainShadowMaxElevationMeters = BUILDER
            .comment("Do not apply rain-shadow nudges above this elevation (meters).")
            .defineInRange("rainShadowMaxElevationMeters", 2200.0D, 200.0D, 5000.0D);

    public static final ForgeConfigSpec.BooleanValue lakeMeterSurfaceEnabled = BUILDER
            .comment("Lake depth")
            .comment("Derive inland lake spill height from real-world meter spill, not block-space spill alone.")
            .define("lakeMeterSurfaceEnabled", true);

    public static final ForgeConfigSpec.BooleanValue lakeShallowPreserveEnabled = BUILDER
            .comment("For shallow basins, cap water surface using DEM depth so flat block spill does not over-fill.")
            .define("lakeShallowPreserveEnabled", true);

    public static final ForgeConfigSpec.IntValue lakeMinDepthBlocksFromDem = BUILDER
            .comment("Minimum water depth (blocks) for inland lakes from DEM spill minus floor.")
            .defineInRange("lakeMinDepthBlocksFromDem", 1, 1, 16);

    public static final ForgeConfigSpec.IntValue lakeMaxDepthBlocks = BUILDER
            .comment("Maximum water depth (blocks) for inland lakes (0 = no cap).")
            .defineInRange("lakeMaxDepthBlocks", 24, 0, 64);

    public static final ForgeConfigSpec.BooleanValue riparianEnabled = BUILDER
            .comment("Riparian heuristics (Sprint 5; augmented by hydro vectors in Sprint 6)")
            .comment("Detect drainage corridors from local DEM relief for wetland/gallery vegetation.")
            .define("riparianEnabled", true);

    public static final ForgeConfigSpec.DoubleValue riparianSampleDegrees = BUILDER
            .comment("Cardinal DEM sample offset (degrees) for riparian relief detection.")
            .defineInRange("riparianSampleDegrees", 0.004D, 0.0005D, 0.05D);

    public static final ForgeConfigSpec.DoubleValue riparianMaxElevationMeters = BUILDER
            .comment("Ignore riparian heuristics above sea level plus this elevation (meters).")
            .defineInRange("riparianMaxElevationMeters", 800.0D, 50.0D, 4000.0D);

    public static final ForgeConfigSpec.DoubleValue riparianMinReliefMeters = BUILDER
            .comment("Minimum average neighbor excess elevation (meters) to classify a drainage corridor.")
            .defineInRange("riparianMinReliefMeters", 0.8D, 0.1D, 20.0D);

    public static final ForgeConfigSpec.DoubleValue riparianMaxReliefMeters = BUILDER
            .comment("Relief (meters) at which riparian strength reaches maximum.")
            .defineInRange("riparianMaxReliefMeters", 35.0D, 5.0D, 200.0D);

    public static final ForgeConfigSpec.DoubleValue riparianCorridorStrengthScale = BUILDER
            .comment("Strength multiplier when relief is not strongly directional (non-corridor cells).")
            .defineInRange("riparianCorridorStrengthScale", 0.65D, 0.1D, 1.0D);

    public static final ForgeConfigSpec.BooleanValue riparianBiomeNudgeEnabled = BUILDER
            .comment("Nudge strong riparian corridors toward floodplain_meadow on dry-land archetypes.")
            .define("riparianBiomeNudgeEnabled", true);

    public static final ForgeConfigSpec.DoubleValue riparianBiomeNudgeStrength = BUILDER
            .comment("Minimum riparian strength (0-1) for floodplain biome nudge.")
            .defineInRange("riparianBiomeNudgeStrength", 0.65D, 0.1D, 1.0D);

    public static final ForgeConfigSpec.DoubleValue riparianWetlandStrength = BUILDER
            .comment("Minimum riparian strength (0-1) for wetland variant profiles.")
            .defineInRange("riparianWetlandStrength", 0.45D, 0.1D, 1.0D);

    public static final ForgeConfigSpec.DoubleValue riparianGalleryTreeStrength = BUILDER
            .comment("Minimum riparian strength (0-1) for gallery-tree density boost on steppe/savanna/plains.")
            .defineInRange("riparianGalleryTreeStrength", 0.35D, 0.1D, 1.0D);

    public static final ForgeConfigSpec.ConfigValue<String> floraPlacementDefault = BUILDER
            .comment("Default flora placement for new worlds: historical (native ranges) or biome (climate-based, Materia-like).")
            .define("floraPlacementDefault", "historical");

    public static final ForgeConfigSpec SPEC = BUILDER.build();

    private TerracraftConfig()
    {
    }
}
