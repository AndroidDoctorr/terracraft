package com.torr.terracraft.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.torr.terracraft.config.FloraPlacementMode;
import com.torr.terracraft.config.HorizontalScaleOption;
import com.torr.terracraft.geo.BiomePlacement;
import com.torr.terracraft.geo.BlockGeoTarget;
import com.torr.terracraft.geo.EarthProjection;
import com.torr.terracraft.geo.GeoCoordinate;
import com.torr.terracraft.geo.ecoregion.EcoregionInfo;
import com.torr.terracraft.geo.ecoregion.EcoregionSamplerHolder;
import com.torr.terracraft.world.PlanetEarthSettingsHelper;
import com.torr.terracraft.world.gen.TerracraftBiomeSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.storage.PrimaryLevelData;
import net.minecraft.world.level.storage.WorldData;
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
                        .executes(context -> showCoords(context.getSource())))
                .then(Commands.literal("debuggen")
                        .executes(context -> showDebugGen(context.getSource()))));
    }

    private static int teleport(CommandSourceStack source, double latitude, double longitude, Double elevationMeters) throws CommandSyntaxException
    {
        ServerPlayer player = source.getPlayerOrException();
        PlanetEarthSettingsHelper.syncFromLevel(player.serverLevel());
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
        PlanetEarthSettingsHelper.syncFromLevel(level);
        FloraPlacementMode floraMode = PlanetEarthSettingsHelper.floraPlacement(level);

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
                geo.elevationMeters(),
                floraMode
        );
        EcoregionInfo ecoregion = EcoregionSamplerHolder.get().sample(geo.latitude(), geo.longitude());
        double blocksPerDegree = EarthProjection.blocksPerDegree();
        String scaleLabel = HorizontalScaleOption.fromBlocksPerDegree(blocksPerDegree).label().getString();

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
        source.sendSuccess(() -> Component.literal(String.format(Locale.ROOT,
                "World scale: %s (%.0f blocks/deg), flora: %s, ecoregions: %s",
                scaleLabel,
                blocksPerDegree,
                floraMode.configValue(),
                EcoregionSamplerHolder.isStub() ? "STUB (run tools/download_ecoregions.ps1)" : "loaded"
        )), false);
        if (!placedBiomeId.equals(classifiedBiome.location().toString()))
        {
            source.sendSuccess(() -> Component.literal(String.format(Locale.ROOT,
                    "Biome mismatch: classified=%s, placed=%s",
                    classifiedBiome.location(),
                    placedBiomeId
            )), false);
        }
        return 1;
    }

    private static int showDebugGen(CommandSourceStack source) throws CommandSyntaxException
    {
        ServerPlayer player = source.getPlayerOrException();
        ServerLevel level = player.serverLevel();
        PlanetEarthSettingsHelper.syncFromLevel(level);
        BlockPos pos = player.blockPosition();
        ChunkAccess chunk = level.getChunk(pos.getX() >> 4, pos.getZ() >> 4);
        int surfaceWg = chunk.getHeight(Heightmap.Types.WORLD_SURFACE_WG, pos.getX() & 15, pos.getZ() & 15);
        int motionBlocking = chunk.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, pos.getX() & 15, pos.getZ() & 15);
        int possibleBiomes = level.getChunkSource().getGenerator().getBiomeSource().possibleBiomes().size();
        WorldData worldData = level.getServer().getWorldData();
        boolean generateStructures = worldData instanceof PrimaryLevelData primary
                && primary.worldGenOptions().generateStructures();
        double savedScale = PlanetEarthSettingsHelper.biomeSource(level)
                .map(TerracraftBiomeSource::horizontalScale)
                .orElse(-1.0D);
        double activeScale = EarthProjection.blocksPerDegree();

        source.sendSuccess(() -> Component.translatable(
                "commands.terracraft.debuggen.success",
                surfaceWg,
                motionBlocking,
                possibleBiomes,
                Boolean.toString(generateStructures),
                chunk.getHighestGeneratedStatus().toString()
        ), false);
        source.sendSuccess(() -> Component.literal(String.format(Locale.ROOT,
                "Scale saved in world: %.0f blocks/deg, active projection: %.0f blocks/deg%s",
                savedScale,
                activeScale,
                Math.abs(savedScale - activeScale) > 1.0D ? " (MISMATCH — reload bug?)" : ""
        )), false);
        return 1;
    }

    private static String formatCoord(double value)
    {
        return String.format(Locale.ROOT, "%.4f", value);
    }
}
