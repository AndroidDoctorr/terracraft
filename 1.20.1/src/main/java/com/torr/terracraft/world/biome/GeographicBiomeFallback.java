package com.torr.terracraft.world.biome;

import com.torr.terracraft.geo.ecoregion.EcoregionSamplerHolder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;

import java.util.Optional;

/**
 * When WWF ecoregion polygons are unavailable, infer recognizable clone biomes from geography.
 */
public final class GeographicBiomeFallback
{
    private GeographicBiomeFallback()
    {
    }

    public static Optional<ResourceKey<Biome>> at(double latitude, double longitude, double elevationMeters)
    {
        if (EcoregionSamplerHolder.get().sample(latitude, longitude).ecoId() != 0)
        {
            return Optional.empty();
        }

        if (TerracraftClimateMapper.inMediterraneanBasin(latitude, longitude))
        {
            return Optional.of(TerracraftBiomes.MEDITERRANEAN_SCRUB);
        }

        if (TerracraftClimateMapper.inSouthwestUs(latitude, longitude))
        {
            if (elevationMeters >= 1800.0D)
            {
                return Optional.of(TerracraftBiomes.key("montane_meadow"));
            }
            return Optional.of(TerracraftBiomes.key("chaparral_nearctic"));
        }

        if (latitude >= 25.0D && latitude <= 37.0D && longitude >= 34.0D && longitude <= 60.0D)
        {
            return Optional.of(TerracraftBiomes.key("semi_arid_scrub"));
        }

        return Optional.empty();
    }
}
