// QUASAR STUB: parked peripheral feature (dimension stack / alternate dimensions / portal wand / command stick), see src/parked.
package qouteall.imm_ptl.peripheral.alternate_dimension;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.util.LinearCongruentialGenerator;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Climate;
import org.jetbrains.annotations.NotNull;
import qouteall.imm_ptl.core.McHelper;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

// QUASAR STUB: generated no-op stand-in; the original lives in src/parked (render rewrite pending).

public class ChaosBiomeSource extends BiomeSource {

    public static String[] vanillaBiomes;
    public static MapCodec<ChaosBiomeSource> MAP_CODEC;
    private HolderSet<Biome> allowedBiomes;
    public ChaosBiomeSource(HolderSet<Biome> holderSet) { super(); }

    @NotNull
    public static ChaosBiomeSource createChaosBiomeSource(HolderGetter<Biome> biomeHolderGetter) { return null; }

    private Holder<Biome> getRandomBiome(int x, int z) { return null; }

    protected MapCodec<? extends BiomeSource> codec() { return null; }

    protected Stream<Holder<Biome>> collectPossibleBiomes() { return null; }

    public Holder<Biome> getNoiseBiome(int x, int y, int z, Climate.Sampler sampler) { return null; }

    protected ChaosBiomeSource() {}

}

