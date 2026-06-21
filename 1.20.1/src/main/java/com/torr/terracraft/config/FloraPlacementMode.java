package com.torr.terracraft.config;

import com.torr.terracraft.config.TerracraftConfig;

public enum FloraPlacementMode
{
    HISTORICAL,
    BIOME;

    public static FloraPlacementMode fromConfig(String value)
    {
        if (value == null)
        {
            return defaultMode();
        }

        return switch (value.trim().toLowerCase())
        {
            case "biome", "climate", "biome_flora" -> BIOME;
            case "historical", "native", "precolumbian", "pre_columbian" -> HISTORICAL;
            default -> defaultMode();
        };
    }

    public static FloraPlacementMode defaultMode()
    {
        return fromConfig(TerracraftConfig.floraPlacementDefault.get());
    }

    public String configValue()
    {
        return this == BIOME ? "biome" : "historical";
    }
}
