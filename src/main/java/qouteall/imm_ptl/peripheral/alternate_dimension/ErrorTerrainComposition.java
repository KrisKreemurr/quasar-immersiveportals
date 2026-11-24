// QUASAR STUB: parked peripheral feature (dimension stack / alternate dimensions / portal wand / command stick), see src/parked.
package qouteall.imm_ptl.peripheral.alternate_dimension;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

// QUASAR STUB: generated no-op stand-in; the original lives in src/parked (render rewrite pending).

public class ErrorTerrainComposition {

    public static BlockState air;
    public static BlockState stone;
    public static BlockState water;
    public static RegionErrorTerrainGenerator.Composition mountain;
    public static RegionErrorTerrainGenerator.Composition classicalSolid;
    public static RegionErrorTerrainGenerator.Composition classicalHollow;
    public static RegionErrorTerrainGenerator.Composition classicalWatery;
    public static RegionErrorTerrainGenerator.Composition newSolid;
    public static RegionErrorTerrainGenerator.Composition floatingSea;
    public static RegionErrorTerrainGenerator.Composition treasured;
    public static RegionErrorTerrainGenerator.Composition layeredHollow;
    private static double getSplitPointClassical(int worldY, double middle, double unitFactor) { return 0D; }

    public static RandomSelector<RegionErrorTerrainGenerator.Composition> selector;
}

