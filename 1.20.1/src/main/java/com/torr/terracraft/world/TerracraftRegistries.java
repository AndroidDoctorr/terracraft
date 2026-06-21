package com.torr.terracraft.world;

import com.torr.terracraft.world.gen.TerracraftBiomeSource;
import com.torr.terracraft.world.gen.TerracraftChunkGenerator;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.RegisterEvent;

public final class TerracraftRegistries
{
    public static final ResourceLocation EARTH_CHUNK_GENERATOR = new ResourceLocation("terracraft", "earth");
    public static final ResourceLocation EARTH_BIOME_SOURCE = new ResourceLocation("terracraft", "earth");

    private TerracraftRegistries()
    {
    }

    public static void register(IEventBus modEventBus)
    {
        modEventBus.addListener(TerracraftRegistries::onRegister);
    }

    private static void onRegister(RegisterEvent event)
    {
        event.register(Registries.CHUNK_GENERATOR, helper ->
                helper.register(EARTH_CHUNK_GENERATOR, TerracraftChunkGenerator.CODEC));
        event.register(Registries.BIOME_SOURCE, helper ->
                helper.register(EARTH_BIOME_SOURCE, TerracraftBiomeSource.CODEC));
    }
}
