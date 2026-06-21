package com.torr.terracraft.config;

import net.minecraft.network.chat.Component;

public enum HorizontalScaleOption
{
    ONE_TO_ONE(100_000.0D, "terracraft.create_world.scale.one_to_one"),
    ONE_TO_TWO(50_000.0D, "terracraft.create_world.scale.one_to_two"),
    ONE_TO_FOUR(25_000.0D, "terracraft.create_world.scale.one_to_four");

    private final double blocksPerDegree;
    private final String translationKey;

    HorizontalScaleOption(double blocksPerDegree, String translationKey)
    {
        this.blocksPerDegree = blocksPerDegree;
        this.translationKey = translationKey;
    }

    public double blocksPerDegree()
    {
        return blocksPerDegree;
    }

    public Component label()
    {
        return Component.translatable(translationKey);
    }

    public static HorizontalScaleOption fromBlocksPerDegree(double blocksPerDegree)
    {
        double closestDistance = Double.MAX_VALUE;
        HorizontalScaleOption closest = ONE_TO_ONE;
        for (HorizontalScaleOption option : values())
        {
            double distance = Math.abs(option.blocksPerDegree - blocksPerDegree);
            if (distance < closestDistance)
            {
                closestDistance = distance;
                closest = option;
            }
        }
        return closest;
    }
}
