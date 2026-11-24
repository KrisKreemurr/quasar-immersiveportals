package qouteall.imm_ptl.core.mixin.client.accessor;

import net.minecraft.client.renderer.CloudRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/** Quasar MC: the cloud mesh's cell and texture size, to place the portal camera in the main view's cloud mesh. */
@Mixin(CloudRenderer.class)
public interface IECloudRenderer_Quasar {
    @Accessor("texture")
    CloudRenderer.TextureData ip_texture();
    
    @Accessor("prevCellX")
    int ip_prevCellX();
    
    @Accessor("prevCellZ")
    int ip_prevCellZ();
    
    @Accessor("quadCount")
    int ip_quadCount();
}
