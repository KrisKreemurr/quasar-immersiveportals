// QUASAR STUB: real code parked in src/parked (rendering is replaced for Quasar MC).
package qouteall.imm_ptl.core.render.renderer;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;
import qouteall.imm_ptl.core.CHelper;
import qouteall.imm_ptl.core.compat.IPPortingLibCompat;
import qouteall.imm_ptl.core.ducks.IEMinecraftClient;
import qouteall.imm_ptl.core.portal.Portal;
import qouteall.imm_ptl.core.render.FrontClipping;
import qouteall.imm_ptl.core.render.MyRenderHelper;
import qouteall.imm_ptl.core.render.QueryManager;
import qouteall.imm_ptl.core.render.SecondaryFrameBuffer;
import qouteall.imm_ptl.core.render.ViewAreaRenderer;
import qouteall.imm_ptl.core.render.context_management.PortalRendering;
import java.util.List;

// QUASAR STUB: generated no-op stand-in; the original lives in src/parked (render rewrite pending).

public class RendererUsingFrameBuffer extends PortalRenderer {

    SecondaryFrameBuffer secondaryFrameBuffer;
    public void onBeforeTranslucentRendering(Matrix4f modelView) {}

    public void onAfterTranslucentRendering(Matrix4f modelView) {}

    public void onHandRenderingEnded() {}

    public void finishRendering() {}

    public void prepareRendering() {}

    protected void doRenderPortal(
        Portal portal,
        Matrix4f modelView
    ) {}

    public void renderPortalInEntityRenderer(Portal portal) {}

    public boolean replaceFrameBufferClearing() { return false; }

    private boolean testShouldRenderPortal(
        Portal portal,
        Matrix4f modelView
    ) { return false; }

    private void renderSecondBufferIntoMainBuffer(Portal portal, Matrix4f modelView) {}

    protected void renderPortals(Matrix4f modelView) {}

}

