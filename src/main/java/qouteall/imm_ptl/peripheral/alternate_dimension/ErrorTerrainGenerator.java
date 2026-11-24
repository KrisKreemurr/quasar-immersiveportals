// QUASAR STUB: parked peripheral feature (dimension stack / alternate dimensions / portal wand / command stick), see src/parked.
package qouteall.imm_ptl.peripheral.alternate_dimension;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.blending.Blender;
import org.jetbrains.annotations.NotNull;
import qouteall.q_misc_util.Helper;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

// QUASAR STUB: generated no-op stand-in; the original lives in src/parked (render rewrite pending).

public class ErrorTerrainGenerator extends DelegatedChunkGenerator {

    public static MapCodec<ErrorTerrainGenerator> MAP_CODEC;
    public static ErrorTerrainGenerator create(
        HolderGetter<Biome> biomeHolderGetter,
        HolderGetter<NoiseGeneratorSettings> noiseGeneratorSettingsHolderGetter
    ) { return null; }

    public static int regionChunkNum;
    public static int averageY;
    public static int maxY;
    private BlockState air;
    private BlockState defaultBlock;
    private BlockState defaultFluid;
    private LoadingCache<ChunkPos, RegionErrorTerrainGenerator> cache;
    public ErrorTerrainGenerator(
        BiomeSource biomeSource, ChunkGenerator delegate
    ) { super(biomeSource, delegate); }

    protected @NotNull MapCodec<? extends ChunkGenerator> codec() { return null; }

    public @NotNull CompletableFuture<ChunkAccess> fillFromNoise(Blender blender, RandomState randomState, StructureManager structureManager, ChunkAccess chunkAccess) { return null; }

    public void doPopulateNoise(ChunkAccess chunk) {}

    protected ErrorTerrainGenerator() {}

}

