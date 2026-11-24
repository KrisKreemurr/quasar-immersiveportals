// QUASAR STUB: parked peripheral feature (dimension stack / alternate dimensions / portal wand / command stick), see src/parked.
package qouteall.imm_ptl.peripheral.wand;

import com.mojang.logging.LogUtils;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import qouteall.imm_ptl.core.portal.Portal;
import qouteall.imm_ptl.core.portal.animation.UnilateralPortalState;
import qouteall.q_misc_util.my_util.Circle;
import qouteall.q_misc_util.my_util.DQuaternion;
import qouteall.q_misc_util.my_util.Plane;
import qouteall.q_misc_util.my_util.Sphere;

// QUASAR STUB: generated no-op stand-in; the original lives in src/parked (render rewrite pending).

public enum PortalCorner {

    LEFT_BOTTOM, LEFT_TOP, RIGHT_BOTTOM, RIGHT_TOP;
    private static Logger LOGGER;
    public int getXSign() { return 0; }

    public int getYSign() { return 0; }

    public Vec3 getOffset(Portal portal) { return null; }

    public Vec3 getPos(Portal portal) { return null; }

    public Vec3 getOffset(UnilateralPortalState ups) { return null; }

    public Vec3 getPos(UnilateralPortalState ups) { return null; }

    public static UnilateralPortalState performDragWithNoLockedCorner(
        UnilateralPortalState originalState,
        PortalCorner freeCorner, Vec3 freePos
    ) { return null; }

    @Nullable
    public static UnilateralPortalState performDragWith1LockedCorner(
        UnilateralPortalState originalState,
        PortalCorner lockedCorner, Vec3 lockedPos,
        PortalCorner freeCorner, Vec3 freePos
    ) { return null; }

    public record DraggingConstraint(
        @Nullable Plane plane,
        @Nullable Sphere sphere
    ) {

        @Nullable
        public Vec3 constrain(Vec3 pos) { return null; }

    }

    public static DraggingConstraint getDraggingConstraintWith2LockedCorners(
        PortalCorner lockedCorner1, Vec3 lockedPos1,
        PortalCorner lockedCorner2, Vec3 lockedPos2,
        PortalCorner freeCorner
    ) { return null; }

    @Nullable
    public static UnilateralPortalState performDragWith2LockedCorners(
        UnilateralPortalState originalState,
        PortalCorner lockedCorner1, Vec3 lockedPos1,
        PortalCorner lockedCorner2, Vec3 lockedPos2,
        PortalCorner freeCorner, Vec3 freePos
    ) { return null; }

}

