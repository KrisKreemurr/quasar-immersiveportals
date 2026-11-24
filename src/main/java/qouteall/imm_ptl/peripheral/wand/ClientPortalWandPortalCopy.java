// QUASAR STUB: parked peripheral feature (dimension stack / alternate dimensions / portal wand / command stick), see src/parked.
package qouteall.imm_ptl.peripheral.wand;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import qouteall.imm_ptl.core.mc_utils.WireRenderingHelper;
import qouteall.imm_ptl.core.platform_specific.IPConfig;
import qouteall.imm_ptl.core.portal.Portal;
import qouteall.imm_ptl.core.portal.PortalUtils;
import qouteall.imm_ptl.core.portal.animation.TimingFunction;
import qouteall.imm_ptl.core.portal.animation.UnilateralPortalState;
import qouteall.imm_ptl.core.render.TransformationManager;
import qouteall.imm_ptl.core.render.context_management.PortalRendering;
import qouteall.imm_ptl.core.render.context_management.RenderStates;
import qouteall.q_misc_util.CustomTextOverlay;
import qouteall.q_misc_util.Helper;
import qouteall.q_misc_util.api.McRemoteProcedureCall;
import qouteall.q_misc_util.api.McRemoteProcedureCallClient;
import qouteall.q_misc_util.my_util.AARotation;
import qouteall.q_misc_util.my_util.DQuaternion;
import qouteall.q_misc_util.my_util.RayTraceResult;
import qouteall.q_misc_util.my_util.WithDim;
import qouteall.q_misc_util.my_util.animation.Animated;
import qouteall.q_misc_util.my_util.animation.RenderedPoint;
import java.util.Arrays;
import java.util.Comparator;
import java.util.UUID;

// QUASAR STUB: generated no-op stand-in; the original lives in src/parked (render rewrite pending).

public class ClientPortalWandPortalCopy {

    private sealed interface Status permits Status_SelectPortal, Status_PlacingPortal {

    }

    private static final class Status_SelectPortal implements Status {

        public UUID selectedPortalId;
    }

    private static final class Status_PlacingPortal implements Status {

        public PlacementRequirement placementRequirement;
        public boolean isCut;
        public UnilateralPortalState pendingPlacement;
        private Status_PlacingPortal(@NotNull PlacementRequirement placementRequirement, boolean isCut) {}

        protected Status_PlacingPortal() {}

    }

    private static Status status;
    public record PlacementRequirement(
        double width, double height
    ) {

    }

    public static Animated<RenderedPoint> cursor;
    public static Animated<UnilateralPortalState> selection;
    public static Animated<DQuaternion> placementOrientation;
    public static Animated<Double> placementOffsetLen;
    public static void reset() {}

    public static void updateDisplay() {}

    public static void onLeftClick() {}

    public static void onRightClick() {}

    private static int colorOfCursor;
    private static int colorOfSelection1;
    private static int colorOfSelection2;
    private static int colorOfPendingPlacement1;
    private static int colorOfPendingPlacement2;
    public static void render(
        PoseStack matrixStack,
        MultiBufferSource.BufferSource bufferSource,
        double camX, double camY, double camZ
    ) {}

}

