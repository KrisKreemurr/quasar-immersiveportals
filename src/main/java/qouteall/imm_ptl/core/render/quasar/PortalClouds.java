package qouteall.imm_ptl.core.render.quasar;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.renderpearl.api.buffers.GpuBuffer;
import com.mojang.renderpearl.api.buffers.GpuBufferSlice;
import com.mojang.renderpearl.api.commands.CommandEncoder;
import com.mojang.renderpearl.api.commands.RenderPass;
import com.mojang.renderpearl.api.textures.GpuTextureView;
import net.minecraft.client.CloudStatus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.CloudRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import net.minecraft.util.ARGB;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4fStack;
import org.joml.Vector4f;
import qouteall.imm_ptl.core.mixin.client.accessor.IECloudRenderer_Quasar;
import qouteall.imm_ptl.core.mixin.client.accessor.IELevelRenderer_QuasarSky;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Optional;
import java.util.OptionalDouble;

/**
 * Quasar MC: clouds through portals, reusing the main view's cloud mesh (no rebuild, no second mesh).
 *
 * <p>Vanilla builds the cloud mesh around the camera's 12-block cloud cell and passes the camera's offset into
 * that cell in the CloudInfo uniform ({@code CloudRenderer.prepare}: colour, (-fx, height - y, -fz), cell size).
 * Here the same offset is computed for the portal camera against the main mesh's cell, so the mesh lines up from
 * the far side of the portal. It is drawn after the portal's terrain with the portal camera's (oblique) projection,
 * so the depth matches the terrain's.</p>
 *
 * <p>Limits: the mesh only reaches the main view's cloud distance around you, and its side faces are chosen for
 * your height (above / inside / below the clouds), so a portal far away or above the clouds is approximate.</p>
 */
public final class PortalClouds {
    public static volatile GpuBufferSlice overrideInfo;
    private static boolean failed;
    
    private PortalClouds() {}
    
    static void draw(CommandEncoder encoder, CameraRenderState main, CameraRenderState virtual,
                     GpuTextureView color, GpuTextureView depth) {
        if (failed) return;
        Minecraft mc = Minecraft.getInstance();
        CloudStatus status = mc.options.cloudStatus().get();
        if (status == CloudStatus.OFF) return;
        LevelRenderState state = ((IELevelRenderer_QuasarSky) mc.levelRenderer).ip_getLevelRenderState();
        if (state == null || state.customCloudsRenderer != null) return;
        CloudRenderer clouds = mc.levelRenderer.cloudRenderer();
        IECloudRenderer_Quasar acc = (IECloudRenderer_Quasar) clouds;
        CloudRenderer.TextureData tex = acc.ip_texture();
        if (tex == null || acc.ip_quadCount() == 0) return;
        
        // CloudRenderer.prepare's maths, for the portal camera against the main mesh's cell
        Vec3 m = main.pos, v = virtual.pos;
        float drift = (float) (state.gameTime % ((long) tex.width() * 400L)) + state.worldPartialTicks;
        double w = tex.width() * 12.0, h = tex.height() * 12.0;
        double mx = m.x + drift * 0.030000001f;
        double mz = m.z + 3.9600000381469727;
        mx -= Math.floor(mx / w) * w;
        mz -= Math.floor(mz / h) * h;
        float fx = (float) (mx + (v.x - m.x) - acc.ip_prevCellX() * 12.0f);
        float fz = (float) (mz + (v.z - m.z) - acc.ip_prevCellZ() * 12.0f);
        float fy = (float) (state.cloudHeight - v.y);
        
        Vector4f c = ARGB.vector4fFromARGB32(state.cloudColor);
        ByteBuffer b = Scratch.small(48);
        b.putFloat(0, c.x).putFloat(4, c.y).putFloat(8, c.z).putFloat(12, c.w);
        b.putFloat(16, -fx).putFloat(20, fy).putFloat(24, -fz);
        b.putFloat(32, 12f).putFloat(36, 4f).putFloat(40, 12f);
        GpuBufferSlice info = encoder.transientMemory().uploadGpu(b, 256, GpuBuffer.USAGE_UNIFORM);
        
        Matrix4fStack modelView = RenderSystem.getModelViewStack();
        modelView.pushMatrix();
        overrideInfo = info; // the portal camera's projection is already set by PortalViewRenderer
        try {
            modelView.set(virtual.viewRotationMatrix);
            try (RenderPass pass = encoder.createRenderPass(() -> "Quasar portal clouds", color, Optional.empty(),
                depth, OptionalDouble.empty())) {
                clouds.render(status, pass);
            }
        }
        catch (Throwable t) {
            failed = true;
            org.slf4j.LoggerFactory.getLogger("Quasar Portals").error("[Quasar Portals] clouds through portals failed; turned off", t);
        }
        finally {
            overrideInfo = null;
            modelView.popMatrix();
        }
    }
}
