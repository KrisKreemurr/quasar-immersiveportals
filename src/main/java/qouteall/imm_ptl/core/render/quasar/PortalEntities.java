package qouteall.imm_ptl.core.render.quasar;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.renderpearl.api.commands.CommandEncoder;
import com.mojang.renderpearl.api.commands.RenderPass;
import com.mojang.renderpearl.api.textures.GpuTextureView;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.RenderBuffers;
import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.feature.FeatureRenderDispatcher;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4fStack;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalDouble;

/**
 * Quasar MC: mobs, players (you, in mirrors) and items in portal views.
 *
 * <ul>
 * <li>Entities within the portal entity distance of the portal camera and inside its culling frustum are
 *     extracted with vanilla's {@code EntityRenderDispatcher.extractEntity}. A render state is built at most
 *     once per frame per entity and shared by every portal (30 mirrors showing you = one extraction).</li>
 * <li>They are submitted relative to the portal camera into our own {@link SubmitNodeStorage} and drawn by our
 *     own {@link FeatureRenderDispatcher} with its own {@link RenderBuffers}: the main view's dispatcher keeps one
 *     prepared frame that is still open while portals render, so it must not be reused.</li>
 * <li>Drawn into the portal's image after its solid terrain (vanilla's order) with the portal camera's rotation;
 *     the projection and fog are already the portal's (set by {@link PortalViewRenderer}).</li>
 * </ul>
 */
public final class PortalEntities {
    private static FeatureRenderDispatcher dispatcher;
    private static RenderBuffers buffers;
    private static boolean failed;
    private static boolean usedThisFrame;
    private static final Map<Entity, EntityRenderState> frameStates = new IdentityHashMap<>();
    static int lastCount;
    
    /** a vanilla Camera placed at the portal camera: particles are made camera-relative and billboarded from it */
    private static final class PortalParticleCamera extends net.minecraft.client.Camera {
        void setTo(CameraRenderState virtual) {
            setPosition(virtual.pos);
            // world -> view rotation, without a mirror's left-right flip (billboards only need to face the camera)
            org.joml.Matrix3f m = new org.joml.Matrix3f();
            virtual.viewRotationMatrix.get3x3(m);
            if (m.determinant() < 0f) {
                m = new org.joml.Matrix3f().scaling(-1f, 1f, 1f).mul(m);
            }
            rotation().setFromNormalized(m).conjugate();
        }
    }
    private static PortalParticleCamera particleCamera;
    private static final net.minecraft.client.renderer.state.level.ParticlesRenderState particleState =
        new net.minecraft.client.renderer.state.level.ParticlesRenderState();
    
    private PortalEntities() {}
    
    /** called once per frame before any portal draws */
    static void beginFrame() {
        frameStates.clear();
        lastCount = 0;
    }
    
    /** called once per frame after all portals drew */
    static void endFrame() {
        if (usedThisFrame && buffers != null) {
            buffers.endFrame();
        }
        usedThisFrame = false;
        frameStates.clear();
    }
    
    static void draw(CommandEncoder encoder, CameraRenderState virtual, GpuTextureView color, GpuTextureView depth,
                     float partialTick) {
        if (failed) return;
        Minecraft mc = Minecraft.getInstance();
        ClientLevel level = mc.level;
        if (level == null) return;
        try {
            if (dispatcher == null) {
                buffers = new RenderBuffers(1);
                dispatcher = new FeatureRenderDispatcher(buffers, mc.getModelManager(), mc.getAtlasManager(),
                    mc.font, mc.gameRenderer.gameRenderState());
            }
            EntityRenderDispatcher erd = mc.getEntityRenderDispatcher();
            Vec3 cam = virtual.pos;
            double maxDist = PortalSettings.entityDistance * 16.0;
            double maxDist2 = maxDist * maxDist;
            SubmitNodeStorage storage = new SubmitNodeStorage();
            PoseStack pose = new PoseStack();
            int n = 0;
            for (Entity e : level.entitiesForRendering()) {
                if (e.isRemoved() || e.distanceToSqr(cam) > maxDist2) continue;
                if (!erd.shouldRender(e, virtual.cullFrustum, cam.x, cam.y, cam.z, partialTick)) continue;
                EntityRenderState state = frameStates.get(e);
                if (state == null) {
                    state = extract(mc, erd, e, partialTick);
                    frameStates.put(e, state);
                }
                erd.submit(state, virtual, state.x - cam.x, state.y - cam.y, state.z - cam.z, pose, storage);
                n++;
            }
            lastCount += n;
            boolean hasParticles = false;
            if (PortalSettings.particles) {
                if (particleCamera == null) particleCamera = new PortalParticleCamera();
                particleCamera.setTo(virtual);
                particleState.reset();
                mc.particleEngine.extract(particleState, virtual.cullFrustum, particleCamera, partialTick);
                if (!particleState.particles.isEmpty()) {
                    particleState.submit(storage, virtual);
                    hasParticles = true;
                }
            }
            if (n == 0 && !hasParticles) return;
            usedThisFrame = true;
            
            Matrix4fStack modelView = RenderSystem.getModelViewStack();
            modelView.pushMatrix();
            try {
                modelView.set(virtual.viewRotationMatrix);
                FeatureRenderDispatcher.PreparedFrame frame = dispatcher.prepareFrame(storage);
                try (RenderPass pass = encoder.createRenderPass(() -> "Quasar portal entities", color, Optional.empty(),
                    depth, OptionalDouble.empty())) {
                    RenderSystem.bindDefaultUniforms(pass);
                    frame.executeSolid(pass);
                    frame.executeTranslucent(pass);
                    // particles (and other effects vanilla draws after translucent terrain) are their own phase
                    frame.executeTranslucentAfterTerrain(pass);
                }
                finally {
                    frame.close();
                }
            }
            finally {
                modelView.popMatrix();
            }
        }
        catch (Throwable t) {
            failed = true;
            org.slf4j.LoggerFactory.getLogger("Quasar Portals").error("[Quasar Portals] entities through portals failed; turned off", t);
        }
    }

    /**
     * Extracts an entity's render state for portal views. The camera entity is extracted as if
     * the camera were in third person: first-person body mods (FirstPerson) hide the helmet and
     * held items while the camera entity is extracted in first person, which is right for the
     * main view but wrong for a mirror or portal looking back at you.
     */
    private static EntityRenderState extract(Minecraft mc, EntityRenderDispatcher erd, Entity e, float partialTick) {
        CameraType type = mc.options.getCameraType();
        if (e != mc.getCameraEntity() || !type.isFirstPerson()) return erd.extractEntity(e, partialTick);
        mc.options.setCameraType(CameraType.THIRD_PERSON_BACK);
        try {
            return erd.extractEntity(e, partialTick);
        } finally {
            mc.options.setCameraType(type);
        }
    }
}
