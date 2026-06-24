package com.torr.terracraft.world;

import com.torr.terracraft.terracraft;
import com.torr.terracraft.world.gen.TerracraftBiomeSource;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = terracraft.MOD_ID)
public final class PlanetEarthWorldEvents
{
    private PlanetEarthWorldEvents()
    {
    }

    @SubscribeEvent
    public static void onLevelLoad(LevelEvent.Load event)
    {
        if (!(event.getLevel() instanceof ServerLevel level))
        {
            return;
        }

        if (!level.dimension().equals(Level.OVERWORLD))
        {
            return;
        }

        PlanetEarthSettingsHelper.biomeSource(level).ifPresent(source ->
        {
            PlanetEarthSettingsHelper.syncFromBiomeSource(source);
            terracraft.LOGGER.info(
                    "Planet Earth world settings: scale {} blocks/deg, flora {}, elevation {}",
                    source.horizontalScale(),
                    source.floraPlacement().configValue(),
                    source.elevationMapping().name().toLowerCase()
            );
        });
    }
}
