package qouteall.imm_ptl.core.mixin.client;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import qouteall.imm_ptl.core.ducks.IEAbstractClientPlayer;

@Mixin(AbstractClientPlayer.class)
public class MixinAbstractClientPlayer implements IEAbstractClientPlayer {
    @Override
    public void ip_setClientLevel(ClientLevel clientWorld) {
        // Quasar MC: 26.x players have no separate clientLevel field (cross-dimension portals are not ported yet).
    }
}
