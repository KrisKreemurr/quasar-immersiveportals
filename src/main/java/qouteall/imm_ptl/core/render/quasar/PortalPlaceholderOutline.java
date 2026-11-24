package qouteall.imm_ptl.core.render.quasar;

import net.minecraft.client.Minecraft;
import net.minecraft.gizmos.GizmoStyle;
import net.minecraft.gizmos.Gizmos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.RenderFrameEvent;
import net.neoforged.neoforge.common.NeoForge;
import qouteall.imm_ptl.core.portal.Portal;

/**
 * Quasar MC: a stand-in so portals can be seen and walked into while the real portal rendering (Meshelium near view +
 * panorama far view) is being built. Each visible portal in the player's world is drawn as a translucent purple pane
 * with a bright outline, using vanilla world gizmos (RenderFrameEvent.Pre runs inside the per-frame gizmo collector).
 * Cost: one filled rect + four lines per portal within 128 blocks. Remove when the real renderer lands.
 */
public final class PortalPlaceholderOutline {
    private static final double MAX_DIST_SQ = 128 * 128;
    private static final int FILL = 0x40A040FF;     // ~25% alpha purple
    private static final int EDGE = 0xFFD080FF;     // opaque light purple
    
    private PortalPlaceholderOutline() {}
    
    public static void init() {
        NeoForge.EVENT_BUS.addListener(PortalPlaceholderOutline::onFrame);
    }
    
    /** Video Settings → Portals → Portal Outlines (saved in quasar-portals.properties) */
    public static volatile boolean enabled = true;

    private static void onFrame(RenderFrameEvent.Pre event) {
        if (!enabled) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) {
            return;
        }
        Vec3 cam = mc.gameRenderer.mainCamera().position();
        for (Entity entity : mc.level.entitiesForRendering()) {
            if (!(entity instanceof Portal portal) || entity.isRemoved()) {
                continue;
            }
            if (PortalViewRenderer.drawnLastFrame.contains(portal.getId())) {
                continue; // shown as a live view
            }
            Vec3 o = portal.getOriginPos();
            if (o.distanceToSqr(cam) > MAX_DIST_SQ) {
                continue;
            }
            Vec3 w = portal.getAxisW().scale(portal.getWidth() / 2);
            Vec3 h = portal.getAxisH().scale(portal.getHeight() / 2);
            Vec3 a = o.subtract(w).subtract(h);
            Vec3 b = o.add(w).subtract(h);
            Vec3 c = o.add(w).add(h);
            Vec3 d = o.subtract(w).add(h);
            // both windings, so the pane shows from either side
            Gizmos.rect(a, b, c, d, GizmoStyle.fill(FILL));
            Gizmos.rect(d, c, b, a, GizmoStyle.fill(FILL));
            Gizmos.line(a, b, EDGE, 3f);
            Gizmos.line(b, c, EDGE, 3f);
            Gizmos.line(c, d, EDGE, 3f);
            Gizmos.line(d, a, EDGE, 3f);
        }
    }
}
