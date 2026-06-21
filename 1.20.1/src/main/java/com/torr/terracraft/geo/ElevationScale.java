package com.torr.terracraft.geo;

import com.torr.terracraft.config.PlanetEarthSettingsHolder;
import com.torr.terracraft.config.TerracraftConfig;
import net.minecraft.util.Mth;

/**
 * Converts real-world elevation (meters) to Minecraft Y offsets from sea level.
 * <p>
 * {@code coastal_log} keeps low-relief coasts and valleys steep enough to recognize (hills, shorelines),
 * while compressing high mountains so Everest still fits in vanilla build height.
 */
public final class ElevationScale
{
    private ElevationScale()
    {
    }

    public static double metersToBlockOffset(double elevationMeters)
    {
        double deltaMeters = elevationMeters - TerracraftConfig.seaLevelMeters.get();
        return switch (PlanetEarthSettingsHolder.elevationMapping())
        {
            case LINEAR -> deltaMeters * TerracraftConfig.verticalScale.get();
            case COASTAL_LOG -> coastalLogOffset(deltaMeters);
        };
    }

    public static double blockOffsetToMetersDelta(double blockOffset)
    {
        return switch (PlanetEarthSettingsHolder.elevationMapping())
        {
            case LINEAR -> blockOffset / TerracraftConfig.verticalScale.get();
            case COASTAL_LOG -> coastalLogInverse(blockOffset);
        };
    }

    public static int metersToBlockY(double elevationMeters)
    {
        double blockY = TerracraftConfig.seaLevelBlockY.get() + metersToBlockOffset(elevationMeters);
        return Mth.clamp(
                Mth.floor(blockY + 0.5D),
                TerracraftConfig.minWorldY.get(),
                TerracraftConfig.maxWorldY.get()
        );
    }

    public static double blockYToMeters(int blockY)
    {
        double blockOffset = blockY - TerracraftConfig.seaLevelBlockY.get();
        return TerracraftConfig.seaLevelMeters.get() + blockOffsetToMetersDelta(blockOffset);
    }

    /**
     * offset = coastalScale * kneeMeters * ln(1 + |delta| / kneeMeters)
     * <p>
     * Derivative at sea level equals {@code coastalScale} (blocks per meter).
     */
    private static double coastalLogOffset(double deltaMeters)
    {
        if (Math.abs(deltaMeters) < 1.0E-9D)
        {
            return 0.0D;
        }

        double sign = Math.signum(deltaMeters);
        double absDelta = Math.abs(deltaMeters);
        double coastalScale = TerracraftConfig.coastalVerticalScale.get();
        double kneeMeters = TerracraftConfig.elevationCompressionMeters.get();
        return sign * coastalScale * kneeMeters * Math.log1p(absDelta / kneeMeters);
    }

    private static double coastalLogInverse(double blockOffset)
    {
        if (Math.abs(blockOffset) < 1.0E-9D)
        {
            return 0.0D;
        }

        double sign = Math.signum(blockOffset);
        double absOffset = Math.abs(blockOffset);
        double coastalScale = TerracraftConfig.coastalVerticalScale.get();
        double kneeMeters = TerracraftConfig.elevationCompressionMeters.get();
        double scale = coastalScale * kneeMeters;
        if (scale <= 1.0E-9D)
        {
            return 0.0D;
        }

        return sign * kneeMeters * Math.expm1(absOffset / scale);
    }
}
