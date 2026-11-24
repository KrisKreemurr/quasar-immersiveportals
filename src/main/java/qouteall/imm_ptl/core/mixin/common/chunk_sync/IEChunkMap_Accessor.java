package qouteall.imm_ptl.core.mixin.common.chunk_sync;

import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ChunkMap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ChunkMap.class)
public interface IEChunkMap_Accessor {
    @org.spongepowered.asm.mixin.gen.Accessor("visibleChunkMap")
    it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap<ChunkHolder> ip_getVisibleChunkMap();
}
