package qouteall.imm_ptl.core.mixin.common;

import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.entity.PersistentEntitySectionManager;
import net.minecraft.world.level.storage.ServerLevelData;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import qouteall.imm_ptl.core.chunk_loading.ImmPtlChunkTracking;
import qouteall.imm_ptl.core.ducks.IEEntity;
import qouteall.imm_ptl.core.ducks.IEServerWorld;

import java.util.List;

@Mixin(ServerLevel.class)
public abstract class MixinServerLevel implements IEServerWorld {
    
    @Shadow
    public abstract ServerChunkCache getChunkSource();
    
    @Shadow
    @Final
    private ServerLevelData serverLevelData;
    
    @Shadow
    @Final
    private PersistentEntitySectionManager<Entity> entityManager;
    
    // Quasar MC: 26.x keeps a dimension ticking through ticket flags; Immersive Portals' ticket type carries
    // FLAG_KEEP_DIMENSION_ACTIVE (ImmPtlChunkTickets), replacing the old players.isEmpty() redirect.
    
    // for debug
    @Inject(method = "Lnet/minecraft/server/level/ServerLevel;toString()Ljava/lang/String;", at = @At("HEAD"), cancellable = true)
    private void onToString(CallbackInfoReturnable<String> cir) {
        final ServerLevel this_ = (ServerLevel) (Object) this;
        cir.setReturnValue("ServerWorld " + this_.dimension().identifier() +
            " " + serverLevelData.getLevelName());
    }
    
    @Inject(
        method = "tickNonPassenger",
        at = @At("HEAD")
    )
    private void onTickNonPassenger(Entity entity, CallbackInfo ci) {
        // this should be done right before setting last tick pos to this tick pos
        ((IEEntity) entity).ip_tickCollidingPortal();
    }
    
    @Override
    public PersistentEntitySectionManager<Entity> ip_getEntityManager() {
        return entityManager;
    }
}
