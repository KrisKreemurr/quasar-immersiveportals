package qouteall.imm_ptl.core.mixin.client;

import com.mojang.renderpearl.api.buffers.GpuBuffer;
import com.mojang.renderpearl.api.buffers.GpuBufferSlice;
import com.mojang.renderpearl.api.commands.RenderPass;
import net.minecraft.client.renderer.CloudRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import qouteall.imm_ptl.core.render.quasar.PortalClouds;

/**
 * Quasar MC: while a portal view draws clouds, the CloudInfo uniform (colour + camera offset into the cloud
 * mesh) is replaced by one computed for the portal camera, so the main view's mesh is reused as is.
 */
@Mixin(CloudRenderer.class)
public class MixinCloudRenderer_QuasarPortal {
    @Redirect(method = "render(Lcom/mojang/renderpearl/api/commands/RenderPass;Lcom/mojang/renderpearl/api/pipeline/RenderPipeline;)V",
        at = @At(value = "INVOKE",
            target = "Lcom/mojang/renderpearl/api/commands/RenderPass;setUniform(Ljava/lang/String;Lcom/mojang/renderpearl/api/buffers/GpuBuffer;)V"))
    private void ip_portalCloudInfo(RenderPass pass, String name, GpuBuffer buffer) {
        GpuBufferSlice o = PortalClouds.overrideInfo;
        if (o != null && "CloudInfo".equals(name)) {
            pass.setUniform(name, o);
        }
        else {
            pass.setUniform(name, buffer);
        }
    }
}
