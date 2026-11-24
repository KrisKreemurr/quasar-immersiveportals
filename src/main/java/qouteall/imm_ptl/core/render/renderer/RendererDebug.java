// QUASAR STUB: real code parked in src/parked (rendering is replaced for Quasar MC).
package qouteall.imm_ptl.core.render.renderer;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;
import qouteall.imm_ptl.core.portal.Portal;
import qouteall.imm_ptl.core.render.QueryManager;
import qouteall.imm_ptl.core.render.ViewAreaRenderer;
import qouteall.imm_ptl.core.render.context_management.PortalRendering;
import qouteall.imm_ptl.core.render.context_management.RenderStates;
import java.util.List;

// QUASAR STUB: generated no-op stand-in; the original lives in src/parked (render rewrite pending).

public class RendererDebug extends PortalRenderer {

    public void onBeforeTranslucentRendering(Matrix4f modelView) {}

    public void onAfterTranslucentRendering(Matrix4f modelView) {}

    public void onHandRenderingEnded() {}

    public void prepareRendering() {}

    public void finishRendering() {}

    public void renderPortalInEntityRenderer(Portal portal) {}

    public boolean replaceFrameBufferClearing() { return false; }

    protected void doRenderPortal(Portal portal, Matrix4f modelView) {}

    private boolean testShouldRenderPortal(
        Portal portal,
        Matrix4f modelView
    ) { return false; }

    protected void renderPortals(Matrix4f modelView) {}

}

