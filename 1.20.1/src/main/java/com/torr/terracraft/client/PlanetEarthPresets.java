package com.torr.terracraft.client;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.presets.WorldPreset;

public final class PlanetEarthPresets
{
    public static final ResourceKey<WorldPreset> PLANET_EARTH = ResourceKey.create(
            Registries.WORLD_PRESET,
            new ResourceLocation("terracraft", "planet_earth")
    );

    private PlanetEarthPresets()
    {
    }
}
