// QUASAR STUB: real code parked in src/parked (rendering is replaced for Quasar MC).
package qouteall.imm_ptl.core.render.renderer;

import com.mojang.blaze3d.systems.RenderSystem;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.Event;
import net.neoforged.neoforge.common.NeoForge;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import qouteall.imm_ptl.core.CHelper;
import qouteall.imm_ptl.core.ClientWorldLoader;
import qouteall.imm_ptl.core.IPCGlobal;
import qouteall.imm_ptl.core.IPGlobal;
import qouteall.imm_ptl.core.compat.IPModInfoChecking;
import qouteall.imm_ptl.core.portal.Mirror;
import qouteall.imm_ptl.core.portal.Portal;
import qouteall.imm_ptl.core.portal.global_portals.GlobalPortalStorage;
import qouteall.imm_ptl.core.render.MyGameRenderer;
import qouteall.imm_ptl.core.render.MyRenderHelper;
import qouteall.imm_ptl.core.render.TransformationManager;
import qouteall.imm_ptl.core.render.context_management.PortalRendering;
import qouteall.imm_ptl.core.render.context_management.RenderStates;
import qouteall.imm_ptl.core.render.context_management.WorldRenderInfo;
import qouteall.q_misc_util.Helper;
import java.util.Comparator;
import java.util.List;
import java.util.function.Supplier;

// QUASAR STUB: generated no-op stand-in; the original lives in src/parked (render rewrite pending).

public abstract class PortalRenderer {

    public static class PortalRenderingPredicateEvent extends Event {

        public Portal portal;
        boolean canRender;
        public PortalRenderingPredicateEvent(Portal portal) {}

        public void setCanRender(boolean canRender) {}

        public boolean canRender() { return false; }

        protected PortalRenderingPredicateEvent() {}

    }

    public static Minecraft client;
    public abstract void onBeforeTranslucentRendering(Matrix4f modelView);

    public abstract void onAfterTranslucentRendering(Matrix4f modelView);

    public abstract void onHandRenderingEnded();

    public void onBeforeHandRendering(Matrix4f modelView) {}

    public abstract void prepareRendering();

    public abstract void finishRendering();

    public abstract void renderPortalInEntityRenderer(Portal portal);

    public abstract boolean replaceFrameBufferClearing();

    protected List<Portal> getPortalsToRender(Matrix4f modelView) { return null; }

    private static boolean shouldSkipRenderingPortal(Portal portal, Supplier<Frustum> frustumSupplier) { return false; }

    public static double getRenderRange() { return 0D; }

    protected final void renderPortalContent(
        Portal portal
    ) {}

    private static int getPortalRenderDistance(Portal portal) { return 0; }

    public void invokeWorldRendering(
        WorldRenderInfo worldRenderInfo
    ) {}

    @Nullable
    public static Matrix4f getPortalTransformation(Portal portal) { return null; }

    @Nullable
    public static Matrix4f getPortalRotationMatrix(Portal portal) { return null; }

    @Nullable
    public static Matrix4f combineNullable(@Nullable Matrix4f a, @Nullable Matrix4f b) { return null; }

    @Nullable
    public static Matrix4f getPortalScaleMatrix(Portal portal) { return null; }

    public static boolean shouldApplyScaleToModelView(Portal portal) { return false; }

    public void onBeginIrisTranslucentRendering(Matrix4f modelView) {}

    private static boolean fabulousWarned;
    public static void switchToCorrectRenderer() {}

    private static void switchRenderer(PortalRenderer renderer) {}

}

