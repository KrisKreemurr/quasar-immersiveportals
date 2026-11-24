// QUASAR STUB: parked peripheral feature (dimension stack / alternate dimensions / portal wand / command stick), see src/parked.
package qouteall.imm_ptl.peripheral.dim_stack;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.Nullable;
import qouteall.q_misc_util.Helper;

// QUASAR STUB: generated no-op stand-in; the original lives in src/parked (render rewrite pending).

public class DimStackEntry {

    public String dimensionIdStr;
    public double scale;
    public boolean flipped;
    public double horizontalRotation;
    public Integer topY;
    public Integer bottomY;
    public String bedrockReplacementStr;
    public boolean connectsPrevious;
    public boolean connectsNext;
    public DimStackEntry(ResourceKey<Level> dimension) {}

    public DimStackEntry() {}

    public ResourceKey<Level> getDimension() { return null; }

    public DimStackEntry copy() { return null; }

}

