package qouteall.imm_ptl.core.mixin.common.entity_sync;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Mob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import qouteall.imm_ptl.core.chunk_loading.ImmPtlChunkTracking;

import java.util.Map;

/**
 * Quasar MC: a mob seen through a portal from another dimension counts as having a player nearby.
 *
 * <p>Vanilla only resets a mob's idle timer ({@code noActionTime}) in {@code checkDespawn} when a player in the same
 * dimension is within 32 blocks. With nobody in its dimension the timer grows every tick, and past 100 ticks
 * {@code RandomStrollGoal} stops: mobs in a portal-loaded area stood still (looking around only) while the player
 * watched them through the portal. Here a player in another dimension whose portal view covers the mob's chunk within
 * two chunks of the portal's far side resets the timer too.</p>
 */
@Mixin(Mob.class)
public abstract class MixinMob_PortalWatchers {
    @Inject(method = "checkDespawn", at = @At("TAIL"))
    private void quasar$portalWatcherKeepsActive(CallbackInfo ci) {
        Mob self = (Mob) (Object) this;
        if (self.level().isClientSide() || self.getNoActionTime() == 0) return;
        Map<ServerPlayer, ImmPtlChunkTracking.PlayerWatchRecord> watchers = ImmPtlChunkTracking.getWatchRecordForChunk(
            self.level().dimension(), self.chunkPosition().x(), self.chunkPosition().z());
        if (watchers == null || watchers.isEmpty()) return;
        for (Map.Entry<ServerPlayer, ImmPtlChunkTracking.PlayerWatchRecord> e : watchers.entrySet()) {
            ImmPtlChunkTracking.PlayerWatchRecord rec = e.getValue();
            if (rec != null && rec.isLoadedToPlayer && rec.distanceToSource <= 2
                && e.getKey().level() != self.level()) {
                self.setNoActionTime(0);
                return;
            }
        }
    }
}
