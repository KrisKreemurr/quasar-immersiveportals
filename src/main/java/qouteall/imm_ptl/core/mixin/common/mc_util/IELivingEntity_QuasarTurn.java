package qouteall.imm_ptl.core.mixin.common.mc_util;

import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

/** Quasar MC: head/body turning of background-world mobs without a full tick (ClientWorldLoader.stepRemoteEntities). */
@Mixin(LivingEntity.class)
public interface IELivingEntity_QuasarTurn {
    @Accessor("lerpHeadSteps")
    int quasar$getLerpHeadSteps();

    @Accessor("lerpHeadSteps")
    void quasar$setLerpHeadSteps(int steps);

    @Accessor("lerpYHeadRot")
    double quasar$getLerpYHeadRot();

    @Invoker("lerpHeadRotationStep")
    void quasar$lerpHeadRotationStep(int steps, double target);

    @Invoker("tickHeadTurn")
    void quasar$tickHeadTurn(float yBodyRotTarget);
}
