// QUASAR STUB: real code parked in src/parked (rendering is replaced for Quasar MC).
package qouteall.imm_ptl.core.render.context_management;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import qouteall.imm_ptl.core.ClientWorldLoader;
import qouteall.imm_ptl.core.ducks.IECamera;
import java.util.function.Consumer;
import java.util.function.Supplier;

// QUASAR STUB: generated no-op stand-in; the original lives in src/parked (render rewrite pending).

@SuppressWarnings("SpellCheckingInspection")
public class FogRendererContext {

    public float red;
    public float green;
    public float blue;
    public int targetBiomeFog;
    public int previousBiomeFog;
    public long biomeChangedTime;
    public static Consumer<FogRendererContext> copyContextFromObject;
    public static Consumer<FogRendererContext> copyContextToObject;
    public static Supplier<Vec3> getCurrentFogColor;
    public static StaticFieldsSwappingManager<FogRendererContext> swappingManager;
    public static void init() {}

    public static void update() {}

    public static Vec3 getFogColorOf(
        ClientLevel destWorld, Vec3 pos
    ) { return null; }

    public static void onPlayerTeleport(ResourceKey<Level> from, ResourceKey<Level> to) {}

}

