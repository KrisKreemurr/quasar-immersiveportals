// QUASAR STUB: parked peripheral feature (dimension stack / alternate dimensions / portal wand / command stick), see src/parked.
package qouteall.imm_ptl.peripheral.wand;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import qouteall.imm_ptl.core.portal.animation.UnilateralPortalState;
import qouteall.q_misc_util.my_util.Circle;
import qouteall.q_misc_util.my_util.DQuaternion;
import qouteall.q_misc_util.my_util.Plane;
import qouteall.q_misc_util.my_util.WithDim;

// QUASAR STUB: generated no-op stand-in; the original lives in src/parked (render rewrite pending).

public class ProtoPortalSide {

    public ResourceKey<Level> dimension;
    public Vec3 leftBottom;
    public Vec3 rightBottom;
    public Vec3 leftTop;
    public ProtoPortalSide(@NotNull ResourceKey<Level> dimension, @NotNull Vec3 leftBottom) {}

    public ProtoPortalSide copy() { return null; }

    public Vec3 getHorizontalAxis() { return null; }

    public Vec3 getVerticalAxis() { return null; }

    public Vec3 getNormal() { return null; }

    public double getWidth() { return 0D; }

    public double getHeight() { return 0D; }

    public double getHeightDivWidth() { return 0D; }

    @Nullable
    public ProtoPortalSide undo() { return null; }

    public boolean isComplete() { return false; }

    @Nullable
    public WithDim<Plane> getCursorConstraintPlane() { return null; }

    @Nullable
    public WithDim<Circle> getCursorConstraintCircle(double heightDivWidth) { return null; }

    public boolean isValidPlacement(@Nullable Double heightDivWidth) { return false; }

    public void placeCursor(Vec3 pos) {}

    public UnilateralPortalState toUnilateralPortalState() { return null; }

    protected ProtoPortalSide() {}

}

