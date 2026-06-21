package com.torr.terracraft.geo;

/**
 * Procedural stand-in until DEM tile fetching is implemented.
 * Produces smooth hills so the chunk generator can be tested in-game.
 */
public final class StubElevationSampler implements ElevationSampler
{
    @Override
    public double sampleElevationMeters(double latitude, double longitude)
    {
        double latRadians = Math.toRadians(latitude);
        double lonRadians = Math.toRadians(longitude);
        double base = Math.sin(latRadians * 6.0D) * 400.0D + Math.cos(lonRadians * 5.0D) * 250.0D;
        double detail = Math.sin(latRadians * 24.0D + lonRadians * 18.0D) * 40.0D;
        return base + detail;
    }
}
