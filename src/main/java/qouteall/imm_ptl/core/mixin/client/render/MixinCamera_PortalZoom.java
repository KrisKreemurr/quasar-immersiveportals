package qouteall.imm_ptl.core.mixin.client.render;

import net.minecraft.client.Camera;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3fc;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import qouteall.imm_ptl.core.portal.Mirror;
import qouteall.imm_ptl.core.portal.Portal;

/**
 * Quasar MC: the third-person camera stops at a portal's surface like at a wall. Pulled back through a portal it would
 * really be in the other world, looking at you from there - geometrically right, but it looks broken (looking down at
 * the frame you see the other world and nothing of yourself).
 */
@Mixin(Camera.class)
public abstract class MixinCamera_PortalZoom {
    @Inject(method = "getMaxZoom(F)F", at = @At("RETURN"), cancellable = true, require = 0)
    private void ip_stopAtPortal(float wanted, CallbackInfoReturnable<Float> cir) {
        Camera self = (Camera) (Object) this;
        Entity owner = self.entity();
        if (owner == null) return;
        float dist = cir.getReturnValueF();
        if (dist <= 0f) return;
        Vec3 start = self.position();
        Vector3fc f = self.forwardVector();
        Vec3 end = start.subtract(f.x() * dist, f.y() * dist, f.z() * dist);
        float best = dist;
        for (Portal portal : owner.level().getEntitiesOfClass(Portal.class, new AABB(start, end).inflate(1.0))) {
            if (portal instanceof Mirror || portal.isRemoved()) continue;
            double ds = portal.getDistanceToPlane(start), de = portal.getDistanceToPlane(end);
            if (ds <= 0 || de >= 0) continue;
            double t = ds / (ds - de);
            Vec3 hit = start.add(end.subtract(start).scale(t)).subtract(portal.getOriginPos());
            if (Math.abs(hit.dot(portal.getAxisW())) > portal.getWidth() / 2
                || Math.abs(hit.dot(portal.getAxisH())) > portal.getHeight() / 2) continue;
            // stop a little short of the surface (the near plane must not reach it either)
            best = (float) Math.min(best, Math.max(0.0, dist * t - 0.15));
        }
        if (best < dist) cir.setReturnValue(best);
    }
}
