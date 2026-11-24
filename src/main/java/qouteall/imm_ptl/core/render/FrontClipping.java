// QUASAR STUB: real code parked in src/parked (rendering is replaced for Quasar MC).
package qouteall.imm_ptl.core.render;

import com.mojang.blaze3d.shaders.Uniform;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL11;
import qouteall.imm_ptl.core.CHelper;
import qouteall.imm_ptl.core.IPCGlobal;
import qouteall.imm_ptl.core.IPGlobal;
import qouteall.imm_ptl.core.ducks.IEShader;
import qouteall.imm_ptl.core.portal.Portal;
import qouteall.imm_ptl.core.render.context_management.PortalRendering;
import qouteall.q_misc_util.my_util.Plane;

// QUASAR STUB: generated no-op stand-in; the original lives in src/parked (render rewrite pending).

public class FrontClipping {

    private static Minecraft client;
    private static double[] activeClipPlaneEquationBeforeModelView;
    private static double[] activeClipPlaneAfterModelView;
    public static boolean isClippingEnabled;
    public static double ADJUSTMENT;
    public static void disableClipping() {}

    private static void enableClipping() {}

    public static void updateInnerClipping(PoseStack matrixStack) {}

    public static void updateInnerClipping(Matrix4f modelView) {}

    public static void setupInnerClipping(
        Plane clipping, Matrix4f modelView, double adjustment
    ) {}

    private static double[] transformClipEquation(
        double[] equation, Matrix4f modelView
    ) { return null; }

    private static double[] getClipEquationInner(
        Vec3 clippingPoint, Vec3 clippingDirection, double correction
    ) { return null; }

    public static void setupOuterClipping(PoseStack matrixStack, Portal portal) {}

    private static double @Nullable [] getClipEquationOuter(Portal portal) { return null; }

    public static double[] getActiveClipPlaneEquationBeforeModelView() { return null; }

    public static double[] getActiveClipPlaneEquationAfterModelView() { return null; }

    public static void updateClippingEquationUniformForCurrentShader(
        boolean isRenderingEntities
    ) {}

    public static void unsetClippingUniform() {}

}

