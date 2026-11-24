// QUASAR STUB: parked peripheral feature (dimension stack / alternate dimensions / portal wand / command stick), see src/parked.
package qouteall.imm_ptl.peripheral.alternate_dimension;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.util.random.WeightedRandomList;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ChunkGeneratorStructureState;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import qouteall.imm_ptl.core.miscellaneous.IPVanillaCopy;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

// QUASAR STUB: generated no-op stand-in; the original lives in src/parked (render rewrite pending).

@IPVanillaCopy
public abstract class DelegatedChunkGenerator extends ChunkGenerator {

    protected ChunkGenerator delegate;
    protected BiomeSource biomeSource_;
    public DelegatedChunkGenerator(
        BiomeSource biomeSource,
        ChunkGenerator delegate
    ) { super(biomeSource); }

    public void applyCarvers(WorldGenRegion worldGenRegion, long l, RandomState randomState, BiomeManager biomeManager, StructureManager structureManager, ChunkAccess chunkAccess, GenerationStep.Carving carving) {}

    public void buildSurface(WorldGenRegion worldGenRegion, StructureManager structureManager, RandomState randomState, ChunkAccess chunkAccess) {}

    public void spawnOriginalMobs(WorldGenRegion region) {}

    public int getGenDepth() { return 0; }

    public @NotNull CompletableFuture<ChunkAccess> fillFromNoise(Blender blender, RandomState randomState, StructureManager structureManager, ChunkAccess chunkAccess) { return null; }

    public int getSeaLevel() { return 0; }

    public int getMinY() { return 0; }

    public int getBaseHeight(int i, int j, Heightmap.Types types, LevelHeightAccessor levelHeightAccessor, RandomState randomState) { return 0; }

    public NoiseColumn getBaseColumn(int i, int j, LevelHeightAccessor levelHeightAccessor, RandomState randomState) { return null; }

    public void addDebugScreenInfo(List<String> list, RandomState randomState, BlockPos blockPos) {}

    public ChunkGeneratorStructureState createState(HolderLookup<StructureSet> holderLookup, RandomState randomState, long l) { return null; }

    public CompletableFuture<ChunkAccess> createBiomes(RandomState randomState, Blender blender, StructureManager structureManager, ChunkAccess chunkAccess) { return null; }

    public void createStructures(RegistryAccess registryAccess, ChunkGeneratorStructureState chunkGeneratorStructureState, StructureManager structureManager, ChunkAccess chunkAccess, StructureTemplateManager structureTemplateManager) {}

    public void applyBiomeDecoration(WorldGenLevel worldGenLevel, ChunkAccess chunkAccess, StructureManager structureManager) {}

    public Optional<ResourceKey<MapCodec<? extends ChunkGenerator>>> getTypeNameForDataFixer() { return null; }

    @Nullable
    public Pair<BlockPos, Holder<Structure>> findNearestMapStructure(ServerLevel serverLevel, HolderSet<Structure> holderSet, BlockPos blockPos, int i, boolean bl) { return null; }

    public int getSpawnHeight(LevelHeightAccessor level) { return 0; }

    public BiomeSource getBiomeSource() { return null; }

    public WeightedRandomList<MobSpawnSettings.SpawnerData> getMobsAt(Holder<Biome> holder, StructureManager structureManager, MobCategory mobCategory, BlockPos blockPos) { return null; }

    public void createReferences(WorldGenLevel worldGenLevel, StructureManager structureManager, ChunkAccess chunkAccess) {}

    public int getFirstFreeHeight(int i, int j, Heightmap.Types types, LevelHeightAccessor levelHeightAccessor, RandomState randomState) { return 0; }

    public int getFirstOccupiedHeight(int i, int j, Heightmap.Types types, LevelHeightAccessor levelHeightAccessor, RandomState randomState) { return 0; }

    public BiomeGenerationSettings getBiomeGenerationSettings(Holder<Biome> holder) { return null; }

    protected DelegatedChunkGenerator() {}

}

