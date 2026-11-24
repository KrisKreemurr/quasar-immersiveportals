// QUASAR STUB: parked peripheral feature (dimension stack / alternate dimensions / portal wand / command stick), see src/parked.
package qouteall.imm_ptl.peripheral.dim_stack;

import com.mojang.datafixers.util.Pair;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.Nullable;
import qouteall.imm_ptl.core.McHelper;
import qouteall.imm_ptl.core.api.PortalAPI;
import qouteall.imm_ptl.core.portal.Portal;
import qouteall.imm_ptl.core.portal.global_portals.GlobalPortalStorage;
import qouteall.imm_ptl.core.portal.global_portals.VerticalConnectingPortal;
import qouteall.q_misc_util.Helper;
import java.util.*;

// QUASAR STUB: generated no-op stand-in; the original lives in src/parked (render rewrite pending).

public class DimStackInfo {

    public boolean loop;
    public boolean gravityTransform;
    public List<DimStackEntry> entries;
    public DimStackInfo() {}

    public DimStackInfo(List<DimStackEntry> entries, boolean loop, boolean gravityTransform) {}

    public static void initializeFuseViewProperty(Portal portal) {}

    public static void createConnectionBetween(
        DimStackEntry a, DimStackEntry b, boolean gravityChange
    ) {}

    public void apply(MinecraftServer server) {}

    public boolean isEffectivelyConnectingPrevious(int index) { return false; }

    public boolean isEffectivelyConnectionNext(int index) { return false; }

    public boolean isEffectivelyConnectingCeil(int index) { return false; }

    public boolean isEffectivelyConnectingFloor(int index) { return false; }

    public record PortalInfo(
        ResourceKey<Level> dimension,
        VerticalConnectingPortal.ConnectorType connectorType
    ) {

    }

    public Map<PortalInfo, List<DimStackEntry>> getPortalInfoMap() { return null; }

    @Nullable
    public static BlockState parseBlockString(@Nullable String str) { return null; }

    public boolean hasDimension(ResourceKey<Level> dimension) { return false; }

}

