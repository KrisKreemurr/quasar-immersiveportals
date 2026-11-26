package qouteall.imm_ptl.core.mixin.client.render;

import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import qouteall.imm_ptl.core.render.quasar.PortalEntities;

/** Quasar MC: entities in a cross-dimension doorway, seen from behind the face they walk into, are not drawn. */
@Mixin(EntityRenderDispatcher.class)
public class MixinEntityRenderDispatcher_Doorway {
    @Inject(method = "shouldRender", at = @At("HEAD"), cancellable = true, require = 0)
    private void ip_hideBeyondDoorway(Entity entity, Frustum frustum, double camX, double camY, double camZ,
                                      float partialTick, CallbackInfoReturnable<Boolean> cir) {
        if (PortalEntities.hiddenInMainView(entity, camX, camY, camZ)) cir.setReturnValue(false);
    }
}
