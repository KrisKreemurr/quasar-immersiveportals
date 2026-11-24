// QUASAR STUB: real code parked in src/parked (rendering is replaced for Quasar MC).
package qouteall.imm_ptl.core.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import qouteall.imm_ptl.core.portal.LoadingIndicatorEntity;

// QUASAR STUB: generated no-op stand-in; the original lives in src/parked (render rewrite pending).

public class LoadingIndicatorRenderer extends EntityRenderer<LoadingIndicatorEntity> {

    public LoadingIndicatorRenderer(EntityRendererProvider.Context context) { super(context); }

    public ResourceLocation getTextureLocation(LoadingIndicatorEntity var1) { return null; }

    public void render(
        LoadingIndicatorEntity entity_1,
        float float_1,
        float float_2,
        PoseStack matrixStack_1,
        MultiBufferSource vertexConsumerProvider_1,
        int int_1
    ) {}

    protected LoadingIndicatorRenderer() {}

}

