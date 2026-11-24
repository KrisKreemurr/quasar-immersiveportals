// QUASAR STUB: real code parked in src/parked (rendering is replaced for Quasar MC).
package qouteall.imm_ptl.core.render.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import org.joml.Matrix4f;
import qouteall.imm_ptl.core.portal.Portal;

// QUASAR STUB: generated no-op stand-in; the original lives in src/parked (render rewrite pending).

public class RendererDummy extends PortalRenderer {

    public boolean replaceFrameBufferClearing() { return false; }

    public void prepareRendering() {}

    public void onBeforeTranslucentRendering(Matrix4f modelView) {}

    public void onAfterTranslucentRendering(Matrix4f modelView) {}

    public void onHandRenderingEnded() {}

    public void finishRendering() {}

    protected void doRenderPortal(
        Portal portal,
        PoseStack matrixStack
    ) {}

    public void renderPortalInEntityRenderer(Portal portal) {}

}

