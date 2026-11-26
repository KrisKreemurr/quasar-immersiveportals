package qouteall.imm_ptl.core.render.quasar;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.logging.LogUtils;
import com.mojang.renderpearl.api.GpuFormat;
import com.mojang.renderpearl.api.buffers.GpuBuffer;
import com.mojang.renderpearl.api.buffers.GpuBufferSlice;
import com.mojang.renderpearl.api.commands.CommandEncoder;
import com.mojang.renderpearl.api.commands.GpuQueryPool;
import com.mojang.renderpearl.api.commands.RenderPass;
import com.mojang.renderpearl.api.device.GpuDevice;
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
import com.mojang.renderpearl.api.textures.GpuSampler;
import com.mojang.renderpearl.api.textures.GpuTexture;
import com.mojang.renderpearl.api.textures.GpuTextureView;
import com.mojang.renderpearl.api.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.core.SectionPos;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.system.MemoryStack;
import org.slf4j.Logger;
import qouteall.imm_ptl.core.portal.Portal;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.OptionalDouble;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Quasar MC portal rendering, milestone 1: a live view through same-dimension portals.
 *
 * <p>Runs from Quasar Meshelium's after-opaque hook (right after the main opaque terrain, no render pass open). For
 * each of the nearest {@link #MAX_PORTALS} visible portals the player stands in front of:</p>
 * <ol>
 *   <li>a virtual camera is placed on the far side (the portal transform applied to the main camera);</li>
 *   <li>its projection gets an oblique near plane lying on the destination portal, so nothing between the virtual
 *       camera and the destination portal is drawn (Lengyel's oblique clipping, in the reverse-Z convention 26.x
 *       uses);</li>
 *   <li>Meshelium draws the resident opaque terrain for that camera into this portal's color/depth images (no
 *       re-meshing: the same GPU terrain the main view uses);</li>
 *   <li>the portal surface is drawn into the main image with a depth test, sampling the portal image at the same
 *       screen position.</li>
 * </ol>
 * Not yet: translucent terrain, entities, sky/clouds (the background is a flat sky colour), the far panorama,
 * update-rate/resolution scaling, nested portals.
 */
public final class PortalViewRenderer {
    private static final Logger LOGGER = LogUtils.getLogger();

    static final int MAX_PORTALS = 128; // slots allocated on demand (images only for slots actually used)
    /** live settings, tunable with /quasarportals (defaults become config values later) */
    static volatile int liveLimit = 3;
    static volatile int renderDistance = 8;
    /** portal view resolution relative to the screen (setting later; scales with on-screen size in milestone 2) */
    static volatile float resolutionScale = 0.5f;
    /** redraw each portal view at most every Nth frame, re-pasting the last picture in between (setting later) */
    static volatile int maxUpdateEvery = 4;
    /** ...but never fewer than this many redraws a second: slower frame rates redraw more often, 60 FPS every frame */
    static volatile int minUpdatesPerSecond = 60;
    private static long viewFrame;
    private static long lastHookNanos;
    private static double avgFrameSeconds = 1.0 / 60.0;
    private static int updateEvery = 1;
    private static final double MAX_DISTANCE = 64;

    /** portals drawn with a live view last frame (the placeholder outline skips these) */
    static final Set<Integer> drawnLastFrame = ConcurrentHashMap.newKeySet();
    private static final Set<Integer> drawnThisFrame = new java.util.HashSet<>();

    private static Method mainCameraMethod;
    private static Method drawTerrainMethod;
    private static Method lastSectionsMethod;
    private static Method drawTranslucentMethod;
    private static Method lastTranslucentMethod;
    private static Field renderDistanceField;
    private static boolean failed;

    private static final Target[] targets = new Target[MAX_PORTALS];
    private static RenderPipeline compositePipeline;
    private static GpuSampler sampler;

    private PortalViewRenderer() {}

    public static void init() {
        try {
            ClassLoader cl = PortalViewRenderer.class.getClassLoader();
            Class<?> hook = Class.forName("com.deds.meshelium.quasar.QuasarPortalHook", true, cl);
            mainCameraMethod = hook.getMethod("mainCamera");
            drawTerrainMethod = hook.getMethod("drawTerrain", CameraRenderState.class, GpuTextureView.class,
                GpuTextureView.class, int.class, int.class);
            try {
                drawTranslucentMethod = hook.getMethod("drawTranslucent", CameraRenderState.class,
                    GpuTextureView.class, GpuTextureView.class, int.class, int.class);
                lastTranslucentMethod = hook.getMethod("lastTranslucentSections");
            }
            catch (NoSuchMethodException ignored) {
                // older Quasar Meshelium: no water/glass through portals
            }
            try {
                lastSectionsMethod = hook.getMethod("lastSections");
            }
            catch (NoSuchMethodException ignored) {
                // older Quasar Meshelium: the F3 line shows no section count
            }
            try {
                renderDistanceField = hook.getField("portalRenderDistance");
            }
            catch (NoSuchFieldException ignored) {
                // older Quasar Meshelium: no portal render distance
            }
            Field after = hook.getField("afterOpaque");
            after.set(null, (Runnable) PortalViewRenderer::renderAll);
            LOGGER.info("[Quasar Portals] live portal views on (Quasar Meshelium found)");
        }
        catch (Throwable t) {
            LOGGER.info("[Quasar Portals] Quasar Meshelium's portal hook not found; portals use the outline stand-in ({})",
                t.toString());
        }
    }

    private static void renderAll() {
        drawnThisFrame.clear();
        compositedThisFrame.clear();
        lastPixels = 0;
        frameTerrainNs = 0;
        frameSkyNs = 0;
        frameHidden = 0;
        frameEntityNs = 0;
        PortalEntities.beginFrame();
        PortalWorldTerrain.beginFrame();
        drawCrossingStandIn();
        frameSections = 0;
        frameCoverage = 0;
        long t0 = System.nanoTime();
        viewFrame++;
        // adaptive skip: N = how many frames fit in 1/60 s at the current frame rate, capped at maxUpdateEvery
        if (lastHookNanos != 0) {
            double dt = (t0 - lastHookNanos) / 1e9;
            if (dt > 0 && dt < 0.5) {
                avgFrameSeconds += (dt - avgFrameSeconds) * 0.05;
            }
        }
        lastHookNanos = t0;
        updateEvery = Math.max(1, Math.min(maxUpdateEvery,
            (int) Math.floor(1.0 / (Math.max(1, minUpdatesPerSecond) * avgFrameSeconds))));
        try {
            if (!failed) {
                renderPortals();
            }
        }
        catch (Throwable t) {
            failed = true;
            LOGGER.error("[Quasar Portals] portal view rendering failed; falling back to outlines", t);
        }
        PortalEntities.endFrame();
        drawnLastFrame.clear();
        drawnLastFrame.addAll(drawnThisFrame);
        Stats.frame(System.nanoTime() - t0, frameTerrainNs, frameSkyNs, frameEntityNs, PortalEntities.lastCount, frameHidden, drawnThisFrame.size(), frameSections, frameCoverage);
    }

    private static RenderPipeline holdPipeline;

    /**
     * Crossing hold (at the end of the world pass): for a moment after crossing a portal the whole frame is drawn the
     * way the portal showed the new world - its sky, the terrain the portal view had built, its mobs and particles,
     * from your camera - over whatever the main renderer produced while it switches worlds, then faded out once the
     * main renderer has what you are looking at. One consistent picture through the crossing instead of a flash.
     */
    /** within this many blocks of a portal into another world, that world is built out to the full render distance */
    private static final double PREWARM_BLOCKS = 24.0;

    /** the portal pictures pasted this frame (pasted again over the crossing hold, which is drawn after them) */
    private record Composited(Portal portal, Target t, float[] rect, boolean fullScreen, boolean camThrough) {}
    private static final List<Composited> compositedThisFrame = new ArrayList<>();

    /** a portal was within reach of the camera this frame (the frame may be the last before a crossing) */
    private static boolean nearCrossing;
    /** the last world picture taken at a portal (before hand and HUD), shown frozen just after crossing */
    private static GpuTexture freezeTex;
    private static GpuTextureView freezeView;
    private static int freezeW, freezeH;
    private static boolean freezeValid, freezeFailed;
    /** the freeze-frame crossing: off (the user found the live hold looked better); kept for comparison */
    private static final boolean FREEZE = false;
    /** projection * view rotation of the camera when the frozen picture was taken (for turning with the camera) */
    private static final Matrix4f freezeViewProj = new Matrix4f();

    public static void drawCrossingHold() {
        try {
            float alpha = PortalWorldTerrain.holdAlpha();
            if (alpha <= 0f) {
                // not crossing: while at a portal keep a copy of the finished world picture for the next crossing
                boolean near = nearCrossing;
                nearCrossing = false;
                if (near && FREEZE && !freezeFailed) captureFreeze(Minecraft.getInstance().gameRenderer.mainRenderTarget());
                else if (!near) freezeValid = false;
                return;
            }
            nearCrossing = false;
            if (freezeValid) {
                // the frozen last frame before the teleport (the destination as the portal showed it)
                Minecraft mcf = Minecraft.getInstance();
                CameraRenderState mainF = (CameraRenderState) mainCameraMethod.invoke(null);
                RenderTarget mt = mcf.gameRenderer.mainRenderTarget();
                if (mainF != null && mainF.initialized && mt.width == freezeW && mt.height == freezeH
                    && mt.getColorTextureView() != null && mt.getDepthTextureView() != null) {
                    compositeHold(RenderSystem.getDevice().createCommandEncoder(), mainF, freezeView, alpha,
                        mt.getColorTextureView(), mt.getDepthTextureView(), mt.width, mt.height, freezeViewProj);
                    return;
                }
            }
            PortalWorldTerrain hold = PortalWorldTerrain.holdTerrain();
            Minecraft mc = Minecraft.getInstance();
            if (hold == null || mc.level == null || mainCameraMethod == null) return;
            CameraRenderState main = (CameraRenderState) mainCameraMethod.invoke(null);
            if (main == null || !main.initialized || main.pos == null || main.cullFrustum == null) return;
            RenderTarget mainTarget = mc.gameRenderer.mainRenderTarget();
            int w = mainTarget.width, h = mainTarget.height;
            GpuTextureView mainColor = mainTarget.getColorTextureView(), mainDepth = mainTarget.getDepthTextureView();
            if (mainColor == null || mainDepth == null || w <= 0 || h <= 0) return;
            GpuDevice device = RenderSystem.getDevice();
            CommandEncoder encoder = device.createCommandEncoder();
            Target t = target(MAX_PORTALS - 1, device, w, h);
            encoder.clearColorAndDepthTextures(t.color, PortalSky.background(main), t.depth, 0.0);
            PortalSky.drawSky(main, t.colorView, t.depthView);
            if (!hold.drawHold(encoder, main, t.colorView, t.depthView)) return;
            if (PortalSettings.entities) {
                PortalEntities.draw(encoder, main, t.colorView, t.depthView,
                    qouteall.imm_ptl.core.render.context_management.RenderStates.getPartialTick(),
                    mc.level, mc.particleEngine, hold.lastLightmap());
            }
            compositeHold(encoder, main, t.colorView, alpha, mainColor, mainDepth, w, h, null);
            // the hold picture has no portals in it: paste this frame's portal pictures over it again (the portal
            // you just came through would otherwise show the world behind it until the hold ends)
            for (Composited c : compositedThisFrame) {
                if (c.t().valid && c.t().portalId == c.portal().getId()) {
                    composite(encoder, main, c.portal(), c.t(), c.rect(), c.t().mirrored, mainColor, mainDepth, w, h,
                        c.fullScreen(), c.camThrough());
                }
            }
        }
        catch (Throwable t) {
            LOGGER.warn("[Quasar Portals] crossing hold failed: {}", t.toString());
        }
    }

    private static void captureFreeze(RenderTarget mainTarget) {
        try {
            GpuTexture src = mainTarget.getColorTexture();
            int w = mainTarget.width, h = mainTarget.height;
            if (src == null || w <= 0 || h <= 0) return;
            GpuDevice device = RenderSystem.getDevice();
            if (freezeTex == null || freezeW != w || freezeH != h) {
                if (freezeView != null) freezeView.close();
                if (freezeTex != null) freezeTex.close();
                freezeTex = device.createTexture("quasar_crossing_freeze",
                    GpuTexture.USAGE_TEXTURE_BINDING | GpuTexture.USAGE_COPY_DST, src.getFormat(), w, h, 1, 1);
                freezeView = device.createTextureView(freezeTex);
                freezeW = w;
                freezeH = h;
            }
            CameraRenderState cam = (CameraRenderState) mainCameraMethod.invoke(null);
            if (cam == null || !cam.initialized) return;
            device.createCommandEncoder().copyTextureToTexture(src, freezeTex, 0, 0, 0, 0, 0, w, h);
            freezeViewProj.set(cam.projectionMatrix).mul(cam.viewRotationMatrix);
            freezeValid = true;
        }
        catch (Throwable t) {
            freezeFailed = true;
            freezeValid = false;
            LOGGER.warn("[Quasar Portals] crossing freeze-frame unavailable ({}); using the live hold", t.toString());
        }
    }

    private static void compositeHold(CommandEncoder encoder, CameraRenderState main, GpuTextureView picture, float alpha,
                                      GpuTextureView mainColor, GpuTextureView mainDepth, int w, int h,
                                      Matrix4f pictureViewProj) {
        ensurePipeline();
        if (holdPipeline == null) {
            holdPipeline = RenderPipeline.builder()
                .withLocation(Identifier.parse("immersive_portals:pipeline/portal_crossing_hold"))
                .withVertexShader(Identifier.parse("immersive_portals:portal_composite"))
                .withFragmentShader(Identifier.parse("immersive_portals:portal_composite"))
                .withBindGroupLayout(BindGroupLayout.builder()
                    .withUniform("PortalColor", UniformType.COMBINED_IMAGE_SAMPLER)
                    .withUniform("PortalUniforms", UniformType.UNIFORM_BUFFER)
                    .build())
                .withCull(false)
                .withPolygonMode(PolygonMode.FILL)
                .withDepthStencilState(new DepthStencilState(CompareOp.ALWAYS_PASS, false))
                .withColorTargetState(new ColorTargetState(
                    Optional.of(com.mojang.renderpearl.api.pipeline.BlendFunction.TRANSLUCENT), GpuFormat.RGBA8_UNORM,
                    ColorTargetState.WRITE_ALL))
                .withVertexBinding(0, VertexFormat.builder(0).addAttribute("Position", GpuFormat.RGB32_FLOAT).build())
                .withPrimitiveTopology(PrimitiveTopology.TRIANGLES)
                .build();
        }
        Matrix4f inv = new Matrix4f(main.projectionMatrix).mul(main.viewRotationMatrix).invert();
        Vec3 a = unproject(inv, -1f, -1f), b = unproject(inv, 1f, -1f), c = unproject(inv, 1f, 1f), d = unproject(inv, -1f, 1f);
        try (MemoryStack stack = MemoryStack.stackPush()) {
            ByteBuffer vb = stack.malloc(6 * 3 * 4);
            putVec(vb, a); putVec(vb, b); putVec(vb, c);
            putVec(vb, a); putVec(vb, c); putVec(vb, d);
            vb.flip();
            ByteBuffer ub = stack.calloc(208);
            new Matrix4f(main.projectionMatrix).mul(main.viewRotationMatrix).get(0, ub);
            ub.putFloat(64, (float) w).putFloat(68, (float) h).putFloat(72, 0f).putFloat(76, alpha);
            ub.putFloat(80, -1f).putFloat(84, -1f).putFloat(88, 1f).putFloat(92, 1f);
            // mode 2: the whole screen; mode 3: the whole screen, the picture turned with the camera since it was taken
            ub.putFloat(108, pictureViewProj != null ? 3f : 2f);
            if (pictureViewProj != null) pictureViewProj.get(144, ub);
            GpuBufferSlice vertices = encoder.transientMemory().uploadGpu(vb, 16, GpuBuffer.USAGE_VERTEX);
            GpuBufferSlice uniforms = encoder.transientMemory().uploadGpu(ub, 256, GpuBuffer.USAGE_UNIFORM);
            try (RenderPass pass = encoder.createRenderPass(() -> "quasar crossing hold",
                mainColor, Optional.empty(), mainDepth, OptionalDouble.empty())) {
                pass.setPipeline(RenderSystem.getCompiledPipeline(holdPipeline));
                pass.setUniform("PortalColor", picture, sampler);
                pass.setUniform("PortalUniforms", uniforms);
                pass.setVertexBuffer(0, vertices);
                pass.draw(6, 1, 0, 0);
            }
        }
    }

    /** The world just entered, as its portal view had built it, until the main renderer has rebuilt it. */
    private static void drawCrossingStandIn() {
        try {
            Minecraft mc = Minecraft.getInstance();
            if (mc.level == null) return;
            CameraRenderState main = (CameraRenderState) mainCameraMethod.invoke(null);
            if (main == null || !main.initialized || main.pos == null || main.cullFrustum == null) return;
            RenderTarget mainTarget = mc.gameRenderer.mainRenderTarget();
            PortalWorldTerrain.drawStandIn(RenderSystem.getDevice().createCommandEncoder(), main,
                mainTarget.getColorTextureView(), mainTarget.getDepthTextureView());
        }
        catch (Throwable t) {
            LOGGER.warn("[Quasar Portals] crossing stand-in failed: {}", t.toString());
        }
    }

    private static void renderPortals() throws Exception {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) {
            return;
        }
        CameraRenderState main = (CameraRenderState) mainCameraMethod.invoke(null);
        if (main == null || !main.initialized || main.pos == null || main.cullFrustum == null) {
            return;
        }
        Vec3 camPos = main.pos;
        if (renderDistanceField != null) {
            renderDistanceField.setInt(null, renderDistance <= 0 ? Integer.MAX_VALUE : renderDistance);
        }

        List<Portal> portals = new ArrayList<>();
        List<Portal> doorways = new ArrayList<>();
        Portal warmPortal = null;
        double warmDist = PREWARM_BLOCKS;
        for (Entity e : mc.level.entitiesForRendering()) {
            if (!(e instanceof Portal portal) || e.isRemoved()) continue;
            if (portal.getDestDim() != mc.level.dimension()
                && portal.getOriginPos().distanceToSqr(camPos) < 64.0 * 64.0) {
                doorways.add(portal);
                // the nearest portal into another world, from its front: pre-warm that world
                double d = portal.getDistanceToNearestPointInPortal(camPos);
                if (d < warmDist && !(portal instanceof qouteall.imm_ptl.core.portal.Mirror)
                    && (portal.isInFrontOfPortal(camPos) || crossing(portal, camPos))) {
                    warmDist = d;
                    warmPortal = portal;
                }
            }
            // another dimension: only when its background world exists (cross-dimension step 3)
            if (portal.getDestDim() != mc.level.dimension()
                && PortalWorldTerrain.forDimension(portal.getDestDim()) == null) continue;
            if (portal.getOriginPos().distanceToSqr(camPos) > MAX_DISTANCE * MAX_DISTANCE) continue;
            boolean camThrough = cameraThrough(portal, camPos);
            if (!portal.isInFrontOfPortal(camPos) && !crossing(portal, camPos) && !camThrough) continue;
            if (!camThrough && !main.cullFrustum.isVisible(portal.getBoundingBox().inflate(0.1))) continue;
            portals.add(portal);
        }
        PortalEntities.crossDimensionPortals = doorways;
        if (warmPortal != null) {
            PortalWorldTerrain.requestPrewarm(warmPortal.getDestDim(), warmPortal.transformPoint(camPos));
            PortalWorldTerrain.requestReturnView(camPos, renderDistance > 0 ? renderDistance
                : mc.options.getEffectiveRenderDistance());
        }
        if (portals.isEmpty()) {
            return;
        }
        // occlusion: a portal whose chunk sections the game's visibility search (caves, walls, hills) did not reach is
        // hidden; hidden portals sort after visible ones so they never take a live slot from a visible portal
        updateOcclusion(mc, portals, camPos);
        portals.sort(Comparator.<Portal>comparingInt(p -> isHidden(p) ? 1 : 0)
            .thenComparingDouble(p -> p.getDistanceToNearestPointInPortal(camPos)));

        RenderTarget mainTarget = mc.gameRenderer.mainRenderTarget();
        int w = mainTarget.width, h = mainTarget.height;
        GpuTextureView mainColor = mainTarget.getColorTextureView();
        GpuTextureView mainDepth = mainTarget.getDepthTextureView();
        if (mainColor == null || mainDepth == null || w <= 0 || h <= 0) {
            return;
        }

        GpuDevice device = RenderSystem.getDevice();
        CommandEncoder encoder = device.createCommandEncoder();
        int gpuSlot = Stats.gpuBegin(device, encoder);
        try {
            renderEach(portals, main, device, encoder, mainColor, mainDepth, w, h);
        }
        finally {
            Stats.gpuEnd(encoder, gpuSlot);
        }
    }
    
    // ------------------------------------------------------------------
    // occlusion (Video Settings → Portals → Hide Portals Behind Walls)
    // ------------------------------------------------------------------

    /** frames in a row each portal's sections were missing from the visibility search; hidden from 3 on */
    private static final it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap missingFrames = new it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap();
    private static final it.unimi.dsi.fastutil.longs.LongOpenHashSet visibleSectionKeys = new it.unimi.dsi.fastutil.longs.LongOpenHashSet();
    static int frameHidden;

    private static boolean isHidden(Portal p) {
        return missingFrames.get(p.getId()) >= 3;
    }

    private static void updateOcclusion(Minecraft mc, List<Portal> portals, Vec3 camPos) {
        if (!PortalSettings.occlusion) {
            missingFrames.clear();
            return;
        }
        var visible = mc.levelRenderer.visibleSections();
        if (visible == null || visible.isEmpty()) {
            missingFrames.clear(); // no visibility information: treat everything as visible
            return;
        }
        visibleSectionKeys.clear();
        for (int i = 0, n = visible.size(); i < n; i++) {
            visibleSectionKeys.add(visible.get(i).getSectionNode());
        }
        it.unimi.dsi.fastutil.ints.IntOpenHashSet seen = new it.unimi.dsi.fastutil.ints.IntOpenHashSet();
        for (Portal p : portals) {
            int id = p.getId();
            seen.add(id);
            boolean reached = p.getDistanceToNearestPointInPortal(camPos) < 8.0; // right next to you: always drawn
            if (!reached) {
                // the portal's sections plus a one-section margin (a portal in open air can sit in empty sections)
                net.minecraft.world.phys.AABB box = p.getBoundingBox().inflate(1.0);
                int x0 = SectionPos.blockToSectionCoord(Mth.floor(box.minX)) - 1, x1 = SectionPos.blockToSectionCoord(Mth.floor(box.maxX)) + 1;
                int y0 = SectionPos.blockToSectionCoord(Mth.floor(box.minY)) - 1, y1 = SectionPos.blockToSectionCoord(Mth.floor(box.maxY)) + 1;
                int z0 = SectionPos.blockToSectionCoord(Mth.floor(box.minZ)) - 1, z1 = SectionPos.blockToSectionCoord(Mth.floor(box.maxZ)) + 1;
                search:
                for (int x = x0; x <= x1; x++) {
                    for (int y = y0; y <= y1; y++) {
                        for (int z = z0; z <= z1; z++) {
                            if (visibleSectionKeys.contains(SectionPos.asLong(x, y, z))) {
                                reached = true;
                                break search;
                            }
                        }
                    }
                }
            }
            if (reached) missingFrames.remove(id);
            else missingFrames.addTo(id, 1);
        }
        missingFrames.keySet().removeIf(id -> !seen.contains(id));
    }

    /**
     * The camera is passing through this portal: within a quarter block in front of its plane (or half a block past
     * it - the teleport only happens on the next tick) and inside its frame. The portal's surface is then nearer than
     * the near clip plane (or behind the camera), so the quad would be clipped away and the world behind the portal
     * would flash for a frame or two; the destination view is drawn over the whole screen instead.
     */
    static boolean crossing(Portal portal, Vec3 cam) {
        if (portal instanceof qouteall.imm_ptl.core.portal.Mirror) return false;
        // only a first-person camera goes through with you; a third-person camera stays in your world
        if (!Minecraft.getInstance().options.getCameraType().isFirstPerson()) return false;
        double dist = portal.getDistanceToPlane(cam);
        if (dist >= 0.25 || dist <= -0.5) return false;
        Vec3 v = cam.subtract(portal.getOriginPos());
        return Math.abs(v.dot(portal.getAxisW())) <= portal.getWidth() / 2 + 0.05
            && Math.abs(v.dot(portal.getAxisH())) <= portal.getHeight() / 2 + 0.05;
    }

    /**
     * Third person: the line from the player's eye back to the camera goes through this portal (eye in front of it,
     * camera behind it, crossing inside the frame). The camera is then really in the portal's destination world: that
     * world is drawn over the whole screen except where the view looks back through the portal, which keeps the
     * player's world (composite mode 4).
     */
    public static boolean cameraThrough(Portal portal, Vec3 cam) {
        if (!CAMERA_THROUGH) return false;
        if (portal instanceof qouteall.imm_ptl.core.portal.Mirror) return false;
        Minecraft mc = Minecraft.getInstance();
        if (mc.options.getCameraType().isFirstPerson() || mc.getCameraEntity() == null) return false;
        Vec3 eye = mc.getCameraEntity().getEyePosition(
            qouteall.imm_ptl.core.render.context_management.RenderStates.getPartialTick());
        double de = portal.getDistanceToPlane(eye), dc = portal.getDistanceToPlane(cam);
        if (de <= 0 || dc >= 0) return false;
        Vec3 hit = eye.add(cam.subtract(eye).scale(de / (de - dc))).subtract(portal.getOriginPos());
        return Math.abs(hit.dot(portal.getAxisW())) <= portal.getWidth() / 2
            && Math.abs(hit.dot(portal.getAxisH())) <= portal.getHeight() / 2;
    }

    /** off: the user wants a third-person camera to stay in the player's world, behind the portal */
    private static final boolean CAMERA_THROUGH = false;

    /** set around {@link #virtualCamera} for the portal being drawn */
    private static boolean crossingNow;

    private static void renderEach(List<Portal> portals, CameraRenderState main, GpuDevice device,
                                   CommandEncoder encoder, GpuTextureView mainColor, GpuTextureView mainDepth,
                                   int w, int h) throws Exception {

        Matrix4f mainViewProj = new Matrix4f(main.projectionMatrix).mul(main.viewRotationMatrix);
        int n = Math.min(Math.min(liveLimit, MAX_PORTALS), portals.size());
        int farSlots = 0;
        for (int i = 0; i < n; i++) {
            Portal portal = portals.get(i);
            
            // only the part of the screen the portal covers: its NDC rectangle (x0, y0, x1, y1)
            boolean camThrough = cameraThrough(portal, main.pos);
            boolean through = camThrough || crossing(portal, main.pos);
            if (through || portal.getDistanceToNearestPointInPortal(main.pos) < 1.5) nearCrossing = true;
            float[] rect = through ? new float[]{-1f, -1f, 1f, 1f} : screenRect(mainViewProj, main.pos, portal);
            if (rect == null) continue; // off screen
            // half resolution (a quarter of the pixels, clears and shading); the composite maps by screen fraction, so
            // the image size does not change the picture's placement
            Target t = target(i, device, Math.max(1, Math.round(w * resolutionScale)),
                Math.max(1, Math.round(h * resolutionScale)));
            
            // near or big portals redraw every frame: a reused picture was drawn from where the camera was, so while you
            // move it slides against the frame around it; far/small ones keep the cheaper rate
            boolean close = portal.getDistanceToNearestPointInPortal(main.pos) < 16.0
                || (rect[2] - rect[0]) * (rect[3] - rect[1]) / 4f > 0.05f;
            boolean update = through || close || !t.valid || t.portalId != portal.getId()
                || (viewFrame + i) % updateEvery == 0;
            if (!through && isHidden(portal)) {
                // behind something: no redraw. Keep pasting its last picture (depth-tested, so invisible while it
                // really is hidden; if the visibility search was ever wrong, a stale view shows instead of nothing)
                frameHidden++;
                if (!t.valid || t.portalId != portal.getId()) continue;
                update = false;
            }
            if (update) {
                crossingNow = through;
                CameraRenderState virtual = virtualCamera(main, portal, rect);
                crossingNow = false;
                if (virtual == null) continue;
                boolean drawn;
                long tt;
                PortalWorldTerrain other = portal.getDestDim() != Minecraft.getInstance().level.dimension()
                    ? PortalWorldTerrain.forDimension(portal.getDestDim()) : null;
                if (other != null) {
                    // another dimension: its fog colour, its sky only if it has the same kind as ours, its terrain
                    // and entities from the background world (no far terrain or clouds from there yet)
                    encoder.clearColorAndDepthTextures(t.color, PortalSky.background(other.level(), virtual.pos),
                        t.depth, 0.0);
                    long ts = System.nanoTime();
                    if (PortalSky.skyMatches(other.level())) PortalSky.drawSky(virtual, t.colorView, t.depthView);
                    frameSkyNs += System.nanoTime() - ts;
                    tt = System.nanoTime();
                    GpuBufferSlice mainFog = RenderSystem.getShaderFog();
                    GpuBufferSlice fog = PortalSky.destinationFog(encoder, other.level(), virtual.pos, main, renderDistance);
                    RenderSystem.backupProjectionMatrix();
                    try {
                        RenderSystem.setProjectionMatrix(projectionSlice(encoder, virtual),
                            com.mojang.blaze3d.ProjectionType.PERSPECTIVE);
                        RenderSystem.setShaderFog(fog);
                        drawn = other.draw(encoder, main, virtual, t.colorView, t.depthView, renderDistance);
                        frameSections += PortalWorldTerrain.lastSections;
                        if (drawn && PortalSettings.entities) {
                            // the destination world's mobs, items, block entities and its own particles
                            long te = System.nanoTime();
                            PortalEntities.draw(encoder, virtual, t.colorView, t.depthView,
                                qouteall.imm_ptl.core.render.context_management.RenderStates.getPartialTick(),
                                other.level(), PortalParticles.existing(other.level()), other.lastLightmap(), portal);
                            frameEntityNs += System.nanoTime() - te;
                        }
                    }
                    finally {
                        RenderSystem.setShaderFog(mainFog);
                        RenderSystem.restoreProjectionMatrix();
                    }
                }
                else {
                    // background = fog colour, then vanilla's sky from the portal camera, then terrain with fog that
                    // ends at the portal render distance (clouds and the far panorama come later)
                    encoder.clearColorAndDepthTextures(t.color, PortalSky.background(main), t.depth, 0.0);
                    long ts = System.nanoTime();
                    PortalSky.drawSky(virtual, t.colorView, t.depthView);
                    frameSkyNs += System.nanoTime() - ts;
                    // far terrain (Quasar Horizons portal panorama) for the nearest portals, before the live terrain
                    boolean far = false;
                    if (PortalSettings.farTerrain && farSlots < PortalFarView.MAX_PORTALS) {
                        farSlots++;
                        long tf = System.nanoTime();
                        far = PortalFarView.draw(encoder, portal.getId(), virtual, main, t.colorView, t.depthView, renderDistance);
                        frameSkyNs += System.nanoTime() - tf;
                    }
                    tt = System.nanoTime();
                    GpuBufferSlice mainFog = RenderSystem.getShaderFog();
                    // with far terrain behind it, the live terrain must not fade out at the portal distance
                    GpuBufferSlice fog = far ? null : PortalSky.portalFog(encoder, main, renderDistance);
                    // Meshelium's terrain shaders (and clouds) read vanilla's Projection buffer: give them the portal
                    // camera's oblique projection, so the portal plane clips the near side and every pass shares one depth
                    GpuBufferSlice projection = projectionSlice(encoder, virtual);
                    RenderSystem.backupProjectionMatrix();
                    try {
                        RenderSystem.setProjectionMatrix(projection, com.mojang.blaze3d.ProjectionType.PERSPECTIVE);
                        if (fog != null) RenderSystem.setShaderFog(fog);
                        drawn = (Boolean) drawTerrainMethod.invoke(null, virtual, t.colorView, t.depthView,
                            t.width, t.height);
                        if (drawn && PortalSettings.entities) {
                            long te = System.nanoTime();
                            PortalEntities.draw(encoder, virtual, t.colorView, t.depthView,
                                qouteall.imm_ptl.core.render.context_management.RenderStates.getPartialTick());
                            long de = System.nanoTime() - te;
                            frameEntityNs += de;
                            tt += de; // entities are reported on their own, not under terrain
                        }
                        if (drawn && PortalSettings.translucent && drawTranslucentMethod != null) {
                            drawTranslucentMethod.invoke(null, virtual, t.colorView, t.depthView, t.width, t.height);
                            frameSections += (Integer) lastTranslucentMethod.invoke(null);
                        }
                        if (drawn && PortalSettings.clouds) {
                            long tc = System.nanoTime();
                            PortalClouds.draw(encoder, main, virtual, t.colorView, t.depthView);
                            frameSkyNs += System.nanoTime() - tc;
                            tt += System.nanoTime() - tc; // clouds count under sky, not terrain
                        }
                    }
                    finally {
                        RenderSystem.setShaderFog(mainFog);
                        RenderSystem.restoreProjectionMatrix();
                    }
                    if (drawn && lastSectionsMethod != null) {
                        frameSections += (Integer) lastSectionsMethod.invoke(null);
                    }
                }
                frameTerrainNs += System.nanoTime() - tt;
                t.portalId = portal.getId();
                t.valid = drawn;
                t.mirrored = lastMirrored;
                if (!drawn) continue;
            }
            frameCoverage += (rect[2] - rect[0]) * (rect[3] - rect[1]) / 4f;
            
            composite(encoder, main, portal, t, rect, t.mirrored, mainColor, mainDepth, w, h, through, camThrough);
            compositedThisFrame.add(new Composited(portal, t, rect, through, camThrough));
            drawnThisFrame.add(portal.getId());
            lastPixels += (long) t.width * t.height;
        }
    }

    /** the portal camera's projection as vanilla's Projection uniform */
    private static GpuBufferSlice projectionSlice(CommandEncoder encoder, CameraRenderState virtual) {
        ByteBuffer pb = Scratch.small(64);
        virtual.projectionMatrix.get(pb);
        return encoder.transientMemory().uploadGpu(pb, 256, GpuBuffer.USAGE_UNIFORM);
    }

    static long lastPixels;
    private static long frameTerrainNs;
    private static long frameSkyNs;
    private static long frameEntityNs;
    /** whether the camera just built is a mirror view (its picture is drawn flipped) */
    private static boolean lastMirrored;
    private static int frameSections;
    private static float frameCoverage;
    
    /** F3 line (read by Quasium's [Quasium] block through reflection). */
    public static String f3Line() {
        String warm = PortalWorldTerrain.prewarmStatus();
        return warm == null ? Stats.line() : Stats.line() + " | " + warm;
    }
    
    /**
     * Portal timings, averaged over ~half a second. CPU = the whole hook (culling + command recording + composite);
     * "terrain" = Meshelium's part of it. GPU = timestamps around all portal work (clears, terrain, composite), read
     * back a few frames later without waiting.
     */
    private static final class Stats {
        private static final int RING = 8, LAG = 4;
        private static GpuQueryPool pool;
        private static boolean poolFailed;
        private static long frameNo;
        private static final long[] slotFrame = new long[RING];
        
        private static long sumCpu, sumTerrain, sumSky, sumEntity, sumEntities, maxHidden, sumGpu, sumSections, frames, gpuFrames, liveFrames;
        private static double sumCoverage;
        private static int maxLive;
        private static long windowStart = System.nanoTime();
        private static String line = "Portals: idle";
        
        static int gpuBegin(GpuDevice device, CommandEncoder encoder) {
            if (poolFailed) return -1;
            try {
                if (pool == null) {
                    pool = device.createTimestampQueryPool(RING * 2);
                    java.util.Arrays.fill(slotFrame, -1);
                }
                frameNo++;
                readBack(device, frameNo - LAG);
                int slot = (int) (frameNo % RING);
                encoder.writeTimestamp(pool, slot * 2);
                return slot;
            }
            catch (Throwable t) {
                poolFailed = true;
                LOGGER.warn("[Quasar Portals] GPU timer unavailable: {}", t.toString());
                return -1;
            }
        }
        
        static void gpuEnd(CommandEncoder encoder, int slot) {
            if (slot < 0 || pool == null) return;
            encoder.writeTimestamp(pool, slot * 2 + 1);
            slotFrame[slot] = frameNo;
        }
        
        private static void readBack(GpuDevice device, long f) {
            if (f < 1) return;
            int slot = (int) (f % RING);
            if (slotFrame[slot] != f) return;
            slotFrame[slot] = -1;
            OptionalLong[] v = pool.getValues(slot * 2, 2);
            if (v.length == 2 && v[0].isPresent() && v[1].isPresent()) {
                double period = device.getDeviceInfo().timestampPeriod();
                long ns = (long) ((v[1].getAsLong() - v[0].getAsLong()) * period);
                if (ns >= 0 && ns < 1_000_000_000L) {
                    sumGpu += ns;
                    gpuFrames++;
                }
            }
        }
        
        static void frame(long cpuNs, long terrainNs, long skyNs, long entityNs, int entities, int hidden, int live, int sections, float coverage) {
            frames++;
            sumCpu += cpuNs;
            sumTerrain += terrainNs;
            sumSky += skyNs;
            sumEntity += entityNs;
            sumEntities += entities;
            maxHidden = Math.max(maxHidden, hidden);
            sumSections += sections;
            sumCoverage += coverage;
            if (live > 0) liveFrames++;
            maxLive = Math.max(maxLive, live);
            long now = System.nanoTime();
            if (now - windowStart < 500_000_000L) return;
            if (liveFrames == 0) {
                line = "Portals: idle (no portal view this half second)";
            }
            else {
                double f = frames;
                line = String.format(java.util.Locale.ROOT,
                    "Portals: %d live, %d hidden (every %d), CPU %.3f ms (terrain %.3f, sky+clouds %.3f, %d entities %.3f), GPU %s, %d sections, %.0f%% of screen",
                    maxLive, maxHidden, updateEvery, sumCpu / f / 1e6, sumTerrain / f / 1e6, sumSky / f / 1e6, Math.round(sumEntities / f), sumEntity / f / 1e6,
                    gpuFrames > 0 ? String.format(java.util.Locale.ROOT, "%.3f ms", sumGpu / (double) gpuFrames / 1e6) : "n/a",
                    Math.round(sumSections / f), sumCoverage / f * 100) + PortalParticles.debugSummary() + backgroundEntities();
            }
            sumCpu = sumTerrain = sumSky = sumEntity = sumEntities = sumGpu = sumSections = frames = gpuFrames = liveFrames = 0;
            sumCoverage = 0;
            maxLive = 0;
            maxHidden = 0;
            windowStart = now;
        }
        
        static String line() {
            return line;
        }
    }
    
    /**
     * The portal's bounding rectangle on screen in NDC (x0, y0, x1, y1), padded by a couple of pixels and clamped to
     * the screen; the whole screen when a corner is behind the camera; null when it is off screen.
     */
    private static float[] screenRect(Matrix4f viewProj, Vec3 cam, Portal portal) {
        Vec3 o = portal.getOriginPos().subtract(cam);
        Vec3 aw = portal.getAxisW().scale(portal.getWidth() / 2);
        Vec3 ah = portal.getAxisH().scale(portal.getHeight() / 2);
        Vec3[] corners = {
            o.subtract(aw).subtract(ah), o.add(aw).subtract(ah), o.add(aw).add(ah), o.subtract(aw).add(ah)
        };
        float x0 = Float.MAX_VALUE, y0 = Float.MAX_VALUE, x1 = -Float.MAX_VALUE, y1 = -Float.MAX_VALUE;
        for (Vec3 c : corners) {
            Vector4f p = viewProj.transform(new Vector4f((float) c.x, (float) c.y, (float) c.z, 1f));
            if (p.w <= 1e-4f) {
                return new float[]{-1f, -1f, 1f, 1f};
            }
            float x = p.x / p.w, y = p.y / p.w;
            x0 = Math.min(x0, x); y0 = Math.min(y0, y);
            x1 = Math.max(x1, x); y1 = Math.max(y1, y);
        }
        float pad = 0.004f;
        x0 = Math.max(-1f, x0 - pad); y0 = Math.max(-1f, y0 - pad);
        x1 = Math.min(1f, x1 + pad); y1 = Math.min(1f, y1 + pad);
        if (x1 <= x0 || y1 <= y0) {
            return null;
        }
        return new float[]{x0, y0, x1, y1};
    }
    
    // ------------------------------------------------------------------
    // virtual camera
    // ------------------------------------------------------------------

    private static CameraRenderState virtualCamera(CameraRenderState main, Portal portal, float[] rect) {
        Vec3 camPos = main.pos;
        Vec3 vPos = portal.transformPoint(camPos);

        // the portal's linear transform L (rotation, and for mirrors a reflection), read off its basis vectors:
        // directions arriving through the portal are transformed by L, so V' = V * L^-1
        Vec3 ex = portal.transformLocalVecNonScale(new Vec3(1, 0, 0));
        Vec3 ey = portal.transformLocalVecNonScale(new Vec3(0, 1, 0));
        Vec3 ez = portal.transformLocalVecNonScale(new Vec3(0, 0, 1));
        Matrix4f l = new Matrix4f(
            (float) ex.x, (float) ex.y, (float) ex.z, 0f,
            (float) ey.x, (float) ey.y, (float) ey.z, 0f,
            (float) ez.x, (float) ez.y, (float) ez.z, 0f,
            0f, 0f, 0f, 1f);
        boolean mirrored = l.determinant() < 0f;
        Matrix4f view = new Matrix4f(main.viewRotationMatrix).mul(new Matrix4f(l).invert());
        if (mirrored) {
            // a reflection is not a camera orientation: render the unreflected view (flip view-space x, which keeps
            // triangle winding and Meshelium's face culling valid) and flip the picture back in the composite
            view = new Matrix4f().scaling(-1f, 1f, 1f).mul(view);
        }

        // clip plane on the destination portal, in camera-relative view space: visible side = content side
        Vec3 n = portal.getContentDirection().normalize();
        Vec3 q = portal.getDestPos().subtract(vPos);
        float d = (float) -(n.dot(q));
        boolean past = false;
        if (d >= 0) {
            if (!crossingNow) return null; // the virtual camera is not behind the destination portal (player not in front)
            past = true; // a hair past the plane before the teleport tick: already "in" the destination, nothing to cut
        }
        Vector3f nv = past ? new Vector3f(0f, 0f, -1f)
            : view.transformDirection(new Vector3f((float) n.x, (float) n.y, (float) n.z));
        if (past) d = -0.05f; // the ordinary near plane (view space looks down -z)
        Vector4f clip = new Vector4f(nv.x, nv.y, nv.z, d);

        // zoom the projection onto the portal's screen rectangle: rows 0/1 remapped so the rectangle fills the image
        // (narrows the frustum too, so only terrain that can appear inside the portal is drawn)
        Matrix4f sub = new Matrix4f(main.projectionMatrix);
        float cx = (rect[0] + rect[2]) * 0.5f, hx = (rect[2] - rect[0]) * 0.5f;
        if (mirrored) {
            // a mirror's picture is rendered unflipped and flipped back in the composite, so the part of the image
            // that lands inside the mirror is the screen rectangle mirrored left-right
            cx = -cx;
        }
        float cy = (rect[1] + rect[3]) * 0.5f, hy = (rect[3] - rect[1]) * 0.5f;
        Vector4f row0 = sub.getRow(0, new Vector4f()), row1 = sub.getRow(1, new Vector4f());
        Vector4f row3 = sub.getRow(3, new Vector4f());
        sub.setRow(0, new Vector4f(row0).sub(new Vector4f(row3).mul(cx)).div(hx));
        sub.setRow(1, new Vector4f(row1).sub(new Vector4f(row3).mul(cy)).div(hy));
        
        // the picture uses the full-screen projection (the composite is pixel for pixel); only culling is narrowed
        // to the portal's screen rectangle, so terrain that cannot appear inside the portal is never drawn
        // how far the portal view must reach before its (tilted) far plane may cut: the portal render distance,
        // or the main view's distance when unlimited, with margin for the diagonal and height
        float reach = (renderDistance > 0 ? renderDistance : Math.max(8, Minecraft.getInstance().options.getEffectiveRenderDistance()))
            * 16f * 1.75f + 64f;
        Matrix4f proj = obliqueReverseZ(main.projectionMatrix, clip, reach);
        Matrix4f cullProj = obliqueReverseZ(sub, clip, reach);
        if (proj == null || cullProj == null) {
            return null;
        }

        CameraRenderState vc = new CameraRenderState();
        vc.pos = vPos;
        vc.blockPos = BlockPos.containing(vPos);
        vc.initialized = true;
        vc.xRot = main.xRot;
        vc.yRot = main.yRot;
        vc.orientation = main.orientation;
        vc.fogType = main.fogType;
        vc.fogData = main.fogData;
        vc.depthFar = main.depthFar;
        vc.hudFov = main.hudFov;
        vc.isFirstPerson = main.isFirstPerson;
        vc.viewRotationMatrix = view;
        lastMirrored = mirrored;
        vc.projectionMatrix = proj;
        Frustum frustum = new Frustum(view, cullProj);
        frustum.prepare(vPos.x, vPos.y, vPos.z);
        vc.cullFrustum = frustum;
        return vc;
    }

    /**
     * Oblique near plane (Lengyel) for 26.x's reverse-Z projection: convert to the standard 0..1 depth form
     * (row2_std = row3 - row2), replace the near plane with {@code clipPlane} (view space, visible side positive,
     * camera on the negative side), convert back.
     */
    static Matrix4f obliqueReverseZ(Matrix4f projection, Vector4f clipPlane, float reach) {
        Matrix4f p = new Matrix4f(projection);
        Vector4f r2 = p.getRow(2, new Vector4f());
        Vector4f r3 = p.getRow(3, new Vector4f());
        Matrix4f std = new Matrix4f(p);
        std.setRow(2, new Vector4f(r3).sub(r2));
        Matrix4f inv = new Matrix4f(std).invert();
        // the plane in clip space picks the far corner on the visible side
        Vector4f planeClip = new Matrix4f(inv).transpose().transform(new Vector4f(clipPlane));
        Vector4f qClip = new Vector4f(Math.signum(planeClip.x), Math.signum(planeClip.y), 1f, 1f);
        Vector4f q = inv.transform(qClip);
        float dot = clipPlane.dot(q);
        if (!(Math.abs(dot) > 1e-12f) || !Float.isFinite(dot)) {
            return null;
        }
        Vector4f c = new Vector4f(clipPlane).mul(1f / dot);
        // Lengyel's scale puts the new far plane through a frustum corner at infinity, which is fine for a portal
        // seen head-on; at a grazing angle that plane swings in and cuts the distant view (washed-out sky with
        // specks in a mirror seen along the wall). Any positive scale keeps the near plane on the portal, so shrink
        // c until nothing within `reach` is beyond the far plane (standard depth c.P <= w for the four corner rays).
        Matrix4f projInv = new Matrix4f(p).invert();
        float maxRatio = 0f;
        for (int i = 0; i < 4; i++) {
            Vector4f corner = projInv.transform(new Vector4f((i & 1) == 0 ? -1f : 1f, (i & 2) == 0 ? -1f : 1f, 0.5f, 1f));
            Vector3f dir = new Vector3f(corner.x / corner.w, corner.y / corner.w, corner.z / corner.w);
            if (!(dir.z < 0f)) continue;
            dir.normalize().mul(reach);
            float ratio = (c.x * dir.x + c.y * dir.y + c.z * dir.z + c.w) / -dir.z;
            if (Float.isFinite(ratio)) maxRatio = Math.max(maxRatio, ratio);
        }
        if (maxRatio > 0.999f) {
            c.mul(0.999f / maxRatio);
        }
        Matrix4f out = new Matrix4f(p);
        out.setRow(2, new Vector4f(r3).sub(c));
        return out;
    }

    // ------------------------------------------------------------------
    // composite
    // ------------------------------------------------------------------

    private static void composite(CommandEncoder encoder, CameraRenderState main, Portal portal, Target t,
                                  float[] rect, boolean mirrored, GpuTextureView mainColor, GpuTextureView mainDepth, int w, int h,
                                  boolean fullScreen, boolean camThrough) {
        ensurePipeline();
        Vec3 cam = main.pos;
        Vec3 a, b, c, d;
        if (fullScreen) {
            // passing through: a quad over the whole screen just beyond the near plane (reverse-Z: depth 1 = near)
            Matrix4f inv = new Matrix4f(main.projectionMatrix).mul(main.viewRotationMatrix).invert();
            a = unproject(inv, -1f, -1f);
            b = unproject(inv, 1f, -1f);
            c = unproject(inv, 1f, 1f);
            d = unproject(inv, -1f, 1f);
        } else {
            // Never exactly on the portal plane (a mirror sits on the glass face and would z-fight with it):
            // mirrors go 3 mm BEHIND the glass face, so the glass texture's own pixels draw over the reflection
            // (the original mod's look); other portals go 3 mm in front, since they may sit flush on a wall.
            double nudge = portal instanceof qouteall.imm_ptl.core.portal.Mirror ? -0.003 : 0.003;
            Vec3 o = portal.getOriginPos().subtract(cam).add(portal.getNormal().scale(nudge));
            Vec3 aw = portal.getAxisW().scale(portal.getWidth() / 2);
            Vec3 ah = portal.getAxisH().scale(portal.getHeight() / 2);
            a = o.subtract(aw).subtract(ah);
            b = o.add(aw).subtract(ah);
            c = o.add(aw).add(ah);
            d = o.subtract(aw).add(ah);
        }

        try (MemoryStack stack = MemoryStack.stackPush()) {
            ByteBuffer vb = stack.malloc(6 * 3 * 4);
            putVec(vb, a); putVec(vb, b); putVec(vb, c);
            putVec(vb, a); putVec(vb, c); putVec(vb, d);
            vb.flip();
            ByteBuffer ub = stack.calloc(208); // the block's full size (uPictureViewProj unused here)
            Matrix4f viewProj = new Matrix4f(main.projectionMatrix).mul(main.viewRotationMatrix);
            viewProj.get(0, ub);
            ub.putFloat(64, (float) w);   // main screen size
            ub.putFloat(68, (float) h);
            ub.putFloat(72, mirrored ? 1f : 0f); // mirror: flip the picture horizontally
            ub.putFloat(80, rect[0]);     // the portal view's NDC rectangle
            ub.putFloat(84, rect[1]);
            ub.putFloat(88, rect[2]);
            ub.putFloat(92, rect[3]);
            // portal rectangle for the per-pixel test (mode 1), camera-relative
            Vec3 po = portal.getOriginPos().subtract(cam), pw = portal.getAxisW(), ph = portal.getAxisH();
            // in front of the plane: only rays going through the portal show the other world (mode 1); just past it
            // (eyes through, body not yet teleported): everything shows the other world except rays looking back
            // through the portal, which keep this world (mode 4) - looking along the portal splits at its plane
            int mode = !fullScreen ? 0 : camThrough || portal.getDistanceToPlane(cam) < 0 ? 4 : 1;
            ub.putFloat(96, (float) po.x).putFloat(100, (float) po.y).putFloat(104, (float) po.z).putFloat(108, mode);
            ub.putFloat(112, (float) pw.x).putFloat(116, (float) pw.y).putFloat(120, (float) pw.z)
                .putFloat(124, (float) (portal.getWidth() / 2));
            ub.putFloat(128, (float) ph.x).putFloat(132, (float) ph.y).putFloat(136, (float) ph.z)
                .putFloat(140, (float) (portal.getHeight() / 2));

            GpuBufferSlice vertices = encoder.transientMemory().uploadGpu(vb, 16, GpuBuffer.USAGE_VERTEX);
            GpuBufferSlice uniforms = encoder.transientMemory().uploadGpu(ub, 256, GpuBuffer.USAGE_UNIFORM);

            try (RenderPass pass = encoder.createRenderPass(() -> "quasar portal composite",
                mainColor, Optional.empty(), mainDepth, OptionalDouble.empty())) {
                pass.setPipeline(RenderSystem.getCompiledPipeline(compositePipeline));
                pass.setUniform("PortalColor", t.colorView, sampler);
                pass.setUniform("PortalUniforms", uniforms);
                pass.setVertexBuffer(0, vertices);
                pass.draw(6, 1, 0, 0);
            }
        }
    }

    /** F3: entities each background world holds on this client (0 = the server never sent them) */
    private static String backgroundEntities() {
        StringBuilder sb = new StringBuilder(", other-world entities:");
        Minecraft mc = Minecraft.getInstance();
        boolean any = false;
        for (net.minecraft.client.multiplayer.ClientLevel l : qouteall.imm_ptl.core.ClientWorldLoader.getClientWorlds()) {
            if (l == mc.level) continue;
            any = true;
            int all = 0, living = 0;
            for (net.minecraft.world.entity.Entity e : l.entitiesForRendering()) {
                all++;
                if (e instanceof net.minecraft.world.entity.LivingEntity) living++;
            }
            sb.append(' ').append(l.dimension().identifier().getPath()).append(' ').append(all)
                .append(" (").append(living).append(" mobs)");
        }
        return any ? sb.toString() : ", other-world entities: none";
    }

    /** camera-relative world position of screen point (x, y) in NDC, just beyond the near plane */
    private static Vec3 unproject(Matrix4f invViewProj, float x, float y) {
        Vector4f p = invViewProj.transform(new Vector4f(x, y, 0.99f, 1f));
        return new Vec3(p.x / p.w, p.y / p.w, p.z / p.w);
    }

    private static void putVec(ByteBuffer b, Vec3 v) {
        b.putFloat((float) v.x).putFloat((float) v.y).putFloat((float) v.z);
    }

    private static void ensurePipeline() {
        if (compositePipeline != null) {
            return;
        }
        compositePipeline = RenderPipeline.builder()
            .withLocation(Identifier.parse("immersive_portals:pipeline/portal_composite"))
            .withVertexShader(Identifier.parse("immersive_portals:portal_composite"))
            .withFragmentShader(Identifier.parse("immersive_portals:portal_composite"))
            .withBindGroupLayout(BindGroupLayout.builder()
                .withUniform("PortalColor", UniformType.COMBINED_IMAGE_SAMPLER)
                .withUniform("PortalUniforms", UniformType.UNIFORM_BUFFER)
                .build())
            .withCull(false)
            .withPolygonMode(PolygonMode.FILL)
            // 26.x is reverse-Z: nearer = larger; write the surface's depth so later passes are hidden behind it
            .withDepthStencilState(new DepthStencilState(CompareOp.GREATER_THAN_OR_EQUAL, true))
            .withColorTargetState(new ColorTargetState(Optional.empty(), GpuFormat.RGBA8_UNORM,
                ColorTargetState.WRITE_ALL))
            .withVertexBinding(0, VertexFormat.builder(0).addAttribute("Position", GpuFormat.RGB32_FLOAT).build())
            .withPrimitiveTopology(PrimitiveTopology.TRIANGLES)
            .build();
        sampler = RenderSystem.getDevice().createSampler(AddressMode.CLAMP_TO_EDGE, AddressMode.CLAMP_TO_EDGE,
            FilterMode.NEAREST, FilterMode.NEAREST, 1, OptionalDouble.empty()); // crisp pixels (user preference)
    }

    // ------------------------------------------------------------------
    // per-portal images
    // ------------------------------------------------------------------

    private static final class Target {
        GpuTexture color, depth;
        GpuTextureView colorView, depthView;
        int width, height;
        /** which portal the picture belongs to, whether it holds a picture, and whether that was a mirror view */
        int portalId = Integer.MIN_VALUE;
        boolean valid;
        boolean mirrored;
    }

    private static Target target(int slot, GpuDevice device, int w, int h) {
        Target t = targets[slot];
        if (t != null && t.width == w && t.height == h) {
            return t;
        }
        if (t != null) {
            t.colorView.close();
            t.depthView.close();
            t.color.close();
            t.depth.close();
        }
        t = new Target();
        int usage = GpuTexture.USAGE_RENDER_ATTACHMENT | GpuTexture.USAGE_TEXTURE_BINDING | GpuTexture.USAGE_COPY_DST;
        t.color = device.createTexture("quasar_portal_color_" + slot, usage, GpuFormat.RGBA8_UNORM, w, h, 1, 1);
        t.depth = device.createTexture("quasar_portal_depth_" + slot, usage, GpuFormat.D32_FLOAT, w, h, 1, 1);
        t.colorView = device.createTextureView(t.color);
        t.depthView = device.createTextureView(t.depth);
        t.width = w;
        t.height = h;
        targets[slot] = t;
        return t;
    }
}
