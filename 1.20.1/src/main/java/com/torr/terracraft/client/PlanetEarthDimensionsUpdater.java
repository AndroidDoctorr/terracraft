package com.torr.terracraft.client;

import com.torr.terracraft.world.gen.TerracraftBiomeSource;
import com.torr.terracraft.world.gen.TerracraftChunkGenerator;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.WorldDimensions;

public final class PlanetEarthDimensionsUpdater
{
    private PlanetEarthDimensionsUpdater()
    {
    }

    public static WorldDimensions apply(
            PlanetEarthSettings settings,
            WorldDimensions dimensions,
            RegistryAccess.Frozen registries
    )
    {
        return dimensions.get(LevelStem.OVERWORLD)
                .map(LevelStem::generator)
                .flatMap(generator -> replaceOverworldGenerator(settings, dimensions, registries, generator))
                .orElse(dimensions);
    }

    private static java.util.Optional<WorldDimensions> replaceOverworldGenerator(
            PlanetEarthSettings settings,
            WorldDimensions dimensions,
            RegistryAccess.Frozen registries,
            ChunkGenerator generator
    )
    {
        if (!(generator instanceof TerracraftChunkGenerator earthGenerator))
        {
            return java.util.Optional.empty();
        }

        if (!(earthGenerator.getBiomeSource() instanceof TerracraftBiomeSource currentSource))
        {
            return java.util.Optional.empty();
        }

        TerracraftBiomeSource updatedSource = currentSource.withSettings(
                settings.floraPlacement(),
                settings.horizontalScale().blocksPerDegree(),
                settings.elevationMapping()
        );
        TerracraftChunkGenerator updatedGenerator = new TerracraftChunkGenerator(updatedSource);
        return java.util.Optional.of(dimensions.replaceOverworldGenerator(registries, updatedGenerator));
    }
}
