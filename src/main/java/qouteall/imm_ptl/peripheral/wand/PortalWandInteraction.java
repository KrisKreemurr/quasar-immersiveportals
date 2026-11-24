// QUASAR STUB: parked peripheral feature (dimension stack / alternate dimensions / portal wand / command stick), see src/parked.
package qouteall.imm_ptl.peripheral.wand;

import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.server.permissions.Permissions;
import com.mojang.logging.LogUtils;
import de.nick1st.imm_ptl.events.ServerCleanupEvent;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import qouteall.imm_ptl.core.IPGlobal;
import qouteall.imm_ptl.core.IPPerServerInfo;
import qouteall.imm_ptl.core.McHelper;
import qouteall.imm_ptl.core.platform_specific.IPConfig;
import qouteall.imm_ptl.core.portal.Portal;
import qouteall.imm_ptl.core.portal.PortalExtension;
import qouteall.imm_ptl.core.portal.PortalManipulation;
import qouteall.imm_ptl.core.portal.PortalState;
import qouteall.imm_ptl.core.portal.animation.UnilateralPortalState;
import qouteall.imm_ptl.core.portal.util.PortalLocalXYNormalized;
import qouteall.imm_ptl.peripheral.CommandStickItem;
import qouteall.q_misc_util.my_util.DQuaternion;
import qouteall.q_misc_util.my_util.Plane;
import qouteall.q_misc_util.my_util.Range;
import java.util.List;
import java.util.UUID;
import java.util.WeakHashMap;

// QUASAR STUB: generated no-op stand-in; the original lives in src/parked (render rewrite pending).

public class PortalWandInteraction {

    private static double SIZE_LIMIT;
    private static Logger LOGGER;
    private WeakHashMap<ServerPlayer, DraggingSession> draggingSessionMap;
    public PortalWandInteraction() {}

    public static PortalWandInteraction of(MinecraftServer server) { return null; }

    public static class RemoteCallables {

        public static void finishPortalCreation(
            ServerPlayer player,
            ProtoPortal protoPortal
        ) {}

        public static void requestApplyDrag(
            ServerPlayer player,
            UUID portalId,
            Vec3 cursorPos,
            DraggingInfo draggingInfo
        ) {}

        public static void undoDrag(ServerPlayer player) {}

        public static void finishDragging(ServerPlayer player) {}

        public static void copyCutPortal(ServerPlayer player, UUID portalId, boolean isCut) {}

        public static void confirmCopyCut(ServerPlayer player, Vec3 origin, DQuaternion orientation) {}

        public static void clearPortalClipboard(ServerPlayer player) {}

    }

    private static void handleFinishPortalCreation(ServerPlayer player, ProtoPortal protoPortal) {}

    private static class DraggingSession {

        private ResourceKey<Level> dimension;
        private UUID portalId;
        private PortalState originalState;
        private DraggingInfo lastDraggingInfo;
        public DraggingSession(
            ResourceKey<Level> dimension, UUID portalId,
            PortalState originalState, DraggingInfo lastDraggingInfo
        ) {}

        @Nullable
        public Portal getPortal() { return null; }

        protected DraggingSession() {}

    }

    public static void init() {}

    public static final class DraggingInfo {

        public PortalLocalXYNormalized lockedAnchor;
        public PortalLocalXYNormalized draggingAnchor;
        public Vec3 previousRotationAxis;
        public boolean lockWidth;
        public boolean lockHeight;
        public DraggingInfo(
            @Nullable PortalLocalXYNormalized lockedAnchor,
            PortalLocalXYNormalized draggingAnchor,
            @Nullable Vec3 previousRotationAxis,
            boolean lockWidth,
            boolean lockHeight
        ) {}

        public boolean shouldLockScale() { return false; }

        public boolean isValid() { return false; }

        protected DraggingInfo() {}

    }

    @Nullable
    public static UnilateralPortalState applyDrag(
        UnilateralPortalState originalState, Vec3 cursorPos, DraggingInfo info,
        boolean updateInternalState
    ) { return null; }

    private static void handleFinishDrag(ServerPlayer player) {}

    private static void handleUndoDrag(ServerPlayer player) {}

    private static void handleDraggingRequest(
        ServerPlayer player, UUID portalId, Vec3 cursorPos, DraggingInfo draggingInfo, Portal portal
    ) {}

    private static boolean checkPermission(ServerPlayer player) { return false; }

    public static boolean validateDraggedPortalState(
        PortalState originalState, UnilateralPortalState newThisSideState,
        Player player
    ) { return false; }

    private static boolean canPlayerUsePortalWand(ServerPlayer player) { return false; }

    private static void giveCommandStick(ServerPlayer player, String command) {}

    public static boolean isDragging(ServerPlayer player) { return false; }

    @Nullable
    public static PortalWandInteraction.OneLockDraggingResult performDragWithOneLockedAnchor(
        UnilateralPortalState originalState,
        PortalLocalXYNormalized lockedLocalPos,
        PortalLocalXYNormalized draggingLocalPos, Vec3 draggedPos,
        @Nullable Vec3 previousRotationAxis,
        boolean lockWidth, boolean lockHeight
    ) { return null; }

    private static DQuaternion getOrientationByNormalDiagonalWidthHeight(
        Vec3 normal, Vec3 diagonal,
        double width, double height,
        double sigX, double sigY
    ) { return null; }

    public static record OneLockDraggingResult(
        UnilateralPortalState newState,
        Vec3 rotationAxis
    ) {

    }

    private record CopyingSession(
        PortalState portalState, CompoundTag portalData, boolean isCut,
        boolean hasFlipped, boolean hasReverse, boolean hasParallel
        
    ) {

    }

    private static WeakHashMap<ServerPlayer, CopyingSession> copyingSessionMap;
    public static void handleCopyCutPortal(ServerPlayer player, UUID portalId, boolean isCut) {}

    private static void handleConfirmCopyCut(ServerPlayer player, Vec3 origin, DQuaternion rawOrientation) {}

    private static void handleClearPortalClipboard(ServerPlayer player) {}

}

