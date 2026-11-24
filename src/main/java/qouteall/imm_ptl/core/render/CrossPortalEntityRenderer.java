// QUASAR STUB: real code parked in src/parked (rendering is replaced for Quasar MC).
package qouteall.imm_ptl.core.render;

import com.mojang.blaze3d.vertex.PoseStack;
import de.nick1st.imm_ptl.events.ClientCleanupEvent;
import de.nick1st.imm_ptl.events.DimensionEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.common.NeoForge;
import org.apache.commons.lang3.Validate;
import org.joml.Matrix4f;
import qouteall.imm_ptl.core.CHelper;
import qouteall.imm_ptl.core.ClientWorldLoader;
import qouteall.imm_ptl.core.IPGlobal;
import qouteall.imm_ptl.core.McHelper;
import qouteall.imm_ptl.core.collision.PortalCollisionEntry;
import qouteall.imm_ptl.core.collision.PortalCollisionHandler;
import qouteall.imm_ptl.core.compat.iris_compatibility.IrisInterface;
import qouteall.imm_ptl.core.ducks.IEEntity;
import qouteall.imm_ptl.core.ducks.IEWorldRenderer;
import qouteall.imm_ptl.core.portal.Mirror;
import qouteall.imm_ptl.core.portal.Portal;
import qouteall.imm_ptl.core.portal.PortalManipulation;
import qouteall.imm_ptl.core.render.context_management.PortalRendering;
import qouteall.imm_ptl.core.render.context_management.RenderStates;
import qouteall.imm_ptl.core.render.context_management.WorldRenderInfo;
import qouteall.imm_ptl.core.render.renderer.PortalRenderer;
import qouteall.q_misc_util.Helper;
import qouteall.q_misc_util.my_util.Plane;
import java.util.WeakHashMap;

// QUASAR STUB: generated no-op stand-in; the original lives in src/parked (render rewrite pending).

@OnlyIn(Dist.CLIENT)
public class CrossPortalEntityRenderer {

    private static Minecraft client;
    private static WeakHashMap<Entity, Object> collidedEntities;
    public static boolean isRenderingEntityNormally;
    public static boolean isRenderingEntityProjection;
    public static void init() {}

    private static void cleanUp() {}

    private static void onClientTick() {}

    public static void onEntityTickClient(Entity entity) {}

    public static void onBeginRenderingEntitiesAndBlockEntities(Matrix4f modelView) {}

    private static boolean isCrossPortalRenderingEnabled() { return false; }

    public static void onEndRenderingEntitiesAndBlockEntities(PoseStack matrixStack) {}

    public static void beforeRenderingEntity(Entity entity, PoseStack matrixStack) {}

    public static void afterRenderingEntity(Entity entity) {}

    private static void renderEntityProjections(PoseStack matrixStack) {}

    public static boolean hasIntersection(
        Vec3 outerPlanePos, Vec3 outerPlaneNormal,
        Vec3 entityPos, Vec3 collidingPortalNormal
    ) { return false; }

    private static void renderProjectedEntity(
        Entity entity,
        Portal collidingPortal,
        PoseStack matrixStack
    ) {}

    private static void renderEntity(
        Entity entity,
        Portal transformingPortal,
        PoseStack matrixStack
    ) {}

    private static void setupEntityProjectionRenderingTransformation(
        Portal portal, PoseStack matrixStack,
        Vec3 entityPos, Vec3 entityLastTickPos, Vec3 cameraPos
    ) {}

    public static boolean shouldRenderPlayerDefault() { return false; }

    public static boolean shouldRenderEntityNow(Entity entity) { return false; }

    public static boolean shouldRenderPlayerNormally(Entity entity) { return false; }

    public static Vec3 getRenderingCameraPos(Entity entity) { return null; }

}

