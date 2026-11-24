package qouteall.imm_ptl.core.mixin.client.accessor;

import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/** Quasar MC: the frame's level render state (its sky state is reused to draw the sky into portal views). */
@Mixin(LevelRenderer.class)
public interface IELevelRenderer_QuasarSky {
    @Accessor("levelRenderState")
    LevelRenderState ip_getLevelRenderState();
}
