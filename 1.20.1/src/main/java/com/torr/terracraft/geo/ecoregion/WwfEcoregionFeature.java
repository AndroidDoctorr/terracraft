package com.torr.terracraft.geo.ecoregion;

import java.util.List;

public record WwfEcoregionFeature(
        int ecoId,
        String name,
        int biomeCode,
        int g200Biome,
        String realm,
        List<List<List<double[]>>> polygons,
        double minLatitude,
        double maxLatitude,
        double minLongitude,
        double maxLongitude
)
{
    public boolean intersects(double minLat, double maxLat, double minLon, double maxLon)
    {
        return maxLatitude >= minLat && minLatitude <= maxLat && maxLongitude >= minLon && minLongitude <= maxLon;
    }

    public double boundingArea()
    {
        return Math.max(0.0D, maxLatitude - minLatitude) * Math.max(0.0D, maxLongitude - minLongitude);
    }
}
