package com.torr.terracraft;

import com.mojang.logging.LogUtils;
import com.torr.terracraft.command.TerracraftCommands;
import com.torr.terracraft.config.TerracraftConfig;
import com.torr.terracraft.world.TerracraftRegistries;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(terracraft.MOD_ID)
public class terracraft
{
    public static final String MOD_ID = "terracraft";
    public static final Logger LOGGER = LogUtils.getLogger();

    public terracraft()
    {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, TerracraftConfig.SPEC);

        TerracraftRegistries.register(modEventBus);

        IEventBus forgeEventBus = MinecraftForge.EVENT_BUS;
        forgeEventBus.register(TerracraftCommands.class);
    }
}
