package qouteall.imm_ptl.core.mixin.client.particle;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import qouteall.imm_ptl.core.render.quasar.PortalParticles;

/**
 * Quasar MC: a background world's particles (portal views into another dimension) go to that world's own particle
 * engine, not the player's world's ({@link PortalParticles}), and its particle distance check measures from where
 * the player would be in that world instead of from the main camera.
 */
@Mixin(ClientLevel.class)
public abstract class MixinClientLevel_Particles {
    @Redirect(
        method = "*",
        at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/client/Minecraft;particleEngine:Lnet/minecraft/client/particle/ParticleEngine;",
            opcode = org.objectweb.asm.Opcodes.GETFIELD
        ),
        require = 0
    )
    private ParticleEngine quasar$particleEngine(Minecraft mc) {
        ParticleEngine own = PortalParticles.engineFor((ClientLevel) (Object) this);
        return own != null ? own : mc.particleEngine;
    }

    @Redirect(
        method = "doAddParticle",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Camera;position()Lnet/minecraft/world/phys/Vec3;"),
        require = 0
    )
    private Vec3 quasar$particleDistanceFrom(Camera camera) {
        ClientLevel self = (ClientLevel) (Object) this;
        if (PortalParticles.isBackground(self)) {
            Vec3 observer = PortalParticles.observer(self);
            if (observer != null) return observer;
        }
        return camera.position();
    }
}
