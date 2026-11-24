package qouteall.imm_ptl.core.render.quasar;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.renderpearl.api.buffers.GpuBuffer;
import com.mojang.renderpearl.api.buffers.GpuBufferSlice;
import com.mojang.renderpearl.api.commands.CommandEncoder;
import com.mojang.renderpearl.api.commands.RenderPass;
import com.mojang.renderpearl.api.pipeline.BindGroupLayout;
import com.mojang.renderpearl.api.pipeline.ColorTargetState;
import com.mojang.renderpearl.api.pipeline.CompareOp;
import com.mojang.renderpearl.api.pipeline.DepthStencilState;
import com.mojang.renderpearl.api.pipeline.PolygonMode;
import com.mojang.renderpearl.api.pipeline.PrimitiveTopology;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import com.mojang.renderpearl.api.pipeline.UniformType;
import com.mojang.renderpearl.api.textures.AddressMode;
import com.mojang.renderpearl.api.textures.FilterMode;
import com.mojang.renderpearl.api.GpuFormat;
import com.mojang.renderpearl.api.textures.GpuSampler;
import com.mojang.renderpearl.api.textures.GpuTextureView;
import com.mojang.renderpearl.api.vertex.VertexFormat;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.lwjgl.system.MemoryStack;

import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.util.Optional;
import java.util.OptionalDouble;

/**
 * Quasar MC: far terrain through portals and mirrors, from Quasar Horizons' portal panoramas.
 *
 * <p>For each of the nearest live portals, Quasar Horizons keeps a cube of the LOD terrain beyond the portal's live
 * terrain, centred on the portal camera and rebuilt one face per frame only when the camera moved or it got old
 * ({@code QuasarPanorama.requestPortal / portalView}). Here it is painted into the portal's image before the live
 * terrain (which then draws in front of it), ray-marched with the same parallax correction as the main panorama.
 * Without Quasar Horizons, or before a cube is ready, nothing happens and the portal keeps its fog edge.</p>
 */
public final class PortalFarView {
    /** how many portals get far terrain (each costs ~25 MB of video memory at 512 px faces) */
    static final int MAX_PORTALS = 2;
    static final int FACE_SIZE = 512;
    
    private static Method requestMethod;
    private static Method viewMethod;
    private static boolean looked, failed;
    private static RenderPipeline pipeline;
    private static GpuSampler sampler;
    
    private PortalFarView() {}
    
    private static boolean available() {
        if (!looked) {
            looked = true;
            try {
                Class<?> pano = Class.forName("com.seibel.distanthorizons.common.quasar.QuasarPanorama");
                requestMethod = pano.getMethod("requestPortal", int.class, double.class, double.class, double.class,
                    float.class, int.class);
                viewMethod = pano.getMethod("portalView", int.class);
            }
            catch (Throwable t) {
                requestMethod = null; // no Quasar Horizons (or an older one): no far terrain through portals
            }
        }
        return requestMethod != null && !failed;
    }
    
    /** the distance (blocks) where the panorama takes over from the portal's live terrain */
    static float startDistance(int portalChunks) {
        return Math.max(32f, portalChunks * 16f - 8f);
    }
    
    /**
     * Asks for this portal's panorama and paints it if it is ready. Returns true if far terrain was painted (the caller
     * then keeps vanilla's fog instead of fading the live terrain out at the portal distance).
     */
    static boolean draw(CommandEncoder encoder, int portalId, CameraRenderState virtual, CameraRenderState main,
                        GpuTextureView color, GpuTextureView depth, int portalChunks) {
        if (portalChunks <= 0 || !available()) return false;
        try {
            Vec3 cam = virtual.pos;
            requestMethod.invoke(null, portalId, cam.x, cam.y, cam.z, startDistance(portalChunks), FACE_SIZE);
            Object[] v = (Object[]) viewMethod.invoke(null, portalId);
            if (v == null) return false;
            GpuTextureView[] colors = (GpuTextureView[]) v[0];
            GpuTextureView[] depths = (GpuTextureView[]) v[1];
            float[] faceVp = (float[]) v[2];
            float[] faceInv = (float[]) v[3];
            double[] center = (double[]) v[4];
            float farDepth = (Float) v[5];
            boolean zeroToOne = (Boolean) v[6];
            for (int f = 0; f < 6; f++) {
                if (colors[f] == null || depths[f] == null) return false;
            }
            ensurePipeline();
            
            Matrix4f invRay = new Matrix4f(main.projectionMatrix).mul(virtual.viewRotationMatrix).invert();
            Matrix4f portalVp = new Matrix4f(virtual.projectionMatrix).mul(virtual.viewRotationMatrix);
            try (MemoryStack stack = MemoryStack.stackPush()) {
                ByteBuffer ub = stack.calloc(928);
                for (int i = 0; i < 96; i++) ub.putFloat(i * 4, faceVp[i]);
                for (int i = 0; i < 96; i++) ub.putFloat(384 + i * 4, faceInv[i]);
                invRay.get(768, ub);
                portalVp.get(832, ub);
                ub.putFloat(896, (float) (center[0] - cam.x));
                ub.putFloat(900, (float) (center[1] - cam.y));
                ub.putFloat(904, (float) (center[2] - cam.z));
                ub.putFloat(912, farDepth);
                ub.putInt(916, zeroToOne ? 1 : 0);
                ByteBuffer vb = stack.malloc(3 * 3 * 4);
                vb.putFloat(-1f).putFloat(-1f).putFloat(0f);
                vb.putFloat(3f).putFloat(-1f).putFloat(0f);
                vb.putFloat(-1f).putFloat(3f).putFloat(0f);
                vb.flip();
                GpuBufferSlice uniforms = encoder.transientMemory().uploadGpu(ub, 256, GpuBuffer.USAGE_UNIFORM);
                GpuBufferSlice vertices = encoder.transientMemory().uploadGpu(vb, 16, GpuBuffer.USAGE_VERTEX);
                try (RenderPass pass = encoder.createRenderPass(() -> "Quasar portal far terrain", color, Optional.empty(),
                    depth, OptionalDouble.empty())) {
                    pass.setPipeline(RenderSystem.getCompiledPipeline(pipeline));
                    for (int f = 0; f < 6; f++) {
                        pass.setUniform("C" + f, colors[f], sampler);
                        pass.setUniform("D" + f, depths[f], sampler);
                    }
                    pass.setUniform("PanoInfo", uniforms);
                    pass.setVertexBuffer(0, vertices);
                    pass.draw(3, 1, 0, 0);
                }
            }
            return true;
        }
        catch (Throwable t) {
            failed = true;
            org.slf4j.LoggerFactory.getLogger("Quasar Portals").error("[Quasar Portals] far terrain through portals failed; turned off", t);
            return false;
        }
    }
    
    private static void ensurePipeline() {
        if (pipeline != null) return;
        BindGroupLayout.Builder layout = BindGroupLayout.builder();
        for (int f = 0; f < 6; f++) layout.withUniform("C" + f, UniformType.COMBINED_IMAGE_SAMPLER);
        for (int f = 0; f < 6; f++) layout.withUniform("D" + f, UniformType.COMBINED_IMAGE_SAMPLER);
        layout.withUniform("PanoInfo", UniformType.UNIFORM_BUFFER);
        pipeline = RenderPipeline.builder()
            .withLocation(Identifier.parse("immersive_portals:pipeline/portal_panorama"))
            .withVertexShader(Identifier.parse("immersive_portals:portal_panorama"))
            .withFragmentShader(Identifier.parse("immersive_portals:portal_panorama"))
            .withBindGroupLayout(layout.build())
            .withCull(false)
            .withPolygonMode(PolygonMode.FILL)
            .withDepthStencilState(new DepthStencilState(CompareOp.GREATER_THAN_OR_EQUAL, true))
            .withColorTargetState(new ColorTargetState(Optional.empty(), GpuFormat.RGBA8_UNORM, ColorTargetState.WRITE_ALL))
            .withVertexBinding(0, VertexFormat.builder(0).addAttribute("Position", GpuFormat.RGB32_FLOAT).build())
            .withPrimitiveTopology(PrimitiveTopology.TRIANGLES)
            .build();
        sampler = RenderSystem.getDevice().createSampler(AddressMode.CLAMP_TO_EDGE, AddressMode.CLAMP_TO_EDGE,
            FilterMode.NEAREST, FilterMode.NEAREST, 1, OptionalDouble.empty());
    }
}
