package qouteall.imm_ptl.core.render.quasar;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.renderpearl.api.buffers.GpuBuffer;
import com.mojang.renderpearl.api.buffers.GpuBufferSlice;
import com.mojang.renderpearl.api.commands.CommandEncoder;
import com.mojang.renderpearl.api.textures.GpuTextureView;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.fog.FogData;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import net.minecraft.client.renderer.state.level.SkyRenderState;
import net.minecraft.world.level.dimension.DimensionType;
import org.joml.Matrix4fStack;
import org.joml.Vector4f;
import qouteall.imm_ptl.core.mixin.client.accessor.IELevelRenderer_QuasarSky;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Quasar MC: sky and fog for portal views.
 * <ul>
 * <li>Background = the frame's fog colour (what vanilla clears the screen to), so the nether and
 *     night look right even where no sky is drawn.</li>
 * <li>Sky = vanilla's own SkyRenderer with this frame's sky state (sun, moon, stars, sunset, time of day,
 *     weather), drawn into the portal's image with the portal camera's rotation
 *     ({@link qouteall.imm_ptl.core.mixin.client.MixinSkyRenderer_QuasarPortal} swaps the target).</li>
 * <li>Fog = vanilla's fog with the render-distance fog ending at the portal render distance, so the
 *     terrain through a portal fades into the sky instead of stopping at a hard edge.</li>
 * </ul>
 */
public final class PortalSky {
    public static volatile GpuTextureView overrideColor;
    public static volatile GpuTextureView overrideDepth;
    private static boolean skyFailed;
    
    private PortalSky() {}
    
    
    /** the colour to clear a portal view to: this frame's fog colour */
    static Vector4f background(CameraRenderState main) {
        FogData fog = main.fogData;
        if (fog == null || fog.color == null) return new Vector4f(0.53f, 0.71f, 1.0f, 1.0f);
        return new Vector4f(fog.color.x, fog.color.y, fog.color.z, 1.0f);
    }
    
    /** draws vanilla's sky into the portal view's image, seen from the portal camera; returns false if it could not */
    static boolean drawSky(CameraRenderState virtual, GpuTextureView color, GpuTextureView depth) {
        if (skyFailed) return false;
        Minecraft mc = Minecraft.getInstance();
        LevelRenderState state = ((IELevelRenderer_QuasarSky) mc.levelRenderer).ip_getLevelRenderState();
        if (state == null) return false;
        SkyRenderState sky = state.skyRenderState;
        if (sky == null || sky.skybox == null || sky.skybox == DimensionType.Skybox.NONE || sky.skyColor == null) {
            return false; // no sky in this dimension (the fog-colour background is the look there)
        }
        Matrix4fStack modelView = RenderSystem.getModelViewStack();
        modelView.pushMatrix();
        overrideColor = color;
        overrideDepth = depth;
        try {
            modelView.set(virtual.viewRotationMatrix);
            mc.levelRenderer.skyRenderer().render(RenderSystem.getShaderFog(), sky);
            return true;
        }
        catch (Throwable t) {
            skyFailed = true;
            org.slf4j.LoggerFactory.getLogger("Quasar Portals").error("[Quasar Portals] sky through portals failed; using the fog colour", t);
            return false;
        }
        finally {
            overrideColor = null;
            overrideDepth = null;
            modelView.popMatrix();
        }
    }
    
    /**
     * Vanilla's fog buffer for the portal view: the main view's fog with the render-distance fog moved in to
     * the portal render distance (vanilla's own fade length: a tenth of the distance, 4 to 64 blocks).
     * Null = use the main fog unchanged (unlimited portal distance, or no fog data).
     */
    static GpuBufferSlice portalFog(CommandEncoder encoder, CameraRenderState main, int portalChunks) {
        FogData f = main.fogData;
        if (f == null || f.color == null || portalChunks <= 0) return null;
        float end = Math.min(f.renderDistanceEnd, portalChunks * 16f);
        float start = Math.min(f.renderDistanceStart, end - Math.max(4f, Math.min(64f, end / 10f)));
        ByteBuffer b = ByteBuffer.allocateDirect(64).order(ByteOrder.nativeOrder());
        b.putFloat(0, f.color.x).putFloat(4, f.color.y).putFloat(8, f.color.z).putFloat(12, f.color.w);
        b.putFloat(16, f.environmentalStart).putFloat(20, f.environmentalEnd);
        b.putFloat(24, start).putFloat(28, end);
        b.putFloat(32, f.skyEnd).putFloat(36, f.cloudEnd);
        return encoder.transientMemory().uploadGpu(b, 256, GpuBuffer.USAGE_UNIFORM);
    }
}
