package com.torr.terracraft.client;

import com.torr.terracraft.config.ElevationMappingMode;
import com.torr.terracraft.config.FloraPlacementMode;
import com.torr.terracraft.config.HorizontalScaleOption;
import com.torr.terracraft.config.TerracraftConfig;
import com.torr.terracraft.world.gen.TerracraftBiomeSource;
import com.torr.terracraft.world.gen.TerracraftChunkGenerator;
import net.minecraft.client.gui.screens.worldselection.WorldCreationContext;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.dimension.LevelStem;

public record PlanetEarthSettings(
        FloraPlacementMode floraPlacement,
        HorizontalScaleOption horizontalScale,
        ElevationMappingMode elevationMapping
)
{
    public static PlanetEarthSettings defaults()
    {
        return new PlanetEarthSettings(
                FloraPlacementMode.defaultMode(),
                HorizontalScaleOption.fromBlocksPerDegree(TerracraftConfig.horizontalScale.get()),
                TerracraftConfig.elevationMappingMode()
        );
    }

    public static PlanetEarthSettings read(WorldCreationContext context)
    {
        return context.selectedDimensions().get(LevelStem.OVERWORLD)
                .map(LevelStem::generator)
                .flatMap(PlanetEarthSettings::read)
                .orElseGet(PlanetEarthSettings::defaults);
    }

    private static java.util.Optional<PlanetEarthSettings> read(ChunkGenerator generator)
    {
        if (!(generator instanceof TerracraftChunkGenerator))
        {
            return java.util.Optional.empty();
        }

        if (!(generator.getBiomeSource() instanceof TerracraftBiomeSource biomeSource))
        {
            return java.util.Optional.empty();
        }

        return java.util.Optional.of(new PlanetEarthSettings(
                biomeSource.floraPlacement(),
                HorizontalScaleOption.fromBlocksPerDegree(biomeSource.horizontalScale()),
                biomeSource.elevationMapping()
        ));
    }
}
