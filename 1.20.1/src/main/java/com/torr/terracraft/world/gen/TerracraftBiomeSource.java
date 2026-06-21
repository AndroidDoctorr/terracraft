package com.torr.terracraft.world.gen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.torr.terracraft.terracraft;
import com.torr.terracraft.config.ElevationMappingMode;
import com.torr.terracraft.config.FloraPlacementMode;
import com.torr.terracraft.config.PlanetEarthSettingsHolder;
import com.torr.terracraft.config.TerracraftConfig;
import com.torr.terracraft.geo.BiomePlacement;
import com.torr.terracraft.geo.EarthProjection;
import com.torr.terracraft.geo.ElevationSamplerHolder;
import com.torr.terracraft.geo.ecoregion.EcoregionInfo;
import com.torr.terracraft.geo.ecoregion.EcoregionSamplerHolder;
import com.torr.terracraft.world.biome.BiomeTransition;
import com.torr.terracraft.world.biome.TerracraftBiomes;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.QuartPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.Climate;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class TerracraftBiomeSource extends BiomeSource
{
    public static final double DEFAULT_HORIZONTAL_SCALE = 100_000.0D;
    public static final int DEFAULT_BORDER_BLEND_BLOCKS = 64;

    public static final Codec<TerracraftBiomeSource> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.LONG.fieldOf("seed").forGetter(TerracraftBiomeSource::seed),
                    Codec.STRING.optionalFieldOf("flora_placement", FloraPlacementMode.defaultMode().configValue())
                            .forGetter(source -> source.floraPlacement().configValue()),
                    Codec.DOUBLE.optionalFieldOf("horizontal_scale", DEFAULT_HORIZONTAL_SCALE)
                            .forGetter(TerracraftBiomeSource::horizontalScale),
                    Codec.STRING.optionalFieldOf("elevation_mapping", ElevationMappingMode.COASTAL_LOG.name().toLowerCase())
                            .forGetter(source -> source.elevationMapping().name().toLowerCase()),
                    Codec.INT.optionalFieldOf("ecoregion_border_blend_blocks", DEFAULT_BORDER_BLEND_BLOCKS)
                            .forGetter(TerracraftBiomeSource::ecoregionBorderBlendBlocks),
                    RegistryOps.retrieveGetter(Registries.BIOME)
            ).apply(instance, TerracraftBiomeSource::new)
    );

    private static final List<ResourceKey<Biome>> VANILLA_BIOMES = List.of(
            Biomes.OCEAN, Biomes.DEEP_OCEAN, Biomes.WARM_OCEAN, Biomes.FROZEN_OCEAN, Biomes.DEEP_FROZEN_OCEAN,
            Biomes.BEACH, Biomes.SNOWY_BEACH, Biomes.STONY_SHORE,
            Biomes.GROVE, Biomes.STONY_PEAKS, Biomes.JAGGED_PEAKS, Biomes.SNOWY_SLOPES,
            Biomes.PLAINS
    );

    private final long seed;
    private final FloraPlacementMode floraPlacement;
    private final double horizontalScale;
    private final ElevationMappingMode elevationMapping;
    private final int ecoregionBorderBlendBlocks;
    private final HolderGetter<Biome> biomeLookup;

    public TerracraftBiomeSource(
            long seed,
            String floraPlacement,
            double horizontalScale,
            String elevationMapping,
            int ecoregionBorderBlendBlocks,
            HolderGetter<Biome> biomeLookup
    )
    {
        this(
                seed,
                FloraPlacementMode.fromConfig(floraPlacement),
                horizontalScale,
                ElevationMappingMode.fromConfig(elevationMapping),
                ecoregionBorderBlendBlocks,
                biomeLookup
        );
    }

    public TerracraftBiomeSource(
            long seed,
            FloraPlacementMode floraPlacement,
            double horizontalScale,
            ElevationMappingMode elevationMapping,
            int ecoregionBorderBlendBlocks,
            HolderGetter<Biome> biomeLookup
    )
    {
        this.seed = seed;
        this.floraPlacement = floraPlacement;
        this.horizontalScale = horizontalScale;
        this.elevationMapping = elevationMapping;
        this.ecoregionBorderBlendBlocks = ecoregionBorderBlendBlocks;
        this.biomeLookup = biomeLookup;
        PlanetEarthSettingsHolder.apply(
                horizontalScale,
                elevationMapping,
                floraPlacement,
                ecoregionBorderBlendBlocks
        );
    }

    public TerracraftBiomeSource(long seed, HolderGetter<Biome> biomeLookup)
    {
        this(
                seed,
                FloraPlacementMode.defaultMode(),
                TerracraftConfig.horizontalScale.get(),
                TerracraftConfig.elevationMappingMode(),
                TerracraftConfig.ecoregionBorderBlendBlocks.get(),
                biomeLookup
        );
    }

    public long seed()
    {
        return seed;
    }

    public FloraPlacementMode floraPlacement()
    {
        return floraPlacement;
    }

    public double horizontalScale()
    {
        return horizontalScale;
    }

    public ElevationMappingMode elevationMapping()
    {
        return elevationMapping;
    }

    public int ecoregionBorderBlendBlocks()
    {
        return ecoregionBorderBlendBlocks;
    }

    public HolderGetter<Biome> biomeLookup()
    {
        return biomeLookup;
    }

    public TerracraftBiomeSource withSettings(
            FloraPlacementMode floraPlacement,
            double horizontalScale,
            ElevationMappingMode elevationMapping
    )
    {
        return new TerracraftBiomeSource(
                seed,
                floraPlacement,
                horizontalScale,
                elevationMapping,
                ecoregionBorderBlendBlocks,
                biomeLookup
        );
    }

    @Override
    protected Codec<? extends BiomeSource> codec()
    {
        return CODEC;
    }

    @Override
    protected Stream<Holder<Biome>> collectPossibleBiomes()
    {
        List<ResourceKey<Biome>> possible = new ArrayList<>(VANILLA_BIOMES);
        possible.addAll(TerracraftBiomes.allRegistered());
        return possible.stream()
                .map(biomeLookup::get)
                .flatMap(java.util.Optional::stream);
    }

    @Override
    public Holder<Biome> getNoiseBiome(int quartX, int quartY, int quartZ, Climate.Sampler sampler)
    {
        int blockX = QuartPos.toBlock(quartX);
        int blockZ = QuartPos.toBlock(quartZ);
        double latitude = EarthProjection.blockZToLatitude(blockZ);
        double longitude = EarthProjection.blockXToLongitude(blockX);
        double elevationMeters = ElevationSamplerHolder.get().sampleElevationMeters(latitude, longitude);
        ResourceKey<Biome> biomeKey = BiomePlacement.classify(latitude, longitude, elevationMeters);

        if (TerracraftBiomes.isTerracraft(biomeKey))
        {
            EcoregionInfo ecoregion = EcoregionSamplerHolder.get().sample(latitude, longitude);
            biomeKey = BiomeTransition.apply(
                    seed,
                    blockX,
                    blockZ,
                    latitude,
                    longitude,
                    ecoregion,
                    biomeKey,
                    floraPlacement
            );
        }

        return resolveBiome(biomeKey);
    }

    private Holder<Biome> resolveBiome(ResourceKey<Biome> biomeKey)
    {
        if (TerracraftBiomes.isTerracraft(biomeKey) && !TerracraftBiomes.isRegistered(biomeKey))
        {
            terracraft.LOGGER.warn("Unregistered Terracraft biome {} — falling back to plains_palearctic", biomeKey.location());
            biomeKey = TerracraftBiomes.PLAINS_PALEARCTIC;
        }

        return biomeLookup.get(biomeKey)
                .or(() -> biomeLookup.get(TerracraftBiomes.PLAINS_PALEARCTIC))
                .or(() -> biomeLookup.get(Biomes.PLAINS))
                .orElseThrow(() -> new IllegalStateException("Fallback plains biome missing from registry"));
    }
}
