package qouteall.imm_ptl.core.render.quasar;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.extract.LevelExtractor;
import net.minecraft.client.renderer.extract.TransientBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Quasar MC: the render-side listener of a background (secondary) client world, e.g. the Nether
 * held while the player is in the Overworld.
 *
 * In 26.x a ClientLevel reports block and section changes to a {@link LevelExtractor}, and Minecraft
 * has exactly one, bound to the player's world. A background world must not poke that one (it would
 * mark the player's world's sections dirty at the other world's coordinates), so it gets this one:
 * it never extracts anything for the main frame, it only records which sections changed so the
 * portal terrain for that world (cross-dimension step 3) knows what to rebuild.
 */
public final class SecondaryLevelExtractor extends LevelExtractor {
    private final LongOpenHashSet dirtySections = new LongOpenHashSet();
    private boolean allDirty = true;

    public SecondaryLevelExtractor(Minecraft mc) {
        // The base constructor only stores these; every method that would use them is overridden.
        super(mc, null, mc.levelRenderer);
    }

    /** Sections changed since the last call (SectionPos longs); null means "everything". Clears the record. */
    public LongOpenHashSet drainDirtySections() {
        if (allDirty) {
            allDirty = false;
            dirtySections.clear();
            return null;
        }
        LongOpenHashSet out = new LongOpenHashSet(dirtySections);
        dirtySections.clear();
        return out;
    }

    private void mark(int sx, int sy, int sz) {
        if (!allDirty) dirtySections.add(SectionPos.asLong(sx, sy, sz));
    }

    // ---- what the background world reports ----

    @Override
    public void blockChanged(BlockPos pos, int flags) {
        mark(SectionPos.blockToSectionCoord(pos.getX()), SectionPos.blockToSectionCoord(pos.getY()),
                SectionPos.blockToSectionCoord(pos.getZ()));
    }

    @Override
    public void setBlockDirty(BlockPos pos, BlockState oldState, BlockState newState) {
        blockChanged(pos, 0);
    }

    @Override
    public void setBlocksDirty(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        setSectionRangeDirty(SectionPos.blockToSectionCoord(minX), SectionPos.blockToSectionCoord(minY),
                SectionPos.blockToSectionCoord(minZ), SectionPos.blockToSectionCoord(maxX),
                SectionPos.blockToSectionCoord(maxY), SectionPos.blockToSectionCoord(maxZ));
    }

    @Override
    public void setSectionDirtyWithNeighbors(int sx, int sy, int sz) {
        setSectionRangeDirty(sx - 1, sy - 1, sz - 1, sx + 1, sy + 1, sz + 1);
    }

    @Override
    public void setSectionRangeDirty(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        if (allDirty) return;
        for (int x = minX; x <= maxX; x++)
            for (int y = minY; y <= maxY; y++)
                for (int z = minZ; z <= maxZ; z++) mark(x, y, z);
    }

    @Override
    public void setSectionDirty(int sx, int sy, int sz) {
        mark(sx, sy, sz);
    }

    @Override
    public void allChanged() {
        allDirty = true;
        dirtySections.clear();
    }

    // ---- main-frame work this world never does ----

    @Override
    public void setLevel(ClientLevel level) {}

    @Override
    public void extract(DeltaTracker deltaTracker, Camera camera, float partialTick) {}

    @Override
    public boolean isEntityVisible(Entity entity, Frustum frustum, double x, double y, double z, float partialTick, long time) {
        return false;
    }

    @Override
    public void queueTransientBlock(TransientBlock block) {}

    @Override
    public void resetSampler() {}

    @Override
    public void onResourceManagerReload(ResourceManager resourceManager) {
        allChanged();
    }
}
