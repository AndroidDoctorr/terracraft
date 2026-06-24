package com.torr.terracraft;

import com.torr.terracraft.config.TerracraftConfig;
import com.torr.terracraft.geo.DemTileCache;
import com.torr.terracraft.geo.ElevationSamplerHolder;
import com.torr.terracraft.geo.StubElevationSampler;
import com.torr.terracraft.geo.TerrariumElevationSampler;
import com.torr.terracraft.geo.ecoregion.CachedEcoregionSampler;
import com.torr.terracraft.geo.ecoregion.EcoregionSamplerHolder;
import com.torr.terracraft.geo.ecoregion.EcoregionTileCache;
import com.torr.terracraft.geo.ecoregion.WwfEcoregionDataset;
import com.torr.terracraft.geo.ecoregion.WwfEcoregionDownloader;
import com.torr.terracraft.world.biome.BiomeCloneRegistry;
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

    private TerracraftBootstrap()
    {
    }

    @SubscribeEvent
    public static void onCommonSetup(FMLCommonSetupEvent event)
    {
        event.enqueueWork(() ->
        {
            BiomeCloneRegistry.load();
            initElevationSampler();
            initEcoregionSampler();
        });
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
        ElevationSamplerHolder.set(new TerrariumElevationSampler(demTileCache, TerracraftConfig.demZoom.get()));
        terracraft.LOGGER.info("Terracraft elevation: AWS Terrarium tiles (zoom {}, cache {}), mapping {}",
                TerracraftConfig.demZoom.get(),
                cacheRoot.toAbsolutePath(),
                TerracraftConfig.elevationMapping.get());
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
            Path cacheRoot = FMLPaths.GAMEDIR.get().resolve("terracraft").resolve("ecoregion_cache_v2");
            ecoregionTileCache = new EcoregionTileCache(cacheRoot, TerracraftConfig.ecoregionZoom.get(), dataset);
            EcoregionSamplerHolder.set(new CachedEcoregionSampler(ecoregionTileCache));
            terracraft.LOGGER.info("Terracraft ecoregions: {} unique regions, zoom {}, cache {}",
                    dataset.uniqueEcoIdCount(),
                    TerracraftConfig.ecoregionZoom.get(),
                    cacheRoot.toAbsolutePath());
        }
        catch (Exception exception)
        {
            terracraft.LOGGER.warn("Terracraft ecoregions: failed to initialize — {}", exception.toString());
            EcoregionSamplerHolder.resetToStub();
        }
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
