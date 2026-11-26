package qouteall.imm_ptl.core.chunk_loading;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientChunkCache;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.SectionPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundLevelChunkPacketData;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import qouteall.imm_ptl.core.CHelper;
import qouteall.imm_ptl.core.ClientWorldLoader;
import qouteall.imm_ptl.core.McHelper;
import qouteall.imm_ptl.core.ducks.IEMinecraftClient;
import qouteall.imm_ptl.core.miscellaneous.IPVanillaCopy;
import qouteall.imm_ptl.core.platform_specific.O_O;
import qouteall.q_misc_util.my_util.SignalArged;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Vanilla use a 2D array to store the chunk references on client and cannot store the chunks that are far from player.
 * This use map to store the chunk references, to eliminate such limitation.
 * (Two maps, one for main thread and one for other threads)
 */
//@OnlyIn(Dist.CLIENT)
@IPVanillaCopy
public class ImmPtlClientChunkMap extends ClientChunkCache {
    private static final Logger LOGGER = LogManager.getLogger();
    
    // the most chunk accesses are from the main thread,
    // so we use two maps to reduce synchronization.
    // the main thread accesses this map, without synchronization
    protected final Long2ObjectOpenHashMap<LevelChunk> chunkMapForMainThread =
        new Long2ObjectOpenHashMap<>();
    // other threads read this map, with synchronization
    protected final Long2ObjectOpenHashMap<LevelChunk> chunkMapForOtherThreads =
        new Long2ObjectOpenHashMap<>();
    
    public final Thread mainThread;
    private final ClientLevel ip_level;
    
    public static final SignalArged<LevelChunk> clientChunkLoadSignal = new SignalArged<>();
    public static final SignalArged<LevelChunk> clientChunkUnloadSignal = new SignalArged<>();
    
    public ImmPtlClientChunkMap(ClientLevel clientWorld, int loadDistance) {
        super(clientWorld, 1);
        // the chunk array is unused. make it small by passing 1 as load distance to super constructor
        this.ip_level = clientWorld;
        
        mainThread = ((IEMinecraftClient) Minecraft.getInstance()).ip_getRunningThread();
    }
    
    @Override
    public void drop(ChunkPos chunkPos) {
        Validate.isTrue(Thread.currentThread() == mainThread);
        
//        LOGGER.info("unload {} {}", level, chunkPos);
        
        LevelChunk chunk = chunkMapForMainThread.get(chunkPos.pack());
        if (chunk != null) {
            modifyChunkMap(chunkMap -> {
                chunkMap.remove(chunkPos.pack());
            });
            
            O_O.postClientChunkUnloadEvent(chunk);
            ip_onChunkRemoved(chunk);
            this.level.unload(chunk);
            clientChunkUnloadSignal.emit(chunk);
        }
    }
    
    public <T> T readChunkMap(Function<Long2ObjectOpenHashMap<LevelChunk>, T> func) {
        if (Thread.currentThread() == mainThread) {
            return func.apply(chunkMapForMainThread);
        }
        else {
            synchronized (chunkMapForOtherThreads) {
                return func.apply(chunkMapForOtherThreads);
            }
        }
    }
    
    public void modifyChunkMap(Consumer<Long2ObjectOpenHashMap<LevelChunk>> func) {
        Validate.isTrue(Thread.currentThread() == mainThread);
        func.accept(chunkMapForMainThread);
        synchronized (chunkMapForOtherThreads) {
            func.accept(chunkMapForOtherThreads);
        }
    }
    
    @Override
    public LevelChunk getChunk(int x, int z, ChunkStatus chunkStatus, boolean create) {
        return readChunkMap(chunkMap -> {
            LevelChunk chunk = chunkMap.get(ChunkPos.pack(x, z));
            if (chunk != null) {
                return chunk;
            }
            
            return create ? this.emptyChunk : null;
        });
    }
    
    public boolean isChunkLoaded(int x, int z) {
        return readChunkMap(chunkMap -> {
            return chunkMap.containsKey(ChunkPos.pack(x, z));
        });
    }
    
    @Override
    public void replaceBiomes(int x, int z, FriendlyByteBuf friendlyByteBuf) {
        Validate.isTrue(Thread.currentThread() == mainThread);
        
        long chunkPosLong = ChunkPos.pack(x, z);
        
        LevelChunk worldChunk = chunkMapForMainThread.get(chunkPosLong);
        ChunkPos chunkPos = new ChunkPos(x, z);
        if (worldChunk == null) {
            LOGGER.error("Trying to replace biomes for missing chunk {} {}", x, z);
        }
        else {
            worldChunk.replaceBiomes(friendlyByteBuf);
        }
    }
    
    @Override
    public LevelChunk replaceWithPacketData(int x, int z, ClientboundLevelChunkPacketData data) {
        Validate.isTrue(Thread.currentThread() == mainThread);
        
        long chunkPosLong = ChunkPos.pack(x, z);
        LevelChunk worldChunk = chunkMapForMainThread.get(chunkPosLong);
        if (worldChunk == null) {
            worldChunk = new LevelChunk(this.level, new ChunkPos(x, z));
            loadChunkDataFromPacket(x, z, data, worldChunk);
            
            LevelChunk worldChunkToPut = worldChunk; // lambda can only capture effectively final variables
            modifyChunkMap(chunkMap -> {
                chunkMap.put(chunkPosLong, worldChunkToPut);
            });
            ip_onChunkAdded(worldChunk);
        }
        else {
            loadChunkDataFromPacket(x, z, data, worldChunk);
            ip_refreshEmptySections(worldChunk);
        }
        
        this.level.onChunkLoaded(new ChunkPos(x, z));
        O_O.postClientChunkLoadEvent(worldChunk);
        clientChunkLoadSignal.emit(worldChunk);
        
//        LOGGER.info("load {} {} {}", level, x, z);
        
        return worldChunk;
    }
    
    /**
     * {@link net.minecraft.core.IdMap#byIdOrThrow(int)}
     * {@link net.minecraft.world.level.chunk.LinearPalette#read(FriendlyByteBuf)}
     */
    private void loadChunkDataFromPacket(int x, int z, ClientboundLevelChunkPacketData data, LevelChunk worldChunk) {
        try {
            worldChunk.replaceWithPacketData(x, z, data);
        }
        catch (Exception e) {
            LOGGER.error(
                "Error deserializing chunk packet {} {}",
                worldChunk.getLevel().dimension().identifier(),
                worldChunk.getPos(),
                e
            );
            CHelper.printChat(
                Component
                    .literal("Failed to deserialize chunk packet. %s %s %s".formatted(
                        worldChunk.getLevel().dimension().identifier(),
                        worldChunk.getPos().x(), worldChunk.getPos().z()
                    ))
                    .append(Component.literal(" Report issue:"))
                    .append(McHelper.getLinkText(O_O.getIssueLink()))
                    .withStyle(ChatFormatting.RED)
            );
            
            throw new RuntimeException(e);
        }
    }
    
    public List<LevelChunk> getCopiedChunkList() {
        return readChunkMap((chunkMap) -> {
            return Arrays.asList(chunkMap.values().toArray(new LevelChunk[0]));
        });
    }
    
    @Override
    public void updateViewCenter(int x, int z) {
        // do nothing
    }
    
    @Override
    public void updateViewRadius(int r) {
        // do nothing
    }
    
    @Override
    public String gatherStats() {
        return "Client Chunks (ImmPtl) " + getLoadedChunksCount();
    }
    
    @Override
    public int getLoadedChunksCount() {
        return readChunkMap(chunkMap -> {
            return chunkMap.size();
        });
    }
    
    @Override
    public void onLightUpdate(LightLayer lightType, SectionPos chunkSectionPos) {
        // Quasar: this world's own extractor (a background world's light must not dirty the player's world)
        ((qouteall.imm_ptl.core.ducks.IEClientWorld) ip_level).ip_getLevelExtractor()
            .setSectionDirty(chunkSectionPos.x(), chunkSectionPos.y(), chunkSectionPos.z());
    }
    

    // ---- Quasar MC: 26.x render-tracking sets ----
    // Vanilla's ClientChunkCache.Storage double-buffers which chunks were added/removed and which sections became
    // empty/non-empty; the level extractor reads them each frame and flips. This map bypasses Storage (radius 1),
    // so without these the renderer never learns a chunk loaded: no terrain, and LevelLoadTracker times out.
    // Same logic as Storage.onChunkAdded / onChunkRemoved / refreshEmptySections / markSection*.
    private final LongOpenHashSet[] ip_addedEmptySections = {new LongOpenHashSet(), new LongOpenHashSet()};
    private final LongOpenHashSet[] ip_removedEmptySections = {new LongOpenHashSet(), new LongOpenHashSet()};
    private final LongOpenHashSet[] ip_addedLoadedChunks = {new LongOpenHashSet(), new LongOpenHashSet()};
    private final LongOpenHashSet[] ip_removedLoadedChunks = {new LongOpenHashSet(), new LongOpenHashSet()};
    private int ip_updatingSetsIndex = 0;
    
    @Override
    public LongOpenHashSet addedEmptySections() { return ip_addedEmptySections[ip_updatingSetsIndex]; }
    
    @Override
    public LongOpenHashSet removedEmptySections() { return ip_removedEmptySections[ip_updatingSetsIndex]; }
    
    @Override
    public LongOpenHashSet addedLoadedChunks() { return ip_addedLoadedChunks[ip_updatingSetsIndex]; }
    
    @Override
    public LongOpenHashSet removedLoadedChunks() { return ip_removedLoadedChunks[ip_updatingSetsIndex]; }
    
    @Override
    public void flipUpdateTrackingSets() {
        ip_updatingSetsIndex = (ip_updatingSetsIndex + 1) % 2;
        ip_addedEmptySections[ip_updatingSetsIndex].clear();
        ip_removedEmptySections[ip_updatingSetsIndex].clear();
        ip_addedLoadedChunks[ip_updatingSetsIndex].clear();
        ip_removedLoadedChunks[ip_updatingSetsIndex].clear();
    }
    
    @Override
    public void onSectionEmptinessChanged(int x, int y, int z, boolean empty) {
        if (!isChunkLoaded(x, z)) {
            return;
        }
        long key = SectionPos.asLong(x, y, z);
        if (empty) ip_markSectionEmpty(key);
        else ip_markSectionNotEmpty(key);
    }
    
    private void ip_markSectionEmpty(long key) {
        ip_removedEmptySections[ip_updatingSetsIndex].remove(key);
        ip_addedEmptySections[ip_updatingSetsIndex].add(key);
    }
    
    private void ip_markSectionNotEmpty(long key) {
        ip_addedEmptySections[ip_updatingSetsIndex].remove(key);
        ip_removedEmptySections[ip_updatingSetsIndex].add(key);
    }
    
    private void ip_onChunkAdded(LevelChunk chunk) {
        long pos = chunk.getPos().pack();
        ip_removedLoadedChunks[ip_updatingSetsIndex].remove(pos);
        ip_addedLoadedChunks[ip_updatingSetsIndex].add(pos);
        ip_refreshEmptySections(chunk);
    }
    
    private void ip_onChunkRemoved(LevelChunk chunk) {
        ChunkPos cp = chunk.getPos();
        long pos = cp.pack();
        ip_addedLoadedChunks[ip_updatingSetsIndex].remove(pos);
        ip_removedLoadedChunks[ip_updatingSetsIndex].add(pos);
        LevelChunkSection[] sections = chunk.getSections();
        for (int i = 0; i < sections.length; i++) {
            ip_markSectionEmpty(SectionPos.asLong(cp.x(), chunk.getSectionYFromSectionIndex(i), cp.z()));
        }
    }
    
    /**
     * Quasar (seamless crossing): announce every loaded chunk and section emptiness again. The main extractor's
     * {@code allChanged} starts a fresh SectionUpdateTracker that only learns about chunks from these sets; a world
     * entered through a portal already has its chunks, announced earlier (or to its background extractor, which
     * never reads them), so without this the renderer thinks nothing is loaded (F3 {@code C: 0/...}).
     */
    public void ip_reannounceAllChunks() {
        int n = 0;
        for (LevelChunk chunk : getCopiedChunkList()) {
            ip_onChunkAdded(chunk);
            n++;
        }
        LOGGER.info("[Quasar Portals] re-announced {} loaded chunks to the renderer", n);
    }
    
    private void ip_refreshEmptySections(LevelChunk chunk) {
        ChunkPos cp = chunk.getPos();
        LevelChunkSection[] sections = chunk.getSections();
        for (int i = 0; i < sections.length; i++) {
            long key = SectionPos.asLong(cp.x(), chunk.getSectionYFromSectionIndex(i), cp.z());
            if (sections[i].hasOnlyAir()) ip_markSectionEmpty(key);
            else ip_markSectionNotEmpty(key);
        }
    }
}
