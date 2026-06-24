package com.torr.terracraft.world.biome;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;

import java.util.ArrayList;
import java.util.List;

/**
 * Biomes that can actually appear in Planet Earth generation must be listed in
 * {@link com.torr.terracraft.world.gen.TerracraftBiomeSource#collectPossibleBiomes()}
 * or {@link net.minecraft.world.level.chunk.ChunkGenerator#applyBiomeDecoration} skips
 * their surface features.
 *
 * <p>Land biomes are always Terracraft clones ({@link TerracraftClimateMapper} maps vanilla
 * heuristics before placement). Only ocean / shore / alpine vanilla biomes remain. Do not
 * include unused vanilla land biomes here — Materia's modifiers target them and
 * {@link net.minecraft.world.level.biome.FeatureSorter} will crash with feature-order cycles
 * against Terracraft clone biomes that use Terracraft's Materia bridge.</p>
 */
public final class AssignableBiomes
{
    private static final List<ResourceKey<Biome>> VANILLA_OCEAN_AND_ALPINE = List.of(
            Biomes.OCEAN,
            Biomes.DEEP_OCEAN,
            Biomes.LUKEWARM_OCEAN,
            Biomes.WARM_OCEAN,
            Biomes.COLD_OCEAN,
            Biomes.FROZEN_OCEAN,
            Biomes.DEEP_LUKEWARM_OCEAN,
            Biomes.DEEP_COLD_OCEAN,
            Biomes.DEEP_FROZEN_OCEAN,
            Biomes.BEACH,
            Biomes.SNOWY_BEACH,
            Biomes.STONY_SHORE,
            Biomes.JAGGED_PEAKS,
            Biomes.STONY_PEAKS,
            Biomes.FROZEN_PEAKS,
            Biomes.SNOWY_SLOPES
    );

    private AssignableBiomes()
    {
    }

    public static List<ResourceKey<Biome>> allKeys()
    {
        List<ResourceKey<Biome>> keys = new ArrayList<>(VANILLA_OCEAN_AND_ALPINE.size() + 64);
        keys.addAll(VANILLA_OCEAN_AND_ALPINE);
        keys.addAll(TerracraftBiomes.allRegistered());
        return keys;
    }
}
