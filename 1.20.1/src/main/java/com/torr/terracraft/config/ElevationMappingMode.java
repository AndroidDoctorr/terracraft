package com.torr.terracraft.config;

public enum ElevationMappingMode
{
    LINEAR,
    COASTAL_LOG;

    public static ElevationMappingMode fromConfig(String value)
    {
        if (value == null)
        {
            return COASTAL_LOG;
        }

        return switch (value.trim().toLowerCase())
        {
            case "linear" -> LINEAR;
            case "coastal_log", "coastal", "log" -> COASTAL_LOG;
            default -> COASTAL_LOG;
        };
    }
}
