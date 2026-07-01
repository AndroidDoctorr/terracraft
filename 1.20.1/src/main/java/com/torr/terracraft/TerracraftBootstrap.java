package com.torr.terracraft;

import com.torr.terracraft.config.TerracraftConfig;
import com.torr.terracraft.geo.DemTileCache;
import com.torr.terracraft.geo.ElevationSamplerHolder;
import com.torr.terracraft.geo.StubElevationSampler;
import com.torr.terracraft.geo.TerrariumElevationSampler;
import com.torr.terracraft.geo.TerrainElevationMapper;
import com.torr.terracraft.geo.ecoregion.CachedEcoregionSampler;
import com.torr.terracraft.geo.ecoregion.EcoregionSamplerHolder;
import com.torr.terracraft.geo.ecoregion.EcoregionTileCache;
import com.torr.terracraft.geo.ecoregion.WwfEcoregionDataset;
import com.torr.terracraft.geo.ecoregion.WwfEcoregionDownloader;
import com.torr.terracraft.geo.hydro.CachedHydroLakeSampler;
import com.torr.terracraft.geo.hydro.HydroLakeDataset;
import com.torr.terracraft.geo.hydro.HydroLakeDownloader;
import com.torr.terracraft.geo.hydro.HydroLakeSamplerHolder;
import com.torr.terracraft.geo.hydro.HydroLakeTileCache;
import com.torr.terracraft.geo.hydro.RegionalWaterSamplerHolder;
import com.torr.terracraft.world.biome.BiomeCloneRegistry;
import com.torr.terracraft.world.biome.BiomeTransitionRegistry;
import com.torr.terracraft.world.biome.BiomeVariantRegistry;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

import java.nio.file.Files;
import java.nio.file.Path;

@Mod.EventBusSubscriber(modid = terracraft.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class TerracraftBootstrap
{
    private static DemTileCache demTileCache;
    private static EcoregionTileCache ecoregionTileCache;
    private static HydroLakeTileCache hydroLakeTileCache;

    private TerracraftBootstrap()
    {
    }

    @SubscribeEvent
    public static void onCommonSetup(FMLCommonSetupEvent event)
    {
        event.enqueueWork(() ->
        {
            BiomeCloneRegistry.load();
            BiomeVariantRegistry.load();
            BiomeTransitionRegistry.load();
            com.torr.terracraft.integration.MateriaIntegration.init();
            com.torr.terracraft.geo.regional.RegionalBiomeZones.load();
            initElevationSampler();
            initEcoregionSampler();
            initRegionalWaterSampler();
            initHydroLakeSampler();
        });
    }

    public static void initRegionalWaterSampler()
    {
        if (!TerracraftConfig.regionalWaterEnabled.get())
        {
            RegionalWaterSamplerHolder.resetToStub();
            terracraft.LOGGER.info("Terracraft regional water: disabled");
            return;
        }

        try
        {
            HydroLakeDataset dataset = HydroLakeDataset.loadFromResource(
                    "regional/chicago_water.geojson",
                    "Chicago OSM"
            );
            RegionalWaterSamplerHolder.set(new CachedHydroLakeSampler(dataset, null));
            terracraft.LOGGER.info("Terracraft regional water: {} Chicago metro polygons (OSM)", dataset.featureCount());
        }
        catch (Exception exception)
        {
            terracraft.LOGGER.warn("Terracraft regional water: failed to initialize — {}", exception.toString());
            RegionalWaterSamplerHolder.resetToStub();
        }
    }

    public static void initElevationSampler()
    {
        if (TerracraftConfig.useStubElevation.get())
        {
            ElevationSamplerHolder.set(new StubElevationSampler());
            terracraft.LOGGER.info("Terracraft elevation: using stub terrain");
            return;
        }

        Path cacheRoot = FMLPaths.GAMEDIR.get().resolve("terracraft").resolve("dem_cache");
        demTileCache = new DemTileCache(
                cacheRoot,
                TerracraftConfig.demTileUrlTemplate.get(),
                TerracraftConfig.demDownloadThreads.get()
        );
        int zoom = TerracraftConfig.demZoom.get();
        ElevationSamplerHolder.set(new TerrariumElevationSampler(demTileCache, zoom));
        if (TerrainElevationMapper.isEnabled())
        {
            int baselineZoom = TerrariumElevationSampler.baselineZoomFor(zoom);
            terracraft.LOGGER.info(
                    "Terracraft elevation: Terrarium z{} (baseline z{}), high-pass detail scale {}, cache {}",
                    zoom,
                    baselineZoom,
                    TerracraftConfig.elevationDetailVerticalScale.get(),
                    cacheRoot.toAbsolutePath()
            );
        }
        else
        {
            terracraft.LOGGER.info("Terracraft elevation: AWS Terrarium tiles (zoom {}, cache {}), mapping {}",
                    zoom,
                    cacheRoot.toAbsolutePath(),
                    TerracraftConfig.elevationMapping.get());
        }
    }

    public static void initEcoregionSampler()
    {
        if (!TerracraftConfig.useEcoregionBiomes.get())
        {
            EcoregionSamplerHolder.resetToStub();
            terracraft.LOGGER.info("Terracraft ecoregions: disabled (useEcoregionBiomes=false)");
            return;
        }

        Path dataRoot = FMLPaths.GAMEDIR.get().resolve("terracraft").resolve("data");
        Path geoJsonPath = resolveDataPath(dataRoot, TerracraftConfig.ecoregionDataFile.get());

        try
        {
            if (TerracraftConfig.autoDownloadEcoregionData.get())
            {
                WwfEcoregionDownloader.downloadIfMissing(geoJsonPath, TerracraftConfig.ecoregionQueryUrlTemplate.get());
            }

            if (!Files.isRegularFile(geoJsonPath))
            {
                terracraft.LOGGER.warn(
                        "Terracraft ecoregions: data file missing at {} — geographic/climate fallback only. "
                                + "Run tools/download_ecoregions.ps1 or enable autoDownloadEcoregionData.",
                        geoJsonPath.toAbsolutePath()
                );
                EcoregionSamplerHolder.resetToStub();
                return;
            }

            WwfEcoregionDataset dataset = WwfEcoregionDataset.load(geoJsonPath);
            Path cacheRoot = FMLPaths.GAMEDIR.get().resolve("terracraft").resolve("ecoregion_cache_v3");
            int supersample = TerracraftConfig.ecoregionRasterSupersample.get();
            ecoregionTileCache = new EcoregionTileCache(
                    cacheRoot,
                    TerracraftConfig.ecoregionZoom.get(),
                    supersample,
                    dataset
            );
            EcoregionSamplerHolder.set(new CachedEcoregionSampler(ecoregionTileCache));
            terracraft.LOGGER.info("Terracraft ecoregions: {} unique regions, zoom {}, supersample {}x, cache {}",
                    dataset.uniqueEcoIdCount(),
                    TerracraftConfig.ecoregionZoom.get(),
                    supersample,
                    cacheRoot.toAbsolutePath());
        }
        catch (Exception exception)
        {
            terracraft.LOGGER.warn("Terracraft ecoregions: failed to initialize — {}", exception.toString());
            EcoregionSamplerHolder.resetToStub();
        }
    }

    public static void initHydroLakeSampler()
    {
        boolean naturalEarth = TerracraftConfig.useHydroLakePolygons.get();
        boolean supplement = TerracraftConfig.hydroLakeSupplementEnabled.get();

        if (!naturalEarth && !supplement)
        {
            HydroLakeSamplerHolder.resetToStub();
            terracraft.LOGGER.info("Terracraft hydro lakes: disabled");
            return;
        }

        try
        {
            HydroLakeDataset dataset;
            if (naturalEarth)
            {
                Path dataRoot = FMLPaths.GAMEDIR.get().resolve("terracraft").resolve("data");
                Path geoJsonPath = resolveDataPath(dataRoot, TerracraftConfig.hydroLakeDataFile.get());

                if (TerracraftConfig.autoDownloadHydroLakeData.get())
                {
                    HydroLakeDownloader.downloadIfMissing(geoJsonPath, TerracraftConfig.hydroLakeDownloadUrl.get());
                }

                if (!Files.isRegularFile(geoJsonPath))
                {
                    terracraft.LOGGER.warn(
                            "Terracraft hydro lakes: Natural Earth file missing at {} — skipping NE polygons.",
                            geoJsonPath.toAbsolutePath()
                    );
                    if (!supplement)
                    {
                        HydroLakeSamplerHolder.resetToStub();
                        return;
                    }
                    dataset = HydroLakeDataset.loadSupplementOnly();
                }
                else
                {
                    dataset = HydroLakeDataset.load(geoJsonPath);
                }
            }
            else
            {
                dataset = HydroLakeDataset.loadSupplementOnly();
            }

            Path cacheRoot = FMLPaths.GAMEDIR.get().resolve("terracraft").resolve("hydro_lake_cache_v1");
            hydroLakeTileCache = new HydroLakeTileCache(cacheRoot, TerracraftConfig.hydroLakeZoom.get(), dataset);
            HydroLakeSamplerHolder.set(new CachedHydroLakeSampler(dataset, hydroLakeTileCache));
            terracraft.LOGGER.info("Terracraft hydro lakes: {} polygons (NE={}, supplement={}), cache {}",
                    dataset.featureCount(),
                    naturalEarth,
                    supplement,
                    cacheRoot.toAbsolutePath());
        }
        catch (Exception exception)
        {
            terracraft.LOGGER.warn("Terracraft hydro lakes: failed to initialize — {}", exception.toString());
            HydroLakeSamplerHolder.resetToStub();
        }
    }

    public static HydroLakeTileCache hydroLakeTileCache()
    {
        return hydroLakeTileCache;
    }

    private static Path resolveDataPath(Path dataRoot, String configuredPath)
    {
        Path configured = Path.of(configuredPath);
        if (configured.isAbsolute())
        {
            return configured;
        }
        return dataRoot.resolve(configuredPath);
    }

    public static DemTileCache demTileCache()
    {
        return demTileCache;
    }

    public static EcoregionTileCache ecoregionTileCache()
    {
        return ecoregionTileCache;
    }
}
