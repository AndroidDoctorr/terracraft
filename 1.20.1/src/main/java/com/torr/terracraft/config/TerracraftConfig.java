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
            .comment("Minimum real-world depth (meters) below the local spill elevation to classify a cell as an inland lake basin.")
            .defineInRange("depressionMinDepthMeters", 0.75D, 0.1D, 50.0D);

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
            .defineInRange("ecoregionBorderBlendBlocks", 64, 0, 512);

    public static final ForgeConfigSpec.ConfigValue<String> floraPlacementDefault = BUILDER
            .comment("Default flora placement for new worlds: historical (native ranges) or biome (climate-based, Materia-like).")
            .define("floraPlacementDefault", "historical");

    public static final ForgeConfigSpec SPEC = BUILDER.build();

    private TerracraftConfig()
    {
    }
}
