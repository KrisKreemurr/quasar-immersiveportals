// QUASAR STUB: real code parked in src/parked (rendering is replaced for Quasar MC).
package qouteall.imm_ptl.core.render.context_management;

import com.mojang.logging.LogUtils;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.slf4j.Logger;
import qouteall.imm_ptl.core.CHelper;
import qouteall.imm_ptl.core.ClientWorldLoader;
import qouteall.imm_ptl.core.IPGlobal;
import qouteall.imm_ptl.core.McHelper;
import qouteall.imm_ptl.core.block_manipulation.BlockManipulationClient;
import qouteall.imm_ptl.core.ducks.IEEntity;
import qouteall.imm_ptl.core.ducks.IEGameRenderer;
import qouteall.imm_ptl.core.miscellaneous.ClientPerformanceMonitor;
import qouteall.imm_ptl.core.mixin.client.particle.IEParticle;
import qouteall.imm_ptl.core.portal.Portal;
import qouteall.imm_ptl.core.portal.animation.StableClientTimer;
import qouteall.imm_ptl.core.render.ForceMainThreadRebuild;
import qouteall.imm_ptl.core.render.MyRenderHelper;
import qouteall.imm_ptl.core.render.QueryManager;
import qouteall.q_misc_util.Helper;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

// QUASAR STUB: generated no-op stand-in; the original lives in src/parked (render rewrite pending).

public class RenderStates {

    private static Logger LOGGER;
    public static int frameIndex;
    public static ResourceKey<Level> originalPlayerDimension;
    public static Vec3 originalPlayerPos;
    public static Vec3 originalPlayerLastTickPos;
    public static GameType originalGameMode;
    public static AABB originalPlayerBoundingBox;
    private static float partialTick;
    public static Set<ResourceKey<Level>> renderedDimensions;
    public static List<List<WeakReference<Portal>>> lastPortalRenderInfos;
    public static List<List<WeakReference<Portal>>> portalRenderInfos;
    public static int portalsRenderedThisFrame;
    public static Vec3 lastCameraPos;
    public static Vec3 cameraPosDelta;
    public static boolean shouldForceDisableCull;
    public static long renderStartNanoTime;
    public static double viewBobFactor;
    public static Matrix4f basicProjectionMatrix;
    public static Camera originalCamera;
    public static String debugText;
    public static boolean isLaggy;
    public static boolean isRenderingEntities;
    public static boolean renderedScalingPortal;
    public static boolean isRenderingPortalWeather;
    /**
     * Quasar MC: the state half of the original (positions, dimension, game mode, partial tick). The rendering
     * half (fog context, view bobbing, projection, laggy detection, queries) stays parked with the renderer.
     */
    public static void updatePreRenderInfo(
        float newPartialTick
    ) {
        ClientWorldLoader.initializeIfNeeded();
        Entity cameraEntity = Minecraft.getInstance().getCameraEntity();
        if (cameraEntity == null) {
            return;
        }
        originalPlayerDimension = cameraEntity.level().dimension();
        originalPlayerPos = cameraEntity.position();
        originalPlayerLastTickPos = McHelper.lastTickPosOf(cameraEntity);
        PlayerInfo entry = CHelper.getClientPlayerListEntry();
        originalGameMode = entry != null ? entry.getGameMode() : GameType.CREATIVE;
        originalPlayerBoundingBox = cameraEntity.getBoundingBox();
        partialTick = newPartialTick;
        renderStartNanoTime = System.nanoTime();
        debugText = "";
    }

    private static void updateIsLaggy() {}

    private static void updateViewBobbingFactor(Entity cameraEntity) {}

    public static double getViewBobbingOffsetMultiplier() { return 0D; }

    private static void setViewBobFactor(double arg) {}

    public static void onTotalRenderEnd() {}

    public static int getRenderedPortalNum() { return 0; }

    public static boolean isDimensionRendered(ResourceKey<Level> dimensionType) { return false; }

    public static boolean shouldRenderParticle(Particle particle) { return false; }

    public static void setPartialTick(float partialTick_) { partialTick = partialTick_; }

    public static float getPartialTick() { return partialTick; }

    public static List<String> collectDebugText() { return null; }

}

