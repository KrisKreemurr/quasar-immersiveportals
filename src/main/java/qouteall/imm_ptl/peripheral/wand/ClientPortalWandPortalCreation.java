// QUASAR STUB: parked peripheral feature (dimension stack / alternate dimensions / portal wand / command stick), see src/parked.
package qouteall.imm_ptl.peripheral.wand;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import qouteall.imm_ptl.core.mc_utils.WireRenderingHelper;
import qouteall.imm_ptl.core.platform_specific.IPConfig;
import qouteall.imm_ptl.core.portal.animation.TimingFunction;
import qouteall.imm_ptl.core.render.context_management.RenderStates;
import qouteall.q_misc_util.CustomTextOverlay;
import qouteall.q_misc_util.Helper;
import qouteall.q_misc_util.api.McRemoteProcedureCall;
import qouteall.q_misc_util.api.McRemoteProcedureCallClient;
import qouteall.q_misc_util.my_util.Circle;
import qouteall.q_misc_util.my_util.Plane;
import qouteall.q_misc_util.my_util.WithDim;
import qouteall.q_misc_util.my_util.animation.Animated;
import qouteall.q_misc_util.my_util.animation.RenderedPlane;

// QUASAR STUB: generated no-op stand-in; the original lives in src/parked (render rewrite pending).

public class ClientPortalWandPortalCreation {

    private static Logger LOGGER;
    public static Animated<Vec3> cursor;
    public static Animated<RenderedPlane> renderedPlane;
    public static ProtoPortal protoPortal;
    public static void reset() {}

    public static void onLeftClick() {}

    private static void undo() {}

    public static void onRightClick() {}

    public static void clearCursorPointing() {}

    public static void updateDisplay() {}

    public static void finish() {}

    private static int colorOfFirstSideLeftBottom;
    private static int colorOfFirstSideRightBottom;
    private static int colorOfFirstSideLeftUp;
    private static int colorOfSecondSideLeftBottom;
    private static int colorOfSecondSideRightBottom;
    private static int colorOfSecondSideLeftUp;
    private static int colorOfPlane;
    private static int colorOfCircle;
    private static int colorOfFirstPortalArea;
    private static int colorOfFirstPortalArea2;
    private static int colorOfSecondPortalArea;
    private static int colorOfSecondPortalArea2;
    public static void render(
        PoseStack matrixStack,
        MultiBufferSource.BufferSource bufferSource,
        double camX, double camY, double camZ
    ) {}

}

