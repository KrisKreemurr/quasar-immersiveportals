// QUASAR STUB: real code parked in src/parked (rendering is replaced for Quasar MC).
package qouteall.imm_ptl.core.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import qouteall.imm_ptl.core.compat.GravityChangerInterface;
import qouteall.imm_ptl.core.portal.Portal;
import qouteall.imm_ptl.core.render.context_management.RenderStates;
import qouteall.imm_ptl.core.render.context_management.WorldRenderInfo;
import qouteall.q_misc_util.Helper;
import qouteall.q_misc_util.my_util.DQuaternion;

// QUASAR STUB: generated no-op stand-in; the original lives in src/parked (render rewrite pending).

@OnlyIn(Dist.CLIENT)
public class TransformationManager {

    private static DQuaternion animationDeltaStart;
    private static long interpolationStartTime;
    private static long interpolationEndTime;
    public static Minecraft client;
    public static boolean isIsometricView;
    public static float isometricViewLength;
    public static DQuaternion getPlayerCameraRotation() { return null; }

    private static DQuaternion getCameraRotationWithGravity(
        Direction gravityDirection,
        float pitch, float yaw
    ) { return null; }

    @Nullable
    private static DQuaternion getCurrentAnimationDelta() { return null; }

    private static void processTransformation(Camera camera, PoseStack matrixStack) {}

    public static Matrix4f processTransformation(Camera camera, Matrix4f matrix4f) { return null; }

    public static boolean isAnimationRunning() { return false; }

    @Nullable
    public static Double getAnimationProgress() { return null; }

    public static double mapProgress(double progress) { return 0D; }

    public static void managePlayerRotationAndChangeGravity(
        Portal portal
    ) {}

    private static double getAnimationDurationSeconds() { return 0D; }

    private static void updateCamera(Minecraft client) {}

    public static Matrix4f getMirrorTransformation(Vec3 normal) { return null; }

    public static Matrix4f getIsometricProjection() { return null; }

    public static boolean isCalculatingViewBobbingOffset;
    @OnlyIn(Dist.CLIENT)
    public static class RemoteCallables {

        public static void enableIsometricView(float viewLength) {}

        public static void disableIsometricView() {}

    }

    public static Vec3 getIsometricAdjustedCameraPos() { return null; }

    public static Vec3 getIsometricAdjustedCameraPos(Camera camera) { return null; }

}

