package com.torr.terracraft.geo;

public final class TerrariumTileMath
{
    private TerrariumTileMath()
    {
    }

    public record TileSample(int tileX, int tileY, int pixelX, int pixelY)
    {
    }

    public static TileSample locate(int zoom, double latitude, double longitude)
    {
        int tileX = longitudeToTileX(longitude, zoom);
        int tileY = latitudeToTileY(latitude, zoom);
        int pixelX = clampPixel(longitudeToPixelX(longitude, zoom, tileX));
        int pixelY = clampPixel(latitudeToPixelY(latitude, zoom, tileY));
        return new TileSample(tileX, tileY, pixelX, pixelY);
    }

    public record TileBounds(double minLatitude, double maxLatitude, double minLongitude, double maxLongitude)
    {
        public boolean intersects(double minLat, double maxLat, double minLon, double maxLon)
        {
            return maxLatitude >= minLat && minLatitude <= maxLat && maxLongitude >= minLon && minLongitude <= maxLon;
        }
    }

    public static TileBounds tileBounds(int zoom, int tileX, int tileY)
    {
        double tiles = 1 << zoom;
        double minLongitude = tileX / tiles * 360.0D - 180.0D;
        double maxLongitude = (tileX + 1) / tiles * 360.0D - 180.0D;
        double maxLatitude = latitudeAtTileRow(tileY, zoom);
        double minLatitude = latitudeAtTileRow(tileY + 1, zoom);
        return new TileBounds(minLatitude, maxLatitude, minLongitude, maxLongitude);
    }

    public static double latitudeAtTileRow(int tileRow, int zoom)
    {
        double tiles = 1 << zoom;
        double mercatorY = Math.PI - 2.0D * Math.PI * tileRow / tiles;
        return Math.toDegrees(Math.atan(Math.sinh(mercatorY)));
    }

    public static double decodeElevationMeters(int argb)
    {
        int red = (argb >> 16) & 0xFF;
        int green = (argb >> 8) & 0xFF;
        int blue = argb & 0xFF;
        return (red * 256.0D + green + blue / 256.0D) - 32768.0D;
    }

    public static int longitudeToTileX(double longitude, int zoom)
    {
        double tiles = 1 << zoom;
        return (int) Math.floor((longitude + 180.0D) / 360.0D * tiles);
    }

    public static int latitudeToTileY(double latitude, int zoom)
    {
        double latRadians = Math.toRadians(clampLatitude(latitude));
        double tiles = 1 << zoom;
        return (int) Math.floor((1.0D - Math.log(Math.tan(latRadians) + 1.0D / Math.cos(latRadians)) / Math.PI) / 2.0D * tiles);
    }

    public static int longitudeToPixelX(double longitude, int zoom, int tileX)
    {
        double worldPixels = (1 << zoom) * 256.0D;
        return (int) Math.floor((longitude + 180.0D) / 360.0D * worldPixels) - tileX * 256;
    }

    public static int latitudeToPixelY(double latitude, int zoom, int tileY)
    {
        double latRadians = Math.toRadians(clampLatitude(latitude));
        double worldPixels = (1 << zoom) * 256.0D;
        double worldY = (1.0D - Math.log(Math.tan(latRadians) + 1.0D / Math.cos(latRadians)) / Math.PI) / 2.0D * worldPixels;
        return (int) Math.floor(worldY) - tileY * 256;
    }

    private static int clampPixel(int pixel)
    {
        return Math.max(0, Math.min(255, pixel));
    }

    private static double clampLatitude(double latitude)
    {
        return Math.max(-85.05112878D, Math.min(85.05112878D, latitude));
    }
}
