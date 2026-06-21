package com.torr.terracraft.config;

public final class FloraPlacementHolder
{
    private static FloraPlacementMode mode = FloraPlacementMode.defaultMode();

    private FloraPlacementHolder()
    {
    }

    public static FloraPlacementMode get()
    {
        return mode;
    }

    public static void set(FloraPlacementMode replacement)
    {
        mode = replacement;
    }
}
