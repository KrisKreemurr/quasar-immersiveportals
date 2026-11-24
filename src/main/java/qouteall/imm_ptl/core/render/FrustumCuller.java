// QUASAR STUB: real code parked in src/parked (rendering is replaced for Quasar MC).
package qouteall.imm_ptl.core.render;

import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;
import qouteall.imm_ptl.core.CHelper;
import qouteall.imm_ptl.core.IPCGlobal;
import qouteall.imm_ptl.core.compat.iris_compatibility.IrisInterface;
import qouteall.imm_ptl.core.compat.sodium_compatibility.SodiumInterface;
import qouteall.imm_ptl.core.portal.Mirror;
import qouteall.imm_ptl.core.portal.Portal;
import qouteall.imm_ptl.core.portal.animation.UnilateralPortalState;
import qouteall.imm_ptl.core.render.context_management.PortalRendering;
import qouteall.q_misc_util.my_util.BoxPredicateF;
import qouteall.q_misc_util.my_util.Plane;
import java.util.Comparator;

// QUASAR STUB: generated no-op stand-in; the original lives in src/parked (render rewrite pending).

@OnlyIn(Dist.CLIENT)
public class FrustumCuller {

    private BoxPredicateF canDetermineInvisibleFunc;
    private double camX;
    private double camY;
    private double camZ;
    public FrustumCuller() {}

    public void update(double cameraX, double cameraY, double cameraZ) {}

    public boolean canDetermineInvisibleWithCameraCoord(
        float minX, float minY, float minZ, float maxX, float maxY, float maxZ
    ) { return false; }

    public boolean canDetermineInvisibleWithCameraCoord(
        double minX, double minY, double minZ, double maxX, double maxY, double maxZ
    ) { return false; }

    public boolean canDetermineInvisibleWithWorldCoord(
        double minX, double minY, double minZ, double maxX, double maxY, double maxZ
    ) { return false; }

    private @Nullable BoxPredicateF getCanDetermineInvisibleFunc(
        double cameraX,
        double cameraY,
        double cameraZ
    ) { return null; }

    @Nullable
    private static Portal getCurrentNearestVisibleCullablePortal() { return null; }

    public static Vec3[] getRectPortalFourVerticesCounterClockwise(
        UnilateralPortalState thisSideState
    ) { return null; }

    public static Frustum4Planes getFrustumPlanesFromFourVerticesCounterClockwise(
        Vec3[] vertices
    ) { return null; }

    public static BoxPredicateF getFlatPortalInnerFrustumCullingFunc(
        Portal portal, Vec3 cameraPos
    ) { return null; }

    public static BoxPredicateF getFlatPortalOuterFrustumCullingFunc(
        Portal portal, Vec3 cameraPos
    ) { return null; }

    public static boolean isFullyInFrontOfPlane(
        float minX, float minY, float minZ, float maxX, float maxY, float maxZ,
        float planeX, float planeY, float planeZ, float planeW
    ) { return false; }

    public static boolean isFullyBehindPlane(
        float minX, float minY, float minZ, float maxX, float maxY, float maxZ,
        float planeX, float planeY, float planeZ, float planeW
    ) { return false; }

    public record Frustum4Planes(
        float p0X, float p0Y, float p0Z, float p0W,
        float p1X, float p1Y, float p1Z, float p1W,
        float p2X, float p2Y, float p2Z, float p2W,
        float p3X, float p3Y, float p3Z, float p3W
    ) {

        public boolean isFullyOutside(
            float minX, float minY, float minZ, float maxX, float maxY, float maxZ
        ) { return false; }

        public boolean isFullyInside(
            float minX, float minY, float minZ, float maxX, float maxY, float maxZ
        ) { return false; }

    }

    @Deprecated
    public static enum BatchTestResult {

        all_true, all_false, both;
    }

    @Deprecated
    public static BatchTestResult testBoxTwoVertices(
        double minX, double minY, double minZ,
        double maxX, double maxY, double maxZ,
        double planeNormalX, double planeNormalY, double planeNormalZ,
        double planePosX, double planePosY, double planePosZ
    ) { return null; }

    @Deprecated
    public static BatchTestResult testBoxTwoVertices(
        double minX, double minY, double minZ,
        double maxX, double maxY, double maxZ,
        Vec3 planeNormal
    ) { return null; }

    @Deprecated
    private static boolean isInFrontOf(
        double x, double y, double z,
        double planeNormalX, double planeNormalY, double planeNormalZ
    ) { return false; }

}

