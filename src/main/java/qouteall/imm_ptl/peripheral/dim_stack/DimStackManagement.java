// QUASAR STUB: parked peripheral feature (dimension stack / alternate dimensions / portal wand / command stick), see src/parked.
package qouteall.imm_ptl.peripheral.dim_stack;

import net.minecraft.server.permissions.Permissions;
import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;
import de.nick1st.imm_ptl.events.DimensionEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.neoforged.neoforge.common.NeoForge;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import qouteall.imm_ptl.core.IPGlobal;
import qouteall.imm_ptl.core.platform_specific.IPConfig;
import qouteall.imm_ptl.core.platform_specific.O_O;
import qouteall.imm_ptl.core.portal.global_portals.GlobalPortalStorage;
import qouteall.imm_ptl.core.portal.global_portals.VerticalConnectingPortal;
import qouteall.q_misc_util.Helper;
import qouteall.q_misc_util.api.McRemoteProcedureCall;
import qouteall.q_misc_util.api.McRemoteProcedureCallClient;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

// QUASAR STUB: generated no-op stand-in; the original lives in src/parked (render rewrite pending).

public class DimStackManagement {

    private static Logger LOGGER;
    public static DimStackInfo dimStackToApply;
    public static Map<ResourceKey<Level>, BlockState> bedrockReplacementMap;
    public static void init() {}

    private static boolean isDimStackEnabled() { return false; }

    public static void onServerEarlyInit(MinecraftServer server) {}

    public static void onServerCreatedWorlds(MinecraftServer server) {}

    private static void updateDimStackFromPresetInDedicatedServer(MinecraftServer server) {}

    private static void updateBedrockReplacementFromStorage(MinecraftServer server) {}

    public static void replaceBedrock(ServerLevel world, ChunkAccess chunk) {}

    private static Collection<ResourceKey<Level>> collectDimStackCandidateWhenServerRunning(
        MinecraftServer server
    ) { return null; }

    public static void onDimensionStackCommandExecute(
        ServerPlayer player
    ) {}

    public static class RemoteCallables {

        public static void clientOpenScreen(List<String> dimensions) {}

        public static void serverSetupDimStack(
            ServerPlayer player, DimStackInfo dimStackInfo
        ) {}

        public static void serverRemoveDimStack(
            ServerPlayer player
        ) {}

    }

    private static void updateDimStack(MinecraftServer server, DimStackInfo dimStackInfo) {}

    public static boolean hasDimStackPortal(MinecraftServer server) { return false; }

    private static void clearDimStackPortals(MinecraftServer server) {}

    @Nullable
    public static DimStackInfo getDimStackPreset() { return null; }

    public static void setDimStackPreset(@Nullable DimStackInfo preset) {}

}

