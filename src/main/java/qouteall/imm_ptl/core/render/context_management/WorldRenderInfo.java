// QUASAR STUB: real code parked in src/parked (rendering is replaced for Quasar MC).
package qouteall.imm_ptl.core.render.context_management;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import qouteall.imm_ptl.core.IPGlobal;
import qouteall.imm_ptl.core.ducks.IECamera;
import java.util.List;
import java.util.Stack;
import java.util.UUID;
import java.util.stream.Collectors;

// QUASAR STUB: generated no-op stand-in; the original lives in src/parked (render rewrite pending).

public class WorldRenderInfo {

    public static record IsometricParameters(
    
    ) {

    }

    public ClientLevel world;
    public Vec3 cameraPos;
    public boolean overwriteCameraTransformation;
    public Matrix4f cameraTransformation;
    public UUID description;
    public int renderDistance;
    public boolean doRenderHand;
    public boolean enableViewBobbing;
    public boolean doRenderSky;
    public boolean hasFog;
    public IsometricParameters isometricParameters;
    private static Stack<WorldRenderInfo> renderInfoStack;
    private static List<UUID> renderingDescCache;
    private WorldRenderInfo(
        ClientLevel world, Vec3 cameraPos,
        @Nullable Matrix4f cameraTransformation,
        boolean overwriteCameraTransformation,
        @Nullable UUID description,
        int renderDistance,
        boolean doRenderHand,
        boolean enableViewBobbing,
        boolean doRenderSky,
        boolean hasFog,
        @Nullable IsometricParameters isometricParameters
    ) {}

    public static void pushRenderInfo(WorldRenderInfo worldRenderInfo) {}

    public static void popRenderInfo() {}

    public static void adjustCameraPos(Camera camera) {}

    public static void applyAdditionalTransformations(PoseStack matrixStack) {}

    public static boolean isRendering() { return false; }

    public static int getRenderingLayer() { return 0; }

    public static List<UUID> getRenderingDescription() { return null; }

    public static int getRenderDistance() { return 0; }

    public static WorldRenderInfo getTopRenderInfo() { return null; }

    public static Vec3 getCameraPos() { return null; }

    public static boolean isViewBobbingEnabled() { return false; }

    public static boolean isFogEnabled() { return false; }

    public static class Builder {

        private ClientLevel world;
        private Vec3 cameraPos;
        private Matrix4f cameraTransformation;
        private boolean overwriteCameraTransformation;
        private UUID description;
        private int renderDistance;
        private boolean doRenderHand;
        private boolean enableViewBobbing;
        private boolean doRenderSky;
        private boolean hasFog;
        private IsometricParameters isometricParameters;
        public Builder() {}

        public Builder setWorld(ClientLevel world) { return null; }

        public Builder setCameraPos(Vec3 cameraPos) { return null; }

        public Builder setCameraTransformation(Matrix4f cameraTransformation) { return null; }

        public Builder setOverwriteCameraTransformation(boolean overwriteCameraTransformation) { return null; }

        public Builder setDescription(UUID description) { return null; }

        public Builder setRenderDistance(int renderDistance) { return null; }

        public Builder setDoRenderHand(boolean doRenderHand) { return null; }

        public Builder setEnableViewBobbing(boolean enableViewBobbing) { return null; }

        public Builder setDoRenderSky(boolean doRenderSky) { return null; }

        public Builder setHasFog(boolean hasFog) { return null; }

        public Builder setIsometricParameters(@Nullable IsometricParameters isometricParameters) { return null; }

        public WorldRenderInfo build() { return null; }

    }

    protected WorldRenderInfo() {}

}

