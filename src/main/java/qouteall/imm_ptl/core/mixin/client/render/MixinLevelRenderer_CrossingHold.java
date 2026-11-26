package qouteall.imm_ptl.core.mixin.client.render;

import net.minecraft.client.renderer.LevelRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import qouteall.imm_ptl.core.render.quasar.PortalViewRenderer;

/**
 * Quasar MC: right after the whole world pass (terrain, entities, translucents, particles, weather, Distant Horizons),
 * before the hand and HUD. Just after crossing a portal the main renderer is still switching worlds (old terrain for
 * a moment, then nothing); the crossing hold draws the new world as the portal showed it over all of that.
 */
@Mixin(LevelRenderer.class)
public class MixinLevelRenderer_CrossingHold {
    @Inject(
        method = "render(Lcom/mojang/blaze3d/resource/GraphicsResourceAllocator;ZLnet/minecraft/client/renderer/state/level/CameraRenderState;Lcom/mojang/renderpearl/api/buffers/GpuBufferSlice;Lorg/joml/Vector4f;ZZ)V",
        at = @At("TAIL"),
        require = 0
    )
    private void ip_crossingHold(CallbackInfo ci) {
        PortalViewRenderer.drawCrossingHold();
    }
}
