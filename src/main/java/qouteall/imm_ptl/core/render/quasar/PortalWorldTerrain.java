package qouteall.imm_ptl.core.render.quasar;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.VertexSorting;
import com.mojang.logging.LogUtils;
import com.mojang.renderpearl.api.buffers.GpuBuffer;
import com.mojang.renderpearl.api.buffers.GpuBufferSlice;
import com.mojang.renderpearl.api.commands.CommandEncoder;
import com.mojang.renderpearl.api.commands.RenderPass;
import com.mojang.renderpearl.api.device.GpuDevice;
import com.mojang.renderpearl.api.pipeline.IndexType;
import com.mojang.renderpearl.api.pipeline.PrimitiveTopology;
import com.mojang.renderpearl.api.textures.FilterMode;
import com.mojang.renderpearl.api.textures.GpuSampler;
import com.mojang.renderpearl.api.textures.GpuTextureView;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.SectionBufferBuilderPack;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.chunk.RenderRegionCache;
import net.minecraft.client.renderer.chunk.RenderSectionRegion;
import net.minecraft.client.renderer.chunk.SectionCompiler;
import net.minecraft.client.renderer.chunk.SectionRenderDispatcher;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.core.SectionPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.slf4j.Logger;
import qouteall.imm_ptl.core.ClientWorldLoader;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Quasar MC (cross-dimension step 3): terrain of a background client world (e.g. the Nether while the player is in
 * the Overworld), drawn into portal views.
 *
 * <p>Quasar Meshelium's terrain store holds the player's world only, so another world gets this small renderer of
 * its own, built entirely from vanilla 26.3 parts: {@link SectionCompiler} meshes the sections on two worker
 * threads (marked "foreign" so Meshelium's compile tap ignores them), the meshes go into one vertex/index buffer per
 * section and layer, and the vanilla terrain pipelines draw them ({@code RenderPipelines.SOLID/CUTOUT/TRANSLUCENT_TERRAIN},
 * the same uniforms vanilla's {@code ChunkSectionsToRender} binds). It does not need mesh shaders, so it also runs on
 * OpenGL.</p>
 *
 * <p>Camera: vanilla's terrain shader places a vertex at {@code Position + (ChunkPosition - CameraBlockPos) +
 * CameraOffset}, with CameraBlockPos/CameraOffset from the frame's Globals block (the main camera). Instead of
 * rewriting Globals, each section's ChunkPosition is shifted by (main camera block - portal camera block) and the
 * remaining sub-block difference goes into the model-view matrix, so the same shader draws from the portal camera.</p>
 *
 * <p>Not yet: the other world's own lightmap (the main world's is used; Nether block light looks a little darker than
 * in the Nether itself), block entities (chests, signs), translucent re-sorting when the portal camera moves far
 * (sorted for the camera position at compile time).</p>
 */
public final class PortalWorldTerrain {
    private static final Logger LOGGER = LogUtils.getLogger();

    /** new section meshes requested per frame and world (region copies happen on the render thread) */
    private static final int SCHEDULE_PER_FRAME = 6;
    private static final int WORKERS = 2;
    /** sections farther than the radius plus this many chunks from the portal camera are freed */
    private static final int EVICT_MARGIN_CHUNKS = 3;
    /** finished compiles uploaded per frame (buffer creation runs on the render thread) */
    private static final int UPLOADS_PER_FRAME = 12;
    /** frames to wait before closing a buffer (it may still be read by frames in flight) */
    private static final int CLOSE_LAG_FRAMES = 4;

    private static final Map<ResourceKey<Level>, PortalWorldTerrain> WORLDS = new HashMap<>();
    private static final ConcurrentLinkedQueue<Done> DONE = new ConcurrentLinkedQueue<>();
    private static final ArrayDeque<PendingClose> PENDING_CLOSE = new ArrayDeque<>();
    private static ExecutorService executor;
    private static ArrayBlockingQueue<SectionBufferBuilderPack> packs;
    private static Field compilerField;
    private static boolean failed;
    private static long frame;

    /** sections drawn by the last {@link #draw} call (F3) */
    static int lastSections;

    private final ResourceKey<Level> dimension;
    private final ClientLevel level;
    private final Long2ObjectOpenHashMap<Section> sections = new Long2ObjectOpenHashMap<>();
    private final AtomicInteger inFlight = new AtomicInteger();
    /** sections with geometry (drawn); the rest of {@link #sections} are empty or not built yet */
    private final it.unimi.dsi.fastutil.objects.ObjectOpenHashSet<Section> drawable =
        new it.unimi.dsi.fastutil.objects.ObjectOpenHashSet<>();
    private long preparedFrame = -1;
    /** this world's own lightmap (the Nether's ambient light, block-light tint, no sky), made on first draw */
    private net.minecraft.client.renderer.Lightmap lightmap;
    private final net.minecraft.client.renderer.state.LightmapRenderState lightState =
        new net.minecraft.client.renderer.state.LightmapRenderState();
    // the last full scan: only redone when the camera changes section column, the radius changes, sections are
    // dirtied, chunks load/unload or the last scan left work for later
    private int scanSx = Integer.MIN_VALUE, scanSz = Integer.MIN_VALUE, scanRadius = -1, scanLoaded = -1;
    private boolean scanAgain = true;
    private int[] columnOrder = new int[0];
    private int columnOrderRadius = -1;
    private boolean disposed;

    private static final class Section {
        final long key;
        final int sx, sy, sz;
        final EnumMap<ChunkSectionLayer, LayerMesh> layers = new EnumMap<>(ChunkSectionLayer.class);
        boolean built;     // a compile result has arrived at least once (possibly empty)
        boolean compiling;
        boolean dirty;
        final AABB box;

        Section(long key) {
            this.key = key;
            this.sx = SectionPos.x(key);
            this.sy = SectionPos.y(key);
            this.sz = SectionPos.z(key);
            this.box = new AABB(sx * 16.0, sy * 16.0, sz * 16.0, sx * 16.0 + 16, sy * 16.0 + 16, sz * 16.0 + 16);
        }
    }

    private record LayerMesh(GpuBuffer vertices, GpuBuffer indices, IndexType indexType, int indexCount) {}

    private record Done(PortalWorldTerrain world, Section section, SectionCompiler.Results results,
                        SectionBufferBuilderPack pack, Throwable error) {}

    private record PendingClose(long frame, GpuBuffer buffer) {}

    private PortalWorldTerrain(ResourceKey<Level> dimension, ClientLevel level) {
        this.dimension = dimension;
        this.level = level;
    }

    // ------------------------------------------------------------------
    // per frame
    // ------------------------------------------------------------------

    /**
     * Once per frame, before any portal view: applies finished compiles, closes buffers whose frames are done, and
     * drops worlds that are gone or became the player's world.
     */
    static void beginFrame() {
        if (failed) return;
        frame++;
        try {
            Minecraft mc = Minecraft.getInstance();
            Iterator<Map.Entry<ResourceKey<Level>, PortalWorldTerrain>> it = WORLDS.entrySet().iterator();
            while (it.hasNext()) {
                PortalWorldTerrain w = it.next().getValue();
                ClientLevel current = ClientWorldLoader.peekWorld(w.dimension);
                if (current != w.level || w.level == mc.level) {
                    w.dispose();
                    it.remove();
                }
            }
            // a few per frame: a burst of finished compiles (portal just opened) is spread over frames
            for (int i = 0; i < UPLOADS_PER_FRAME; i++) {
                Done d = DONE.poll();
                if (d == null) break;
                d.world.accept(d);
            }
            while (!PENDING_CLOSE.isEmpty() && PENDING_CLOSE.peekFirst().frame + CLOSE_LAG_FRAMES <= frame) {
                PendingClose c = PENDING_CLOSE.pollFirst();
                if (!c.buffer.isClosed()) c.buffer.close();
            }
        }
        catch (Throwable t) {
            fail("frame bookkeeping", t);
        }
    }

    /** The terrain of {@code dim}'s background world, or null when there is none (or it is the player's world). */
    static PortalWorldTerrain forDimension(ResourceKey<Level> dim) {
        if (failed) return null;
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.level.dimension() == dim || ClientWorldLoader.isSwitchingDimension()) return null;
        ClientLevel level = ClientWorldLoader.peekWorld(dim);
        if (level == null || level == mc.level) return null;
        PortalWorldTerrain w = WORLDS.get(dim);
        if (w == null || w.level != level) {
            if (w != null) w.dispose();
            w = new PortalWorldTerrain(dim, level);
            WORLDS.put(dim, w);
            LOGGER.info("[Quasar Portals] portal terrain for background world {}", dim.identifier());
        }
        return w;
    }

    ClientLevel level() {
        return level;
    }

    /** Frees everything (world unloaded or session left). */
    public static void disposeAll() {
        for (PortalWorldTerrain w : WORLDS.values()) w.dispose();
        WORLDS.clear();
    }

    private void dispose() {
        if (disposed) return;
        disposed = true;
        for (Section s : sections.values()) freeMeshes(s);
        sections.clear();
        drawable.clear();
        if (lightmap != null) {
            try {
                lightmap.close();
            }
            catch (Throwable ignored) {
            }
            lightmap = null;
        }
        LOGGER.info("[Quasar Portals] portal terrain for {} freed", dimension.identifier());
    }

    // ------------------------------------------------------------------
    // building
    // ------------------------------------------------------------------

    /**
     * Light updates, dirty sections and new compiles around the portal camera; at most once per frame per world.
     */
    private void prepare(Vec3 cam, int radiusChunks) {
        if (preparedFrame == frame) return;
        preparedFrame = frame;

        // The world is never extracted by the main renderer, so its light engine's pending work runs here.
        // Not pollLightUpdates(): the queued tasks are vanilla packet handlers that apply light data to the
        // packet listener's *current* world, which outside withSwitchedWorld is the player's world. Run here,
        // the Nether's light (no sky light) landed on the Overworld chunks at the same coordinates and turned
        // them black. ClientWorldLoader.tickRemoteWorld polls them each tick inside withSwitchedWorld.
        level.getChunkSource().getLightEngine().runLightUpdates();

        boolean changed = false;
        SecondaryLevelExtractor ext = ClientWorldLoader.SECONDARY_EXTRACTORS.get(dimension);
        if (ext != null) {
            LongOpenHashSet dirty = ext.drainDirtySections();
            if (dirty == null) {
                for (Section s : sections.values()) s.dirty = true;
                changed = true;
            }
            else if (!dirty.isEmpty()) {
                for (it.unimi.dsi.fastutil.longs.LongIterator i = dirty.iterator(); i.hasNext(); ) {
                    Section s = sections.get(i.nextLong());
                    if (s != null) s.dirty = true;
                }
                changed = true; // also covers dirty sections not in the map yet
            }
        }

        int camSx = SectionPos.blockToSectionCoord(cam.x);
        int camSz = SectionPos.blockToSectionCoord(cam.z);
        int loaded = level.getChunkSource().getLoadedChunksCount();
        if (!changed && !scanAgain && camSx == scanSx && camSz == scanSz && radiusChunks == scanRadius
                && loaded == scanLoaded) {
            return; // nothing new since the last scan
        }
        scanSx = camSx;
        scanSz = camSz;
        scanRadius = radiusChunks;
        scanLoaded = loaded;
        scanAgain = false;

        int minSy = level.getMinSectionY(), maxSy = level.getMaxSectionY();
        int[] order = columnOrder(radiusChunks);
        int budget = SCHEDULE_PER_FRAME;
        SectionCompiler compiler = compiler();
        // fresh each batch, like vanilla: the cache keeps section copies, which would go stale
        RenderRegionCache regionCache = new RenderRegionCache();
        for (int i = 0; i < order.length; i += 2) {
            int cx = camSx + order[i], cz = camSz + order[i + 1];
            boolean ready = level.hasChunk(cx, cz) && neighboursLoaded(cx, cz);
            LevelChunk chunk = ready ? level.getChunk(cx, cz) : null;
            for (int sy = minSy; sy <= maxSy; sy++) {
                long key = SectionPos.asLong(cx, sy, cz);
                Section s = sections.get(key);
                if (!ready) {
                    if (s != null) {
                        if (s.compiling) {
                            scanAgain = true; // drop it once its compile is back
                        }
                        else {
                            freeMeshes(s); // the chunk unloaded: drop what was drawn there
                            sections.remove(key);
                        }
                    }
                    continue;
                }
                if (s == null) {
                    LevelChunkSection cs = chunk.getSection(level.getSectionIndexFromSectionY(sy));
                    s = new Section(key);
                    sections.put(key, s);
                    if (cs == null || cs.hasOnlyAir()) {
                        // nothing to draw; remembered as built-empty so it isn't checked again until changed
                        s.built = true;
                        continue;
                    }
                    s.dirty = true;
                }
                if (!s.dirty) continue;
                if (s.compiling || budget <= 0 || compiler == null || inFlight.get() >= WORKERS * 4) {
                    scanAgain = true; // more to build: scan again next frame
                    continue;
                }
                if (s.built && s.layers.isEmpty()) {
                    // an empty section that changed: only compile if it isn't air any more
                    LevelChunkSection cs = chunk.getSection(level.getSectionIndexFromSectionY(sy));
                    if (cs == null || cs.hasOnlyAir()) {
                        s.dirty = false;
                        continue;
                    }
                }
                schedule(s, compiler, regionCache, cam);
                budget--;
            }
        }

        // free what is well outside the portal camera's range
        int keep = radiusChunks + EVICT_MARGIN_CHUNKS;
        Iterator<Section> it = sections.values().iterator();
        while (it.hasNext()) {
            Section s = it.next();
            if (!s.compiling && (Math.abs(s.sx - camSx) > keep || Math.abs(s.sz - camSz) > keep)) {
                freeMeshes(s);
                it.remove();
            }
        }
    }

    private boolean neighboursLoaded(int cx, int cz) {
        return level.hasChunk(cx - 1, cz) && level.hasChunk(cx + 1, cz)
            && level.hasChunk(cx, cz - 1) && level.hasChunk(cx, cz + 1)
            && level.hasChunk(cx - 1, cz - 1) && level.hasChunk(cx + 1, cz + 1)
            && level.hasChunk(cx - 1, cz + 1) && level.hasChunk(cx + 1, cz - 1);
    }

    /** column offsets (dx, dz pairs) within the radius, nearest first */
    private int[] columnOrder(int r) {
        if (r == columnOrderRadius) return columnOrder;
        List<int[]> cols = new ArrayList<>();
        for (int dx = -r; dx <= r; dx++)
            for (int dz = -r; dz <= r; dz++) cols.add(new int[]{dx, dz});
        cols.sort((a, b) -> Integer.compare(a[0] * a[0] + a[1] * a[1], b[0] * b[0] + b[1] * b[1]));
        int[] out = new int[cols.size() * 2];
        for (int i = 0; i < cols.size(); i++) {
            out[i * 2] = cols.get(i)[0];
            out[i * 2 + 1] = cols.get(i)[1];
        }
        columnOrder = out;
        columnOrderRadius = r;
        return out;
    }

    private void schedule(Section s, SectionCompiler compiler, RenderRegionCache regionCache, Vec3 cam) {
        RenderSectionRegion region = regionCache.createRegion(level, s.key); // copies the blocks (render thread)
        SectionPos pos = SectionPos.of(s.key);
        // translucent faces sorted back to front for the portal camera as it is now
        VertexSorting sorting = VertexSorting.byDistance(
            (float) (cam.x - pos.minBlockX()), (float) (cam.y - pos.minBlockY()), (float) (cam.z - pos.minBlockZ()));
        s.compiling = true;
        s.dirty = false;
        inFlight.incrementAndGet();
        executor().execute(() -> {
            SectionBufferBuilderPack pack = null;
            try {
                pack = packs.take();
                SectionCompiler.Results r = compiler.compile(pos, region, sorting, pack, List.of());
                DONE.add(new Done(this, s, r, pack, null));
            }
            catch (Throwable t) {
                DONE.add(new Done(this, s, null, pack, t));
            }
        });
    }

    /** render thread: a compile finished; upload its meshes */
    private void accept(Done d) {
        inFlight.decrementAndGet();
        Section s = d.section;
        s.compiling = false;
        try {
            if (d.error != null) {
                LOGGER.warn("[Quasar Portals] compiling a {} section failed: {}", dimension.identifier(), d.error.toString());
                return;
            }
            if (disposed || sections.get(s.key) != s) return; // dropped while compiling
            scanAgain = true; // a section may have been dirtied or unloaded while it compiled
            freeMeshes(s);
            s.built = true;
            GpuDevice device = RenderSystem.getDevice();
            for (Map.Entry<ChunkSectionLayer, MeshData> e : d.results.renderedLayers.entrySet()) {
                MeshData mesh = e.getValue();
                MeshData.DrawState ds = mesh.drawState();
                if (ds.vertexCount() == 0 || ds.indexCount() == 0) continue;
                String label = "quasar_portal_terrain " + s.sx + "," + s.sy + "," + s.sz + " " + e.getKey().label();
                GpuBuffer vb = device.createBuffer(() -> label, GpuBuffer.USAGE_VERTEX | GpuBuffer.USAGE_COPY_DST,
                    mesh.vertexBuffer());
                GpuBuffer ib = null;
                ByteBuffer indexBytes = mesh.indexBuffer();
                if (indexBytes != null) {
                    ib = device.createBuffer(() -> label + " indices", GpuBuffer.USAGE_INDEX | GpuBuffer.USAGE_COPY_DST,
                        indexBytes);
                }
                s.layers.put(e.getKey(), new LayerMesh(vb, ib, ib != null ? ds.indexType() : null, ds.indexCount()));
            }
            if (!s.layers.isEmpty()) drawable.add(s);
        }
        catch (Throwable t) {
            fail("uploading section meshes", t);
        }
        finally {
            if (d.results != null) d.results.release();
            if (d.pack != null) {
                d.pack.clearAll();
                packs.offer(d.pack);
            }
        }
    }

    private void freeMeshes(Section s) {
        drawable.remove(s);
        for (LayerMesh m : s.layers.values()) {
            PENDING_CLOSE.addLast(new PendingClose(frame, m.vertices));
            if (m.indices != null) PENDING_CLOSE.addLast(new PendingClose(frame, m.indices));
        }
        s.layers.clear();
    }

    // ------------------------------------------------------------------
    // drawing
    // ------------------------------------------------------------------

    /**
     * Draws this world's terrain within {@code radiusChunks} of the portal camera into the given attachments. The
     * Projection (portal camera's oblique projection) and Fog buffers must already be set. Returns false when it could
     * not draw at all.
     */
    boolean draw(CommandEncoder encoder, CameraRenderState main, CameraRenderState virtual,
                 GpuTextureView color, GpuTextureView depth, int radiusChunks) {
        lastSections = 0;
        if (failed || disposed) return false;
        try {
            int radius = radiusChunks > 0 ? radiusChunks
                : Math.max(2, Minecraft.getInstance().options.getEffectiveRenderDistance());
            Vec3 cam = virtual.pos;
            prepare(cam, radius);

            // visible sections: in range, in the portal camera's (portal-narrowed) frustum
            int camSx = SectionPos.blockToSectionCoord(cam.x), camSz = SectionPos.blockToSectionCoord(cam.z);
            List<Section> visible = new ArrayList<>();
            for (Section s : drawable) {
                if (Math.abs(s.sx - camSx) > radius || Math.abs(s.sz - camSz) > radius) continue;
                if (!virtual.cullFrustum.isVisible(s.box)) continue;
                visible.add(s);
            }
            if (visible.isEmpty()) return true;
            // near to far (opaque: early depth rejects); translucent is drawn far to near below
            visible.sort((a, b) -> Double.compare(dist2(a, cam), dist2(b, cam)));

            // the frame's Globals hold the MAIN camera; shift each section and fold the sub-block rest into the view
            Vec3 m = main.pos;
            int mbx = (int) Math.floor(m.x), mby = (int) Math.floor(m.y), mbz = (int) Math.floor(m.z);
            int vbx = (int) Math.floor(cam.x), vby = (int) Math.floor(cam.y), vbz = (int) Math.floor(cam.z);
            float dx = (float) ((vbx - cam.x) - (mbx - m.x));
            float dy = (float) ((vby - cam.y) - (mby - m.y));
            float dz = (float) ((vbz - cam.z) - (mbz - m.z));
            Matrix4f modelView = new Matrix4f(virtual.viewRotationMatrix).translate(dx, dy, dz);

            Minecraft mc = Minecraft.getInstance();
            AbstractTexture atlas = mc.getTextureManager().getTexture(TextureAtlas.LOCATION_BLOCKS);
            GpuTextureView atlasView = atlas.getTextureView();
            GpuSampler atlasSampler = atlas.getSampler() != null ? atlas.getSampler()
                : RenderSystem.getSamplerCache().getRepeat(FilterMode.NEAREST, true);
            GpuTextureView lightmap = updateLightmap(cam);
            lastLightmap = lightmap;
            GpuSampler lightSampler = RenderSystem.getSamplerCache().getClampToEdge(FilterMode.LINEAR);

            ByteBuffer tb = Scratch.small(80);
            modelView.get(0, tb);
            tb.putInt(64, atlasView.getWidth(0));
            tb.putInt(68, atlasView.getHeight(0));
            GpuBufferSlice terrainUniform = encoder.transientMemory().uploadGpu(tb, 256, GpuBuffer.USAGE_UNIFORM);

            int n = visible.size();
            ByteBuffer cb = Scratch.large(n * 256);
            for (int i = 0; i < n; i++) {
                Section s = visible.get(i);
                int o = i * 256;
                cb.putInt(o, s.sx * 16 - vbx + mbx);
                cb.putInt(o + 4, s.sy * 16 - vby + mby);
                cb.putInt(o + 8, s.sz * 16 - vbz + mbz);
                cb.putFloat(o + 12, 1.0f); // ChunkVisibility: fully faded in
            }
            GpuBufferSlice all = encoder.transientMemory().uploadGpu(cb, 256, GpuBuffer.USAGE_UNIFORM);
            GpuBufferSlice[] infos = new GpuBufferSlice[n];
            for (int i = 0; i < n; i++) infos[i] = all.slice((long) i * 256, 16);

            int maxSequential = 0;
            for (Section s : visible) {
                for (LayerMesh lm : s.layers.values()) {
                    if (lm.indices == null) maxSequential = Math.max(maxSequential, lm.indexCount);
                }
            }
            RenderSystem.AutoStorageIndexBuffer seq = RenderSystem.getSequentialBuffer(PrimitiveTopology.QUADS);
            GpuBuffer seqBuffer = maxSequential > 0 ? seq.getBuffer(maxSequential) : null;
            IndexType seqType = maxSequential > 0 ? seq.type() : null;

            try (RenderPass pass = encoder.createRenderPass(() -> "Quasar portal terrain " + dimension.identifier(),
                color, Optional.empty(), depth, OptionalDouble.empty())) {
                RenderSystem.bindDefaultUniforms(pass);
                pass.setUniform("TerrainUniform", terrainUniform);
                pass.setUniform("Sampler0", atlasView, atlasSampler);
                pass.setUniform("Sampler2", lightmap, lightSampler);
                for (ChunkSectionLayer layer : ChunkSectionLayer.values()) {
                    List<RenderPass.Draw<GpuBufferSlice[]>> draws = new ArrayList<>();
                    boolean backToFront = layer.translucent();
                    for (int k = 0; k < n; k++) {
                        int i = backToFront ? n - 1 - k : k;
                        LayerMesh lm = visible.get(i).layers.get(layer);
                        if (lm == null) continue;
                        if (lm.indices == null && seqBuffer == null) continue;
                        final int slot = i;
                        draws.add(new RenderPass.Draw<>(0, lm.vertices, lm.indices, lm.indexType, 0, lm.indexCount, 0,
                            (arr, up) -> up.setUniform("ChunkSection", arr[slot])));
                    }
                    if (draws.isEmpty()) continue;
                    pass.setPipeline(RenderSystem.getCompiledPipeline(layer.pipeline(false)));
                    pass.drawMultipleIndexed(draws, seqBuffer, seqType, List.of("ChunkSection"), infos);
                }
            }
            lastSections = n;
            return true;
        }
        catch (Throwable t) {
            fail("drawing", t);
            return false;
        }
    }

    /**
     * Renders this world's lightmap for the portal camera, the way vanilla's LightmapRenderStateExtractor does for
     * the player's world, but with this world's light attributes (Nether: warm ambient, no sky light). Player
     * effects that belong to the viewer (night vision, gamma) carry over; darkness/boss fog do not. Falls back to
     * the main lightmap if anything is missing.
     */
    /** This world's lightmap as last built for a portal camera (null before the first draw). */
    private GpuTextureView lastLightmap;

    /** The lightmap the last {@link #draw} lit this world's terrain with, for its entities in the same view. */
    public GpuTextureView lastLightmap() {
        return lastLightmap;
    }

    private GpuTextureView updateLightmap(Vec3 cam) {
        Minecraft mc = Minecraft.getInstance();
        try {
            if (lightmap == null) lightmap = new net.minecraft.client.renderer.Lightmap();
            var attrs = level.environmentAttributes();
            net.minecraft.client.renderer.state.LightmapRenderState st = lightState;
            st.needsUpdate = true;
            st.blockFactor = 1.4f;
            st.blockLightTint = attrs.getValue(net.minecraft.world.attribute.EnvironmentAttributes.BLOCK_LIGHT_TINT, cam);
            st.skyFactor = attrs.getValue(net.minecraft.world.attribute.EnvironmentAttributes.SKY_LIGHT_FACTOR, cam);
            st.skyLightColor = attrs.getValue(net.minecraft.world.attribute.EnvironmentAttributes.SKY_LIGHT_COLOR, cam);
            st.ambientColor = attrs.getValue(net.minecraft.world.attribute.EnvironmentAttributes.AMBIENT_LIGHT_COLOR, cam);
            st.brightness = mc.options.gamma().get().floatValue();
            st.darknessEffectScale = 0f;
            float nv = 0f;
            if (mc.player != null && mc.player.hasEffect(net.minecraft.world.effect.MobEffects.NIGHT_VISION)) {
                nv = net.minecraft.client.renderer.GameRenderer.nightVisionScale(mc.player,
                    qouteall.imm_ptl.core.render.context_management.RenderStates.getPartialTick());
            }
            st.nightVisionEffectIntensity = nv;
            st.nightVisionColor = attrs.getValue(net.minecraft.world.attribute.EnvironmentAttributes.NIGHT_VISION_COLOR, cam);
            st.bossOverlayWorldDarkening = 0f;
            lightmap.render(st);
            return lightmap.getTextureView();
        }
        catch (Throwable t) {
            if (!lightmapFailed) {
                lightmapFailed = true;
                LOGGER.warn("[Quasar Portals] {} lightmap failed, using the main one: {}", dimension.identifier(), t.toString());
            }
            return mc.gameRenderer.lightmap();
        }
    }

    private boolean lightmapFailed;

    private static double dist2(Section s, Vec3 c) {
        double x = s.sx * 16 + 8 - c.x, y = s.sy * 16 + 8 - c.y, z = s.sz * 16 + 8 - c.z;
        return x * x + y * y + z * z;
    }

    // ------------------------------------------------------------------
    // shared
    // ------------------------------------------------------------------

    /** the main renderer's section compiler (block models, fluid models, colours, AO setting) */
    private static SectionCompiler compiler() {
        try {
            if (compilerField == null) {
                compilerField = SectionRenderDispatcher.class.getDeclaredField("sectionCompiler");
                compilerField.setAccessible(true);
            }
            SectionRenderDispatcher d = Minecraft.getInstance().levelRenderer.sectionRenderDispatcher();
            return d == null ? null : (SectionCompiler) compilerField.get(d);
        }
        catch (Throwable t) {
            fail("finding the section compiler", t);
            return null;
        }
    }

    private static ExecutorService executor() {
        if (executor == null) {
            packs = new ArrayBlockingQueue<>(WORKERS);
            for (int i = 0; i < WORKERS; i++) packs.add(new SectionBufferBuilderPack());
            Method mark = null;
            try {
                mark = Class.forName("com.deds.meshelium.quasar.QuasarPortalHook", true,
                    PortalWorldTerrain.class.getClassLoader()).getMethod("markForeignCompileThread");
            }
            catch (Throwable ignored) {
                // no (or an older) Quasar Meshelium: nothing taps the compiler for another world
            }
            final Method markForeign = mark;
            AtomicInteger n = new AtomicInteger();
            executor = Executors.newFixedThreadPool(WORKERS, r -> {
                Thread t = new Thread(() -> {
                    if (markForeign != null) {
                        try {
                            markForeign.invoke(null);
                        }
                        catch (Throwable e) {
                            LOGGER.warn("[Quasar Portals] could not mark the portal terrain thread for Meshelium: {}", e.toString());
                        }
                    }
                    r.run();
                }, "Quasar Portal Terrain #" + n.incrementAndGet());
                t.setDaemon(true);
                t.setPriority(Thread.NORM_PRIORITY - 1);
                return t;
            });
        }
        return executor;
    }

    private static void fail(String what, Throwable t) {
        if (failed) return;
        failed = true;
        LOGGER.error("[Quasar Portals] other-dimension portal terrain failed ({}); those portals show fog only", what, t);
    }

    // ------------------------------------------------------------------
    // Meshelium's foreign-world registry (its far field must ignore background worlds)
    // ------------------------------------------------------------------

    private static Method registerForeign, unregisterForeign;
    private static boolean registryLookedUp;

    public static void registerForeignLevel(ClientLevel level) {
        callRegistry(true, level);
    }

    public static void unregisterForeignLevel(ClientLevel level) {
        callRegistry(false, level);
    }

    private static void callRegistry(boolean add, ClientLevel level) {
        try {
            if (!registryLookedUp) {
                registryLookedUp = true;
                Class<?> hook = Class.forName("com.deds.meshelium.quasar.QuasarPortalHook", true,
                    PortalWorldTerrain.class.getClassLoader());
                registerForeign = hook.getMethod("registerForeignLevel", Object.class);
                unregisterForeign = hook.getMethod("unregisterForeignLevel", Object.class);
            }
            Method m = add ? registerForeign : unregisterForeign;
            if (m != null) m.invoke(null, level);
        }
        catch (Throwable ignored) {
            // no (or an older) Quasar Meshelium
        }
    }
}
