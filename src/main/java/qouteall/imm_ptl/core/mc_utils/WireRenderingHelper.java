// QUASAR STUB: render helper parked (see src/parked).
package qouteall.imm_ptl.core.mc_utils;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import qouteall.imm_ptl.core.CHelper;
import qouteall.imm_ptl.core.portal.Portal;
import qouteall.imm_ptl.core.portal.animation.StableClientTimer;
import qouteall.imm_ptl.core.portal.animation.UnilateralPortalState;
import qouteall.imm_ptl.core.portal.shape.PortalShape;
import qouteall.imm_ptl.core.portal.shape.SpecialFlatPortalShape;
import qouteall.imm_ptl.core.render.context_management.RenderStates;
import qouteall.q_misc_util.my_util.Circle;
import qouteall.q_misc_util.my_util.DQuaternion;
import qouteall.q_misc_util.my_util.Plane;
import qouteall.q_misc_util.my_util.Sphere;
import java.util.Random;
import java.util.function.IntSupplier;

// QUASAR STUB: generated no-op stand-in; the original lives in src/parked (render rewrite pending).

public class WireRenderingHelper {

    public static void renderSmallCubeFrame(
        VertexConsumer vertexConsumer, Vec3 cameraPos, Vec3 boxCenter,
        int color, double scale,
        PoseStack matrixStack
    ) {}

    public static DQuaternion getRandomSmoothRotation(Random random) { return null; }

    public static double getRandomSmoothCycle(Random random) { return 0D; }

    @NotNull
    public static Vec3 randomVec(Random random) { return null; }

    public static void renderPlane(
        VertexConsumer vertexConsumer, Vec3 cameraPos,
        Plane plane, double renderedPlaneScale,
        int color, PoseStack matrixStack,
        boolean isLineStrip
    ) {}

    public static void renderCircle(
        Circle circle,
        int color, PoseStack matrixStack
    ) {}

    public static void renderLockShape(
        VertexConsumer vertexConsumer, Vec3 cameraPos,
        Vec3 center, double scale,
    ) {}

    public static void renderLines(
        VertexConsumer vertexConsumer,
        Vec3 cameraPos, Vec3 center,
        Vec3[] lineVertices,
        double scale, DQuaternion rotation,
        int color, PoseStack matrixStack
    ) {}

    public static void putLine(VertexConsumer vertexConsumer, int color, Matrix4f matrix, Matrix3f normalMatrix, Vec3 lineStart, Vec3 lineEnd) {}

    public static void putLine(
        VertexConsumer vertexConsumer,
        int color, Vec3 normal, Matrix4f matrix, Matrix3f normalMatrix,
        Vec3 lineStart, Vec3 lineEnd
    ) {}

    private static void putLineToLineStrip(
        VertexConsumer vertexConsumer, int color, Vec3 normal,
        Matrix4f matrix, Vec3 lineStart, Vec3 lineEnd
    ) {}

    public static void renderRectLine(
        VertexConsumer vertexConsumer, Vec3 cameraPos,
        UnilateralPortalState rect,
        int flowCount, int color, double shrinkFactor,
        int flowDirection,
        PoseStack matrixStack
    ) {}

    public static void renderFlowLines(
        VertexConsumer vertexConsumer,
        Vec3[] vertices,
        int flowCount, int color, int flowDirection,
        PoseStack matrixStack, IntSupplier randCycleSupplier
    ) {}

    private static Vec3[] getRectVertices(UnilateralPortalState rect, double shrinkFactor) { return null; }

    public static void renderSubLineInLineLoop(
        VertexConsumer vertexConsumer, Matrix4f matrix, Matrix3f normalMatrix,
        Vec3[] lineVertices, int color,
        double totalStartRatio, double totalEndRatio
    ) {}

    private static void putLinePart(
        VertexConsumer vertexConsumer, int color,
        Matrix4f matrix, Matrix3f normalMatrix, Vec3 lineStart, Vec3 lineEnd,
        double startRatio, double endRatio
    ) {}

    public static void renderSphere(
        VertexConsumer vertexConsumer, int color,
        PoseStack matrixStack, Vec3 cameraPos,
        Sphere sphere, DQuaternion sphereOrientation,
        double animationProgress, double rotationProgress
    ) {}

    public static void renderRectFrameFlow(
        PoseStack matrixStack, Vec3 cameraPos,
        VertexConsumer vertexConsumer, UnilateralPortalState rect,
        int innerColor, int outerColor
    ) {}

    public static void renderPortalShapeMeshDebug(
        PoseStack matrixStack, VertexConsumer vertexConsumer, Portal portal
    ) {}

}

