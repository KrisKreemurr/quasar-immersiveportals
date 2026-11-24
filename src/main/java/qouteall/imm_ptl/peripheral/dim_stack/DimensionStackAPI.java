// QUASAR STUB: parked peripheral feature (dimension stack / alternate dimensions / portal wand / command stick), see src/parked.
package qouteall.imm_ptl.peripheral.dim_stack;

import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.neoforged.bus.api.Event;
import org.jetbrains.annotations.Nullable;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

// QUASAR STUB: generated no-op stand-in; the original lives in src/parked (render rewrite pending).

public class DimensionStackAPI {

    public interface DimensionCollectionCallback {

        Collection<ResourceKey<Level>> getExtraDimensionKeys(
            RegistryAccess.Frozen registryAccess,
            WorldOptions options
        );

    }

    public interface DimensionStackPreUpdateCallback {

        void run(MinecraftServer server, @Nullable DimStackInfo dimStackInfo);

    }

    public static class DimensionStackCandidateCollectionEvent extends Event {

        public RegistryAccess.Frozen registryAccess;
        public WorldOptions options;
        Set<ResourceKey<Level>> result;
        public DimensionStackCandidateCollectionEvent(RegistryAccess.Frozen registryAccess, WorldOptions options) {}

        public void addToResult(Collection<ResourceKey<Level>> dimensions) {}

        public Set<ResourceKey<Level>> getDimensionsResult() { return null; }

        protected DimensionStackCandidateCollectionEvent() {}

    }

    public static class DimensionStackPreUpdateEvent extends Event {

        public MinecraftServer server;
        public DimStackInfo dimStackInfo;
        public DimensionStackPreUpdateEvent(MinecraftServer server, @Nullable DimStackInfo dimStackInfo) {}

        protected DimensionStackPreUpdateEvent() {}

    }

}

