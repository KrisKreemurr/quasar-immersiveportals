// QUASAR STUB: real code parked in src/parked (rendering is replaced for Quasar MC).
package qouteall.imm_ptl.core.render.context_management;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import qouteall.imm_ptl.core.CHelper;
import qouteall.imm_ptl.core.IPGlobal;
import qouteall.imm_ptl.core.portal.Mirror;
import qouteall.imm_ptl.core.portal.Portal;
import qouteall.imm_ptl.core.portal.shape.BoxPortalShape;
import qouteall.imm_ptl.core.render.VisibleSectionDiscovery;
import qouteall.imm_ptl.core.render.renderer.PortalRenderer;
import qouteall.q_misc_util.my_util.Plane;
import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Stack;
import java.util.function.Function;
import java.util.stream.Collectors;

// QUASAR STUB: generated no-op stand-in; the original lives in src/parked (render rewrite pending).

public class PortalRendering {

    private static Stack<Portal> portalLayers;
    private static boolean isRenderingCache;
    private static boolean isRenderingOddNumberOfMirrorsCache;
    public static void pushPortalLayer(Portal portal) {}

    public static void popPortalLayer() {}

    private static void updateCache() {}

    public static int getPortalLayer() { return 0; }

    public static boolean isRendering() { return false; }

    public static boolean isRenderingOddNumberOfMirrors() { return false; }

    public static int getMaxPortalLayer() { return 0; }

    public static @NotNull Portal getRenderingPortal() { return null; }

    public static void onBeginPortalWorldRendering() {}

    public static void onEndPortalWorldRendering() {}

    public static Vec3 getRenderingCameraPos() { return null; }

    public static double getExtraModelViewScaling() { return 0D; }

    public static boolean shouldEnableSodiumCaveCulling() { return false; }

    public static boolean shouldRenderHitResult() { return false; }

    public static @Nullable Plane getActiveClippingPlane() { return null; }

    public static boolean isInvalidRecursionRendering(
        Portal toRender
    ) { return false; }

}

