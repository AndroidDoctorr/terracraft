package com.torr.terracraft.world;

import com.torr.terracraft.config.FloraPlacementMode;
import com.torr.terracraft.config.PlanetEarthSettingsHolder;
import com.torr.terracraft.world.gen.TerracraftBiomeSource;
import com.torr.terracraft.world.gen.TerracraftChunkGenerator;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;

import java.util.Optional;

public final class PlanetEarthSettingsHelper
{
    private PlanetEarthSettingsHelper()
    {
    }

    public static Optional<TerracraftBiomeSource> biomeSource(ServerLevel level)
    {
        return biomeSource(level.getChunkSource().getGenerator());
    }

    public static Optional<TerracraftBiomeSource> biomeSource(ChunkGenerator generator)
    {
        if (!(generator instanceof TerracraftChunkGenerator))
        {
            return Optional.empty();
        }

        if (!(generator.getBiomeSource() instanceof TerracraftBiomeSource source))
        {
            return Optional.empty();
        }

        return Optional.of(source);
    }

    public static FloraPlacementMode floraPlacement(ServerLevel level)
    {
        return biomeSource(level)
                .map(TerracraftBiomeSource::floraPlacement)
                .orElse(FloraPlacementMode.defaultMode());
    }

    public static void syncFromBiomeSource(TerracraftBiomeSource source)
    {
        PlanetEarthSettingsHolder.apply(
                source.horizontalScale(),
                source.elevationMapping(),
                source.floraPlacement(),
                source.ecoregionBorderBlendBlocks()
        );
    }

    public static void syncFromLevel(ServerLevel level)
    {
        biomeSource(level).ifPresent(PlanetEarthSettingsHelper::syncFromBiomeSource);
    }
}
