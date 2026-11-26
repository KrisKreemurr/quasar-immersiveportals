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
    
    /**
     * While set, {@code GameRenderer.lightmap()} returns this (mixin): entities and block entities of a background
     * world in a cross-dimension portal view are lit with that world's lightmap, like its terrain, instead of the
     * player's world's (the Nether's mobs and chests came out near black with the Overworld's).
     */
    private static volatile GpuTextureView lightmapOverride;
    
    public static GpuTextureView lightmapOverride() {
        return lightmapOverride;
    }
    
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
        draw(encoder, virtual, color, depth, partialTick, Minecraft.getInstance().level,
            Minecraft.getInstance().particleEngine);
    }
    
    /**
     * Draws {@code level}'s entities and block entities as seen from {@code virtual}. For a cross-dimension portal
     * {@code level} is the background world of the destination and {@code particleEngine} its own engine
     * ({@link PortalParticles}); null draws no particles.
     */
    static void draw(CommandEncoder encoder, CameraRenderState virtual, GpuTextureView color, GpuTextureView depth,
                     float partialTick, ClientLevel level, net.minecraft.client.particle.ParticleEngine particleEngine,
                     GpuTextureView lightmap) {
        draw(encoder, virtual, color, depth, partialTick, level, particleEngine, lightmap, null);
    }

    /**
     * As above; {@code through} (a cross-dimension portal) also draws the player's-world entities standing in that
     * portal's doorway, moved through it: the part past the portal plane shows on the other side (the portal camera's
     * oblique near plane cuts off the part still on this side).
     */
    static void draw(CommandEncoder encoder, CameraRenderState virtual, GpuTextureView color, GpuTextureView depth,
                     float partialTick, ClientLevel level, net.minecraft.client.particle.ParticleEngine particleEngine,
                     GpuTextureView lightmap, qouteall.imm_ptl.core.portal.Portal through) {
        lightmapOverride = lightmap;
        visitorsThrough = through;
        try {
            draw(encoder, virtual, color, depth, partialTick, level, particleEngine);
        }
        finally {
            lightmapOverride = null;
            visitorsThrough = null;
        }
    }

    private static qouteall.imm_ptl.core.portal.Portal visitorsThrough;

    /** cross-dimension portals near the camera (set each frame by PortalViewRenderer) */
    static volatile java.util.List<qouteall.imm_ptl.core.portal.Portal> crossDimensionPortals = java.util.List.of();

    /**
     * Main view (mixin on EntityRenderDispatcher.shouldRender): an entity standing in a cross-dimension doorway, seen
     * from behind the face it is walking into, is not drawn. From that side the part past the plane is in the other
     * world (not here), and the rest is behind the portal surface, which shows the other world - so none of it is
     * visible. From the front it is drawn as usual and its other half shows in that portal's view.
     */
    public static boolean hiddenInMainView(Entity e, double camX, double camY, double camZ) {
        java.util.List<qouteall.imm_ptl.core.portal.Portal> list = crossDimensionPortals;
        if (list.isEmpty() || e instanceof qouteall.imm_ptl.core.portal.Portal) return false;
        Vec3 cam = null;
        for (qouteall.imm_ptl.core.portal.Portal portal : list) {
            if (portal.isRemoved() || portal.getRotation() != null || Math.abs(portal.getScale() - 1.0) > 1e-6) continue;
            if (!portal.getBoundingBox().inflate(2.0).intersects(e.getBoundingBox())) continue;
            net.minecraft.world.phys.AABB box = e.getBoundingBox();
            Vec3 c = box.getCenter();
            double reach = Math.max(box.getXsize(), Math.max(box.getYsize(), box.getZsize())) * 0.5 + 0.1;
            if (portal.getDistanceToNearestPointInPortal(c) > reach || !portal.isInFrontOfPortal(c)) continue;
            if (cam == null) cam = new Vec3(camX, camY, camZ);
            // third-person camera through this portal: it looks back at you through it, so you stay visible
            if (!portal.isInFrontOfPortal(cam) && !PortalViewRenderer.cameraThrough(portal, cam)) return true;
        }
        return false;
    }

    /** entities of the player's world in {@code portal}'s doorway, submitted at their place on the other side */
    private static int submitVisitors(Minecraft mc, EntityRenderDispatcher erd, ClientLevel there,
                                      qouteall.imm_ptl.core.portal.Portal portal,
                                      CameraRenderState virtual, Vec3 cam, float partialTick, PoseStack pose,
                                      SubmitNodeStorage storage) {
        ClientLevel here = mc.level;
        if (here == null || portal.getRotation() != null || Math.abs(portal.getScale() - 1.0) > 1e-6) return 0;
        // translation-only portal (rotation and scale ruled out above)
        Vec3 offset = portal.transformPoint(portal.getOriginPos()).subtract(portal.getOriginPos());
        net.minecraft.world.phys.AABB near = portal.getBoundingBox().inflate(2.0);
        int n = 0;
        for (Entity e : here.getEntities((Entity) null, near, x -> !(x instanceof qouteall.imm_ptl.core.portal.Portal))) {
            if (e.isRemoved()) continue;
            // you in first person: no body to show (the camera is inside it)
            if (e == mc.getCameraEntity() && mc.options.getCameraType().isFirstPerson()) continue;
            net.minecraft.world.phys.AABB box = e.getBoundingBox();
            Vec3 c = box.getCenter();
            double reach = Math.max(box.getXsize(), Math.max(box.getYsize(), box.getZsize())) * 0.5 + 0.1;
            // only what is actually in the doorway: within its own size of the portal rectangle
            if (portal.getDistanceToNearestPointInPortal(c) > reach) continue;
            // a Nether portal is two portals back to back: only the one the entity is walking into from its front
            // carries the part past the plane (from behind, that part belongs to the back-facing portal)
            if (!portal.isInFrontOfPortal(c)) continue;
            // its own render state (not the shared one): its light is the other world's
            EntityRenderState state = extract(mc, erd, e, partialTick);
            // lit where that half is: the destination world's light there (the lightmap in use is the destination's;
            // the entity's own sky light from the Overworld came out near black with the Nether's lightmap)
            net.minecraft.core.BlockPos lp = net.minecraft.core.BlockPos.containing(c.add(offset));
            int block = there.getBrightness(net.minecraft.world.level.LightLayer.BLOCK, lp);
            int sky = there.getBrightness(net.minecraft.world.level.LightLayer.SKY, lp);
            if (e.isOnFire()) block = 15;
            state.lightCoords = (block << 4) | (sky << 20);
            erd.submit(state, virtual, state.x + offset.x - cam.x, state.y + offset.y - cam.y,
                state.z + offset.z - cam.z, pose, storage);
            n++;
        }
        return n;
    }
    
    static void draw(CommandEncoder encoder, CameraRenderState virtual, GpuTextureView color, GpuTextureView depth,
                     float partialTick, ClientLevel level, net.minecraft.client.particle.ParticleEngine particleEngine) {
        if (failed) return;
        Minecraft mc = Minecraft.getInstance();
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
                // the renderer's own frustum check, not the dispatcher's: occlusion-culling mods (Entity Culling)
                // hook the dispatcher and answer for the player's eye in the player's world, which is wrong for a
                // portal camera (mobs blinked in portal views as their culled flag flipped)
                if (!shouldRenderFromPortal(erd, e, virtual, cam, partialTick)) continue;
                EntityRenderState state = frameStates.get(e);
                if (state == null) {
                    state = extract(mc, erd, e, partialTick);
                    frameStates.put(e, state);
                }
                erd.submit(state, virtual, state.x - cam.x, state.y - cam.y, state.z - cam.z, pose, storage);
                n++;
            }
            if (visitorsThrough != null && level != mc.level) {
                n += submitVisitors(mc, erd, level, visitorsThrough, virtual, cam, partialTick, pose, storage);
            }
            n += submitBlockEntities(mc, level, virtual, cam, maxDist2, partialTick, pose, storage);
            lastCount += n;
            boolean hasParticles = false;
            if (particleEngine != null && PortalSettings.particles) {
                if (particleCamera == null) particleCamera = new PortalParticleCamera();
                particleCamera.setTo(virtual);
                particleState.reset();
                particleEngine.extract(particleState, virtual.cullFrustum, particleCamera, partialTick);
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

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static boolean shouldRenderFromPortal(EntityRenderDispatcher erd, Entity e, CameraRenderState virtual,
                                                  Vec3 cam, float partialTick) {
        net.minecraft.client.renderer.entity.EntityRenderer renderer = erd.getRenderer(e);
        return renderer != null && renderer.shouldRender(e, virtual.cullFrustum, cam.x, cam.y, cam.z, partialTick);
    }
    
    /**
     * Chests, signs, banners, beds, skulls and other block entities within the portal entity distance of the portal
     * camera: extracted with vanilla's dispatcher (its camera position moved to the portal camera for the distance
     * checks, then put back) and submitted like vanilla's submitBlockEntities, relative to the portal camera.
     */
    private static int submitBlockEntities(Minecraft mc, ClientLevel level, CameraRenderState virtual, Vec3 cam,
                                           double maxDist2, float partialTick, PoseStack pose, SubmitNodeStorage storage) {
        if (!PortalSettings.blockEntities) return 0;
        net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher bed =
            mc.levelRenderer.blockEntityRenderDispatcher();
        int radius = Math.max(1, PortalSettings.entityDistance);
        int ccx = net.minecraft.util.Mth.floor(cam.x) >> 4, ccz = net.minecraft.util.Mth.floor(cam.z) >> 4;
        int n = 0;
        bed.prepare(cam);
        try {
            for (int cx = ccx - radius; cx <= ccx + radius; cx++) {
                for (int cz = ccz - radius; cz <= ccz + radius; cz++) {
                    net.minecraft.world.level.chunk.LevelChunk chunk = level.getChunkSource().getChunk(cx, cz, false);
                    if (chunk == null || chunk.getBlockEntities().isEmpty()) continue;
                    for (net.minecraft.world.level.block.entity.BlockEntity be : chunk.getBlockEntities().values()) {
                        net.minecraft.core.BlockPos bp = be.getBlockPos();
                        if (cam.distanceToSqr(bp.getX() + 0.5, bp.getY() + 0.5, bp.getZ() + 0.5) > maxDist2) continue;
                        var renderer = bed.getRenderer(be);
                        if (renderer == null) continue;
                        net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState state =
                            bed.tryExtractRenderState(be, partialTick, null, renderer.shouldRenderOffScreen(),
                                virtual.cullFrustum);
                        if (state == null) continue;
                        pose.pushPose();
                        pose.translate(bp.getX() - cam.x, bp.getY() - cam.y, bp.getZ() - cam.z);
                        bed.submit(state, pose, storage, virtual);
                        pose.popPose();
                        n++;
                    }
                }
            }
        }
        finally {
            bed.prepare(mc.gameRenderer.mainCamera().position());
        }
        return n;
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
