package com.torr.terracraft.geo;

import com.torr.terracraft.config.PlanetEarthSettingsHolder;
import net.minecraft.util.Mth;

public final class EarthProjection
{
    private EarthProjection()
    {
    }

    public static double blocksPerDegree()
    {
        return PlanetEarthSettingsHolder.horizontalScale();
    }

    public static double blockXToLongitude(int blockX)
    {
        return blockX / blocksPerDegree();
    }

    public static double blockZToLatitude(int blockZ)
    {
        return -blockZ / blocksPerDegree();
    }

    public static int longitudeToBlockX(double longitude)
    {
        return Mth.floor(longitude * blocksPerDegree() + 0.5D);
    }

    public static int latitudeToBlockZ(double latitude)
    {
        return Mth.floor(-latitude * blocksPerDegree() + 0.5D);
    }

    public static int elevationMetersToBlockY(double elevationMeters)
    {
        return ElevationScale.metersToBlockY(elevationMeters);
    }

    public static GeoCoordinate blockToGeo(int blockX, int blockY, int blockZ)
    {
        double latitude = blockZToLatitude(blockZ);
        double longitude = blockXToLongitude(blockX);
        double elevationMeters = ElevationScale.blockYToMeters(blockY);
        return new GeoCoordinate(latitude, longitude, elevationMeters);
    }

    public static BlockGeoTarget geoToBlock(double latitude, double longitude, double elevationMeters)
    {
        return new BlockGeoTarget(
                longitudeToBlockX(longitude),
                elevationMetersToBlockY(elevationMeters),
                latitudeToBlockZ(latitude),
                elevationMeters
        );
    }
}
