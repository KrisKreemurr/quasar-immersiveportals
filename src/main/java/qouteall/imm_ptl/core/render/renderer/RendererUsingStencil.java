// QUASAR STUB: real code parked in src/parked (rendering is replaced for Quasar MC).
package qouteall.imm_ptl.core.render.renderer;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;
import qouteall.imm_ptl.core.CHelper;
import qouteall.imm_ptl.core.compat.IPPortingLibCompat;
import qouteall.imm_ptl.core.portal.Portal;
import qouteall.imm_ptl.core.portal.PortalRenderInfo;
import qouteall.imm_ptl.core.render.FrontClipping;
import qouteall.imm_ptl.core.render.MyRenderHelper;
import qouteall.imm_ptl.core.render.ViewAreaRenderer;
import qouteall.imm_ptl.core.render.context_management.FogRendererContext;
import qouteall.imm_ptl.core.render.context_management.PortalRendering;
import qouteall.imm_ptl.core.render.context_management.WorldRenderInfo;
import java.util.List;
import static org.lwjgl.opengl.GL11.*;

// QUASAR STUB: generated no-op stand-in; the original lives in src/parked (render rewrite pending).

public class RendererUsingStencil extends PortalRenderer {

    public boolean replaceFrameBufferClearing() { return false; }

    public void onBeforeTranslucentRendering(Matrix4f modelView) {}

    protected void doPortalRendering(Matrix4f modelView) {}

    protected void renderPortals(Matrix4f modelView) {}

    public void onAfterTranslucentRendering(Matrix4f modelView) {}

    public void onHandRenderingEnded() {}

    public void prepareRendering() {}

    public void finishRendering() {}

    private void myFinishRendering() {}

    protected void doRenderPortal(
        Portal portal,
        Matrix4f modelView
    ) {}

    public void renderPortalInEntityRenderer(Portal portal) {}

    private void renderPortalViewAreaToStencil(
        Portal portal, Matrix4f modelView
    ) {}

    private void clearDepthOfThePortalViewArea(
        Portal portal
    ) {}

    protected void restoreDepthOfPortalViewArea(
        Portal portal, Matrix4f modelView,
        int portalStencilValue
    ) {}

    public static void clampStencilValue(
        int maximumValue
    ) {}

    private void setStencilStateForWorldRendering() {}

    public static void setStencilLimitation(int stencilValue) {}

    public static boolean shouldSkipRenderingInsideFuseViewPortal(Portal portal) { return false; }

}

