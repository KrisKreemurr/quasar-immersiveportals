package qouteall.imm_ptl.core;

import de.nick1st.imm_ptl.events.ClientExitEvent;
import de.nick1st.imm_ptl.events.DimensionEvents;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import qouteall.imm_ptl.core.ducks.IECamera;
import qouteall.imm_ptl.core.ducks.IEClientPlayNetworkHandler;
import qouteall.imm_ptl.core.ducks.IEClientWorld;
import qouteall.imm_ptl.core.ducks.IEMinecraftClient;
import qouteall.imm_ptl.core.ducks.IEParticleManager;
import qouteall.imm_ptl.core.ducks.IEWorld;
import qouteall.imm_ptl.core.ducks.IEWorldRenderer;
import qouteall.imm_ptl.core.mixin.client.accessor.IEClientLevelData;
import qouteall.imm_ptl.core.mixin.client.accessor.IEClientLevel_Accessor;
import qouteall.imm_ptl.core.portal.Portal;
import qouteall.imm_ptl.core.render.context_management.DimensionRenderHelper;
import qouteall.imm_ptl.core.render.context_management.PortalRendering;
import qouteall.q_misc_util.my_util.CountDownInt;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@SuppressWarnings("resource")
//@OnlyIn(Dist.CLIENT)
public class ClientWorldLoader {
    private static final Logger LOGGER = LoggerFactory.getLogger(ClientWorldLoader.class);
    
    private static final CountDownInt LOG_LIMIT = new CountDownInt(20);

    private static final Map<ResourceKey<Level>, ClientLevel> CLIENT_WORLD_MAP =
        new Object2ObjectOpenHashMap<>();
    public static final Map<ResourceKey<Level>, LevelRenderer> WORLD_RENDERER_MAP =
        new Object2ObjectOpenHashMap<>();
    public static final Map<ResourceKey<Level>, DimensionRenderHelper> RENDER_HELPER_MAP =
        new Object2ObjectOpenHashMap<>();
    
    public static @Nullable Map<ResourceKey<Level>, ResourceKey<DimensionType>> dimIdToDimTypeId;
    // Quasar MC: read Minecraft.getInstance() live; a static copy is null if this class loads during mod setup.
    
    private static boolean isInitialized = false;
    
    private static boolean isCreatingClientWorld = false;
    
    public static boolean isClientRemoteTicking = false;
    
    private static boolean isWorldSwitched = false;
    
    public static void init() {
        NeoForge.EVENT_BUS.addListener(DimensionEvents.CLIENT_DIMENSION_UPDATE_EVENT.class, (event -> {
            if (getIsInitialized()) {
                List<ResourceKey<Level>> dimensionsToRemove =
                        CLIENT_WORLD_MAP.keySet().stream()
                                .filter(dim -> !event.dimensions.contains(dim)).toList();

                for (ResourceKey<Level> dim : dimensionsToRemove) {
                    disposeDimensionDynamically(dim);
                }

            }
        }));

        NeoForge.EVENT_BUS.addListener(ClientExitEvent.class, (e) -> dimIdToDimTypeId = null);
    }
    
    public static boolean getIsInitialized() {
        return isInitialized;
    }
    
    public static boolean getIsCreatingClientWorld() {
        return isCreatingClientWorld;
    }
    
    public static void tick() {
        if (IPCGlobal.isClientRemoteTickingEnabled) {
            isClientRemoteTicking = true;
            CLIENT_WORLD_MAP.values().forEach(world -> {
                if (Minecraft.getInstance().level != world) {
                    tickRemoteWorld(world);
                }
            });
            isClientRemoteTicking = false;
        }
        
        // Quasar MC: per-dimension lightmaps belonged to the parked renderer.
        
    }
    
    public static void disposeRenderHelpers() {
        RENDER_HELPER_MAP.values().forEach(DimensionRenderHelper::cleanUp);
        RENDER_HELPER_MAP.clear();
    }
    
    private static void tickRemoteWorld(ClientLevel newWorld) {
        List<Portal> nearbyPortals = CHelper.getClientNearbyPortals(10).collect(Collectors.toList());
        
        withSwitchedWorld(newWorld, () -> {
            try {
                newWorld.tickEntities();
                newWorld.tick(() -> true);
                
                if (!Minecraft.getInstance().isPaused()) {
                    tickRemoteWorldRandomTicksClient(newWorld, nearbyPortals);
                }
                
                newWorld.pollLightUpdates();
            }
            catch (Throwable e) {
                if (LOG_LIMIT.tryDecrement()) {
                    LOGGER.error("", e);
                }
            }
        });
    }
    
    // show nether particles through portal
    // TODO optimize it in future version?
    private static void tickRemoteWorldRandomTicksClient(
        ClientLevel newWorld, List<Portal> nearbyPortals
    ) {
        nearbyPortals.stream().filter(
            portal -> portal.getDestDim() == newWorld.dimension()
        ).findFirst().ifPresent(portal -> {
            assert Minecraft.getInstance().player != null;
            Vec3 playerPos = Minecraft.getInstance().player.position();
            Vec3 center = portal.transformPoint(playerPos);
            
            Camera camera = Minecraft.getInstance().gameRenderer.mainCamera();
            Vec3 oldCameraPos = camera.position();
            
            if (camera instanceof IECamera quasarCam) quasarCam.portal_setPos(center);
            
            if (newWorld.getGameTime() % 2 == 0) {
                // it costs some CPU time
                newWorld.animateTick(
                    (int) center.x, (int) center.y, (int) center.z
                );
            }
            
            Minecraft.getInstance().particleEngine.tick();
            
            if (camera instanceof IECamera quasarCam) quasarCam.portal_setPos(oldCameraPos);
        });
        
        
    }
    
    public static void cleanUp() {
        WORLD_RENDERER_MAP.values().forEach(
            ClientWorldLoader::disposeWorldRenderer
        );
        
        for (ClientLevel clientWorld : CLIENT_WORLD_MAP.values()) {
            ((IEClientWorld) clientWorld).ip_resetWorldRendererRef();
        }
        
        CLIENT_WORLD_MAP.clear();
        WORLD_RENDERER_MAP.clear();
        
        disposeRenderHelpers();
        
        isInitialized = false;
    }
    
    private static void disposeWorldRenderer(LevelRenderer worldRenderer) {
        if (worldRenderer != Minecraft.getInstance().levelRenderer) {
            worldRenderer.close();
            ((IEWorldRenderer) worldRenderer).portal_fullyDispose();
        }
    }
    
    private static void disposeDimensionDynamically(ResourceKey<Level> dimension) {
        Validate.notNull(Minecraft.getInstance().player, "player is null");
        Validate.notNull(Minecraft.getInstance().level, "level is null");
        Validate.isTrue(
            Minecraft.getInstance().level.dimension() != dimension,
            "Cannot dispose current dimension"
        );
        Validate.isTrue(
            Minecraft.getInstance().player.level().dimension() != dimension,
            "Cannot dispose current dimension"
        );
        Validate.isTrue(Minecraft.getInstance().isSameThread(), "not on client thread");
        
        LevelRenderer worldRenderer = WORLD_RENDERER_MAP.get(dimension);
        disposeWorldRenderer(worldRenderer);
        WORLD_RENDERER_MAP.remove(dimension);
        
        Validate.isTrue(Minecraft.getInstance().levelRenderer != worldRenderer);
        
        ClientLevel clientWorld = CLIENT_WORLD_MAP.get(dimension);
        ((IEClientWorld) clientWorld).ip_resetWorldRendererRef();
        CLIENT_WORLD_MAP.remove(dimension);
        
        DimensionRenderHelper renderHelper = RENDER_HELPER_MAP.remove(dimension);
        if (renderHelper != null) {
            renderHelper.cleanUp();
        }
        
        LOGGER.info("Client Dynamically Removed Dimension {}", dimension.identifier());
        
        if (clientWorld.getChunkSource().getLoadedChunksCount() > 0) {
            LOGGER.error("The chunks of that dimension was not cleared before removal");
        }
        
        if (clientWorld.getEntityCount() > 0) {
            LOGGER.error("The entities of that dimension was not cleared before removal");
        }
        
        Minecraft.getInstance().gameRenderer.resetData();

        NeoForge.EVENT_BUS.post(new DimensionEvents.CLIENT_DIMENSION_DYNAMIC_REMOVE_EVENT(dimension));
    }
    
    @NotNull
    public static LevelRenderer getWorldRenderer(ResourceKey<Level> dimension) {
        initializeIfNeeded();
        
        LevelRenderer result = WORLD_RENDERER_MAP.get(dimension);
        
        if (result == null) {
            LOGGER.warn(
                "Acquiring LevelRenderer before acquiring Level. Something is probably wrong. {}",
                dimension.identifier(), new Throwable()
            );
            
            // the world renderer is created along with the world
            // so create the world now
            getWorld(dimension);
            
            result = WORLD_RENDERER_MAP.get(dimension);
            
            if (result == null) {
                throw new RuntimeException("Unable to get LevelRenderer of " + dimension.identifier());
            }
        }
        
        return result;
    }
    
    
    /**
     * Get the client world and create if missing.
     * If the dimension intId is invalid, it will throw an error
     */
    @NotNull
    public static ClientLevel getWorld(ResourceKey<Level> dimension) {
        Validate.notNull(dimension, "dimension is null");
        Validate.isTrue(Minecraft.getInstance().isSameThread());
        
        initializeIfNeeded();
        
        if (!CLIENT_WORLD_MAP.containsKey(dimension)) {
            return createSecondaryClientWorld(dimension);
        }
        
        ClientLevel result = CLIENT_WORLD_MAP.get(dimension);
        Validate.notNull(result, "null value in world map");
        return result;
    }
    
    /**
     * Get the client world and create if missing.
     * If the dimension intId is invalid, it will return null
     */
    @Nullable
    public static ClientLevel getOptionalWorld(ResourceKey<Level> dimension) {
        Validate.notNull(dimension, "dimension is null");
        Validate.isTrue(Minecraft.getInstance().isSameThread(), "not on client thread");
        
        if (getServerDimensions().contains(dimension)) {
            return getWorld(dimension);
        }
        
        return null;
    }
    
    public static DimensionRenderHelper getDimensionRenderHelper(ResourceKey<Level> dimension) {
        initializeIfNeeded();
        
        DimensionRenderHelper result = RENDER_HELPER_MAP.computeIfAbsent(
            dimension,
            dimensionType -> {
                return new DimensionRenderHelper(
                    getWorld(dimension)
                );
            }
        );
        
        Validate.isTrue(result.world.dimension() == dimension);
        
        return result;
    }
    
    @SuppressWarnings("ConstantValue")
    public static void initializeIfNeeded() {
        if (!isInitialized) {
            Validate.isTrue(
                Minecraft.getInstance().level != null, "level is null"
            );
            // note: client.levelRenderer is not necessarily not null due to mixin
            Validate.isTrue(
                Minecraft.getInstance().levelRenderer != null, "levelRenderer is null"
            );
            
            Validate.notNull(
                Minecraft.getInstance().player,
                "player is null. This may be caused by prior initialization failure. The log may provide useful information."
            );
            Validate.isTrue(
                Minecraft.getInstance().player.level() == Minecraft.getInstance().level,
                "The player level is not the same as client level"
            );
            
            ResourceKey<Level> playerDimension = Minecraft.getInstance().level.dimension();
            CLIENT_WORLD_MAP.put(playerDimension, Minecraft.getInstance().level);
            WORLD_RENDERER_MAP.put(playerDimension, Minecraft.getInstance().levelRenderer);
            RENDER_HELPER_MAP.put(
                Minecraft.getInstance().level.dimension(),
                new DimensionRenderHelper(Minecraft.getInstance().level)
            );
            
            isInitialized = true;
        }
    }
    
    @SuppressWarnings("DataFlowIssue")
    private static ClientLevel createSecondaryClientWorld(ResourceKey<Level> dimension) {
        // Quasar MC: cross-dimension portals need a second client world and renderer. Not ported to 26.3 yet
        // (same-dimension portals come first; the original is in src/parked/ClientWorldLoader.java.txt).
        throw new IllegalStateException("Quasar MC: cross-dimension portals are not ported yet (" + dimension.identifier() + ")");
    }
    
    public static Set<ResourceKey<Level>> getServerDimensions() {
        assert Minecraft.getInstance().player != null;
        return Minecraft.getInstance().player.connection.levels();
    }
    
    public static Collection<ClientLevel> getClientWorlds() {
        Validate.isTrue(isInitialized);
        
        return CLIENT_WORLD_MAP.values();
    }
    
    private static boolean isReloadingOtherWorldRenderers = false;
    
    @SuppressWarnings("Convert2MethodRef")
    public static void _onWorldRendererReloaded() {
        Validate.isTrue(Minecraft.getInstance().isSameThread());
        if (Minecraft.getInstance().level != null) {
            LOGGER.info("WorldRenderer reloaded {}", Minecraft.getInstance().level.dimension().identifier());
        }
        
        if (isReloadingOtherWorldRenderers) {
            return;
        }
        if (PortalRendering.isRendering()) {
            return;
        }
        if (ClientWorldLoader.getIsCreatingClientWorld()) {
            return;
        }
        
        isReloadingOtherWorldRenderers = true;
        
        List<ResourceKey<Level>> toReload = WORLD_RENDERER_MAP.keySet().stream()
            .filter(d -> d != Minecraft.getInstance().level.dimension()).toList();
        
        for (ResourceKey<Level> dim : toReload) {
            ClientLevel world = CLIENT_WORLD_MAP.get(dim);
            Validate.notNull(world, "missing client world %s", dim.identifier());
            withSwitchedWorld(
                world,
                () -> {
                    // cannot be replaced into method reference
                    // because levelRenderer field is actually mutable
                    Minecraft.getInstance().levelRenderer.resetLevelRenderData();
                }
            );
        }
        
        isReloadingOtherWorldRenderers = false;
    }
    
    /**
     * It will not switch the dimension of client player
     */
    @SuppressWarnings({"ReassignedVariable", "DataFlowIssue"})
    public static <T> T withSwitchedWorld(ClientLevel newWorld, Supplier<T> supplier) {
        Validate.isTrue(Minecraft.getInstance().isSameThread(), "not on client thread");
        Validate.isTrue(Minecraft.getInstance().player != null, "player is null");
        
        ClientPacketListener networkHandler = Minecraft.getInstance().getConnection();
        assert networkHandler != null;
        
        ClientLevel originalWorld = Minecraft.getInstance().level;
        LevelRenderer originalWorldRenderer = Minecraft.getInstance().levelRenderer;
        ClientLevel originalNetHandlerWorld = networkHandler.getLevel();
        boolean originalIsWorldSwitched = isWorldSwitched;
        
        LevelRenderer newWorldRenderer = getWorldRenderer(newWorld.dimension());
        
        Validate.notNull(newWorldRenderer, "new world renderer is null");
        
        Minecraft.getInstance().level = newWorld;
        if (Minecraft.getInstance().particleEngine instanceof IEParticleManager quasarPm) quasarPm.ip_setWorld(newWorld); // Quasar MC: particle mixin parked
        ((IEMinecraftClient) Minecraft.getInstance()).ip_setWorldRenderer(newWorldRenderer);
        ((IEClientPlayNetworkHandler) networkHandler).ip_setWorld(newWorld);
        isWorldSwitched = true;
        
        try {
            return supplier.get();
        }
        finally {
            if (Minecraft.getInstance().level != newWorld) {
                LOGGER.error("Respawn packet should not be redirected");
                originalWorld = Minecraft.getInstance().level;
                originalWorldRenderer = Minecraft.getInstance().levelRenderer;
                // client.levelRenderer is not final by mixin.
            }
            
            Minecraft.getInstance().level = originalWorld;
            ((IEMinecraftClient) Minecraft.getInstance()).ip_setWorldRenderer(originalWorldRenderer);
            if (Minecraft.getInstance().particleEngine instanceof IEParticleManager quasarPm) quasarPm.ip_setWorld(originalWorld); // Quasar MC: particle mixin parked
            ((IEClientPlayNetworkHandler) networkHandler).ip_setWorld(originalNetHandlerWorld);
            isWorldSwitched = originalIsWorldSwitched;
        }
    }
    
    public static void withSwitchedWorld(ClientLevel newWorld, Runnable runnable) {
        withSwitchedWorld(newWorld, () -> {
            runnable.run();
            return null;
        });
    }
    
    public static void withSwitchedWorldFailSoft(ResourceKey<Level> dim, Runnable runnable) {
        ClientLevel world = getOptionalWorld(dim);

        if (runnable == null)
            LOGGER.error("Breakpoint");
        
        if (world == null) {
            LOGGER.error(
                "Ignoring redirected task of invalid dimension {}", dim.identifier(), new Throwable()
            );
            return;
        }
        
        withSwitchedWorld(world, runnable);
    }
    
    public static boolean getIsWorldSwitched() {
        return isWorldSwitched;
    }
    
    public static class RemoteCallables {
        public static void checkBiomeRegistry(
            Map<String, Integer> idMap
        ) {
            LocalPlayer player = Minecraft.getInstance().player;
            assert player != null;
            RegistryAccess registryAccess = player.connection.registryAccess();
            Registry<Biome> biomes = registryAccess.lookupOrThrow(Registries.BIOME);
            
            for (Map.Entry<String, Integer> entry : idMap.entrySet()) {
                Identifier id = McHelper.newResourceLocation(entry.getKey());
                int expectedId = entry.getValue();
                
                if (biomes.getId(biomes.getValue(id)) != expectedId) {
                    LOGGER.error("Biome intId mismatch: {} {}", id, expectedId);
                }
            }
            
            if (idMap.size() != biomes.keySet().size()) {
                LOGGER.error("Biome intId mismatch: size not equal");
            }
            
            LOGGER.info("Biome intId check finished");
        }
    }
}
