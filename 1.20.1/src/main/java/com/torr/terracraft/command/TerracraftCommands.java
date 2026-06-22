package com.torr.terracraft.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.torr.terracraft.geo.BiomePlacement;
import com.torr.terracraft.geo.BlockGeoTarget;
import com.torr.terracraft.geo.EarthProjection;
import com.torr.terracraft.geo.GeoCoordinate;
import com.torr.terracraft.geo.ecoregion.EcoregionInfo;
import com.torr.terracraft.geo.ecoregion.EcoregionSamplerHolder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.biome.Biome;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Locale;

import static com.torr.terracraft.terracraft.MOD_ID;

@Mod.EventBusSubscriber(modid = MOD_ID)
public final class TerracraftCommands
{
    private TerracraftCommands()
    {
    }

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event)
    {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
        dispatcher.register(Commands.literal("tpll")
                .then(Commands.argument("latitude", DoubleArgumentType.doubleArg(-90.0D, 90.0D))
                        .then(Commands.argument("longitude", DoubleArgumentType.doubleArg(-180.0D, 180.0D))
                                .executes(context -> teleport(
                                        context.getSource(),
                                        DoubleArgumentType.getDouble(context, "latitude"),
                                        DoubleArgumentType.getDouble(context, "longitude"),
                                        null
                                ))
                                .then(Commands.argument("elevationMeters", DoubleArgumentType.doubleArg(-500.0D, 9000.0D))
                                        .executes(context -> teleport(
                                                context.getSource(),
                                                DoubleArgumentType.getDouble(context, "latitude"),
                                                DoubleArgumentType.getDouble(context, "longitude"),
                                                DoubleArgumentType.getDouble(context, "elevationMeters")
                                        ))))));

        dispatcher.register(Commands.literal("terracraft")
                .then(Commands.literal("coords")
                        .executes(context -> showCoords(context.getSource()))));
    }

    private static int teleport(CommandSourceStack source, double latitude, double longitude, Double elevationMeters) throws CommandSyntaxException
    {
        ServerPlayer player = source.getPlayerOrException();
        double elevation = elevationMeters != null
                ? elevationMeters
                : source.getLevel().getHeight(
                        net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                        EarthProjection.longitudeToBlockX(longitude),
                        EarthProjection.latitudeToBlockZ(latitude)
                );
        BlockGeoTarget target = EarthProjection.geoToBlock(latitude, longitude, elevation);
        player.teleportTo(target.blockX() + 0.5D, target.blockY(), target.blockZ() + 0.5D);

        source.sendSuccess(() -> Component.translatable(
                "commands.terracraft.tpll.success",
                formatCoord(latitude),
                formatCoord(longitude),
                formatCoord(elevation),
                target.blockY()
        ), true);
        return 1;
    }

    private static int showCoords(CommandSourceStack source) throws CommandSyntaxException
    {
        ServerPlayer player = source.getPlayerOrException();
        ServerLevel level = player.serverLevel();
        GeoCoordinate geo = EarthProjection.blockToGeo(
                player.blockPosition().getX(),
                player.blockPosition().getY(),
                player.blockPosition().getZ()
        );
        Holder<Biome> placedBiome = level.getBiome(player.blockPosition());
        String placedBiomeId = placedBiome.unwrapKey()
                .map(key -> key.location().toString())
                .orElse("unknown");
        ResourceKey<Biome> classifiedBiome = BiomePlacement.classify(
                geo.latitude(),
                geo.longitude(),
                geo.elevationMeters()
        );
        EcoregionInfo ecoregion = EcoregionSamplerHolder.get().sample(geo.latitude(), geo.longitude());

        source.sendSuccess(() -> Component.translatable(
                "commands.terracraft.coords.success",
                player.blockPosition().getX(),
                player.blockPosition().getY(),
                player.blockPosition().getZ(),
                formatCoord(geo.latitude()),
                formatCoord(geo.longitude()),
                formatCoord(geo.elevationMeters()),
                placedBiomeId,
                classifiedBiome.location().toString(),
                ecoregion.name(),
                Integer.toString(ecoregion.ecoId())
        ), false);
        return 1;
    }

    private static String formatCoord(double value)
    {
        return String.format(Locale.ROOT, "%.4f", value);
    }
}
