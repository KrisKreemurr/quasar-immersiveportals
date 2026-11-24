package qouteall.imm_ptl.core.mixin.client;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.renderpearl.api.textures.GpuTextureView;
import net.minecraft.client.renderer.SkyRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import qouteall.imm_ptl.core.render.quasar.PortalSky;

/**
 * Quasar MC: lets the portal renderer draw vanilla's sky into a portal view's image.
 * SkyRenderer always renders into the main render target; while {@link PortalSky} has a target set,
 * the colour/depth views it opens its pass on are swapped for the portal view's.
 */
@Mixin(SkyRenderer.class)
public class MixinSkyRenderer_QuasarPortal {
    @Redirect(method = "render", at = @At(value = "INVOKE",
        target = "Lcom/mojang/blaze3d/pipeline/RenderTarget;getColorTextureView()Lcom/mojang/renderpearl/api/textures/GpuTextureView;"))
    private GpuTextureView ip_portalColor(RenderTarget target) {
        GpuTextureView v = PortalSky.overrideColor;
        return v != null ? v : target.getColorTextureView();
    }
    
    @Redirect(method = "render", at = @At(value = "INVOKE",
        target = "Lcom/mojang/blaze3d/pipeline/RenderTarget;getDepthTextureView()Lcom/mojang/renderpearl/api/textures/GpuTextureView;"))
    private GpuTextureView ip_portalDepth(RenderTarget target) {
        GpuTextureView v = PortalSky.overrideDepth;
        return v != null ? v : target.getDepthTextureView();
    }
}
