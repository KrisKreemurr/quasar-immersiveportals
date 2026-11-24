package qouteall.imm_ptl.core.mixin.client;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.neoforged.neoforge.common.NeoForge;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import qouteall.imm_ptl.core.IPGlobal;
import qouteall.imm_ptl.core.portal.animation.ClientPortalAnimationManagement;
import qouteall.imm_ptl.core.portal.animation.StableClientTimer;
import qouteall.imm_ptl.core.render.context_management.RenderStates;
import qouteall.imm_ptl.core.teleportation.ClientTeleportationManager;

/**
 * Quasar MC: the non-rendering half of the original MixinGameRenderer's per-frame hook (which is parked with
 * the renderer). It keeps the camera/player snapshot current, advances portal animation and runs the per-frame
 * teleport check, so crossing a portal is checked every frame rather than every tick.
 * 26.x splits a frame into extract (with the DeltaTracker) and render; this runs at the start of extract.
 */
@Mixin(GameRenderer.class)
public class MixinGameRenderer_PreRender {
    @Shadow @Final private Minecraft minecraft;
    
    @Inject(method = "extract(Lnet/minecraft/client/DeltaTracker;Z)V", at = @At("HEAD"))
    private void ip_onPreRender(DeltaTracker deltaTracker, boolean advanceGameTime, CallbackInfo ci) {
        net.minecraft.util.profiling.Profiler.get().push("ip_pre_total_render");
        IPGlobal.PRE_TOTAL_RENDER_TASK_LIST.processTasks();
        net.minecraft.util.profiling.Profiler.get().pop();
        if (minecraft.level == null) {
            return;
        }
        net.minecraft.util.profiling.Profiler.get().push("ip_pre_render");
        float partialTick = deltaTracker.getGameTimeDeltaPartialTick(true);
        RenderStates.updatePreRenderInfo(partialTick);
        StableClientTimer.update(minecraft.level.getGameTime(), partialTick);
        ClientPortalAnimationManagement.update(); // must update before teleportation
        ClientTeleportationManager.manageTeleportation(false);
        NeoForge.EVENT_BUS.post(new IPGlobal.PreGameRenderEvent());
        net.minecraft.util.profiling.Profiler.get().pop();
        RenderStates.frameIndex++;
    }
}
