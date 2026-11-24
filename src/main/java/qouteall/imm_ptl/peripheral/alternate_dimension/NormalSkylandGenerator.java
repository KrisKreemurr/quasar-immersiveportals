// QUASAR STUB: parked peripheral feature (dimension stack / alternate dimensions / portal wand / command stick), see src/parked.
package qouteall.imm_ptl.peripheral.alternate_dimension;

import com.google.common.base.Suppliers;
import com.mojang.datafixers.DataFixer;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.progress.ChunkProgressListener;
import net.minecraft.tags.TagKey;
import net.minecraft.util.thread.BlockableEventLoop;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.biome.*;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ChunkGeneratorStructureState;
import net.minecraft.world.level.chunk.LightChunkGetter;
import net.minecraft.world.level.entity.ChunkStatusUpdateListener;
import net.minecraft.world.level.levelgen.densityfunction.DensityFunction;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.minecraft.world.level.levelgen.synth.NormalNoise;
import net.minecraft.world.level.storage.LevelStorageSource;
import org.jetbrains.annotations.NotNull;
import qouteall.imm_ptl.peripheral.mixin.common.alternate_dimension.IEChunkAccess_AlternateDim;
import qouteall.imm_ptl.peripheral.mixin.common.alternate_dimension.IEChunkGenerator_AlternateDim;
import qouteall.imm_ptl.peripheral.mixin.common.alternate_dimension.IENoiseRouterData;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Supplier;
import java.util.stream.Stream;

// QUASAR STUB: generated no-op stand-in; the original lives in src/parked (render rewrite pending).

public class NormalSkylandGenerator extends NoiseBasedChunkGenerator {

    public static MapCodec<NormalSkylandGenerator> MAP_CODEC;
    private RandomState delegatedRandomState;
    private HolderGetter<Biome> biomeHolderGetter;
    private HolderGetter<DensityFunction> densityFunctionHolderGetter;
    private HolderGetter<NormalNoise.NoiseParameters> noiseParametersHolderGetter;
    private long seed;
    public NormalSkylandGenerator(
        BiomeSource biomeSource,
        Holder<NoiseGeneratorSettings> noiseGeneratorSettings,
        
        NoiseBasedChunkGenerator delegate,
        
        HolderGetter<Biome> biomeHolderGetter,
        HolderGetter<DensityFunction> densityFunctionHolderGetter,
        HolderGetter<NormalNoise.NoiseParameters> noiseParametersHolderGetter,
        long seed
    ) { super(biomeSource, noiseGeneratorSettings); }

    public static NormalSkylandGenerator create(
        HolderGetter<Biome> biomeHolderGetter,
        HolderGetter<DensityFunction> densityFunctionHolderGetter,
        HolderGetter<NormalNoise.NoiseParameters> noiseParametersHolderGetter,
        HolderGetter<NoiseGeneratorSettings> noiseGeneratorSettingsHolderGetter,
        HolderGetter<MultiNoiseBiomeSourceParameterList> biomeParamListLookup,
        long seed
    ) { return null; }

    private NoiseBasedChunkGenerator delegate;
    protected @NotNull MapCodec<? extends ChunkGenerator> codec() { return null; }

    public CompletableFuture<ChunkAccess> fillFromNoise(
        Blender blender, RandomState pRandomState,
        StructureManager structureManager, ChunkAccess chunkAccess
    ) { return null; }

    public ChunkGeneratorStructureState createState(HolderLookup<StructureSet> structureSetLookup, RandomState randomState, long seed) { return null; }

    protected NormalSkylandGenerator() {}

}

