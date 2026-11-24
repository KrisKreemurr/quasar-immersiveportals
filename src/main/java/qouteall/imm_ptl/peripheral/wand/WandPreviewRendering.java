package qouteall.imm_ptl.peripheral.wand;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.RenderFrameEvent;
import net.neoforged.neoforge.common.NeoForge;
import qouteall.imm_ptl.core.mc_utils.WireBuffers;

/**
 * Quasar MC: draws the portal wand's previews (corner markers, wire outlines, the portal being made or dragged) as
 * world gizmos while the wand is in the main hand. Replaces the original mixin into vanilla's DebugRenderer, which no
 * longer exists in that form; RenderFrameEvent.Pre runs while the frame's gizmos are collected.
 */
public final class WandPreviewRendering {
    private WandPreviewRendering() {}
    
    public static void init() {
        NeoForge.EVENT_BUS.addListener(WandPreviewRendering::onFrame);
        // the wand's on-screen instructions
        NeoForge.EVENT_BUS.addListener((net.neoforged.neoforge.client.event.RenderGuiEvent.Post e) ->
            qouteall.q_misc_util.CustomTextOverlay.render(e.getGuiGraphics()));
        // left click with the wand (was a mixin into Minecraft.startAttack): wand action instead of attacking
        NeoForge.EVENT_BUS.addListener((net.neoforged.neoforge.client.event.InputEvent.InteractionKeyMappingTriggered e) -> {
            if (!e.isAttack()) return;
            LocalPlayer player = Minecraft.getInstance().player;
            if (player == null || PortalWandItem.instance == null) return;
            ItemStack stack = player.getMainHandItem();
            if (stack.getItem() != PortalWandItem.instance) return;
            PortalWandItemClient.onClientLeftClick(player, stack);
            e.setSwingHand(false);
            e.setCanceled(true);
        });
    }
    
    private static void onFrame(RenderFrameEvent.Pre event) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || mc.level == null || PortalWandItem.instance == null) {
            return;
        }
        ItemStack itemStack = player.getMainHandItem();
        if (itemStack.getItem() != PortalWandItem.instance) {
            return;
        }
        Vec3 cam = mc.gameRenderer.mainCamera().position();
        PortalWandItem.clientRender(player, itemStack, new PoseStack(), new WireBuffers(cam), cam.x, cam.y, cam.z);
    }
}
