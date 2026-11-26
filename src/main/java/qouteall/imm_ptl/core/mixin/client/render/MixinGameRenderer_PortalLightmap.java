package qouteall.imm_ptl.core.mixin.client.render;

import com.mojang.renderpearl.api.textures.GpuTextureView;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import qouteall.imm_ptl.core.render.quasar.PortalEntities;

/**
 * Quasar MC: while a cross-dimension portal view draws the destination world's entities, block entities and
 * particles, they are lit with that world's lightmap ({@link PortalEntities#lightmapOverride()}).
 */
@Mixin(GameRenderer.class)
public class MixinGameRenderer_PortalLightmap {
    @Inject(method = "lightmap", at = @At("HEAD"), cancellable = true)
    private void quasar$portalLightmap(CallbackInfoReturnable<GpuTextureView> cir) {
        GpuTextureView override = PortalEntities.lightmapOverride();
        if (override != null) cir.setReturnValue(override);
    }
}
