package qouteall.imm_ptl.core.render.quasar;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.particle.ParticleResources;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.IdentityHashMap;
import java.util.Map;

/**
 * Quasar MC: particles of background worlds (other dimensions seen through portals).
 *
 * <p>Minecraft has one particle engine, bound to the player's world. Particles a background world spawns (lava
 * pops, ash, crits and smoke from its mobs, block-break bits) used to land in that engine at the background
 * world's coordinates, so they showed up in the player's world, and Immersive Portals ticked the main engine a
 * second time per tick (particles near a portal aged twice as fast) after moving the main camera for the
 * particle distance check. Now each background world gets its own engine:</p>
 * <ul>
 * <li>{@code MixinClientLevel_Particles} / {@code MixinClientPacketListener_Particles} route a background world's
 *     particle calls to {@link #engineFor}.</li>
 * <li>{@link #tick} runs once per client tick per background world (from {@code ClientWorldLoader.tickRemoteWorld}).</li>
 * <li>The particle distance check uses {@link #observer}: where the player would be in that world (the player's
 *     position mapped through the nearest portal into it), instead of the moved main camera.</li>
 * <li>Portal views of that world draw its engine ({@link PortalEntities}).</li>
 * </ul>
 */
public final class PortalParticles {
    private static final Map<ClientLevel, ParticleEngine> ENGINES = new IdentityHashMap<>();
    private static final Map<ClientLevel, Vec3> OBSERVERS = new IdentityHashMap<>();
    private static ParticleResources resources;
    private static boolean failed;

    private PortalParticles() {}

    /** True when {@code level} is a client world the player is not in (a portal background world). */
    public static boolean isBackground(ClientLevel level) {
        Minecraft mc = Minecraft.getInstance();
        return level != null && mc.player != null && mc.player.level() != level;
    }

    /**
     * The engine for a background world's particles (created on first use), or null for the player's own world
     * (use the main engine). Render/client thread only.
     */
    @Nullable
    public static ParticleEngine engineFor(ClientLevel level) {
        if (!isBackground(level)) {
            // the player is in this world now: the main engine is its engine, drop the old background one
            if (level != null && !ENGINES.isEmpty()) ENGINES.remove(level);
            return null;
        }
        if (failed) return null;
        ParticleEngine engine = ENGINES.get(level);
        if (engine == null) {
            try {
                engine = new ParticleEngine(level, resources());
            }
            catch (Throwable t) {
                failed = true;
                org.slf4j.LoggerFactory.getLogger("Quasar Portals")
                    .error("[Quasar Portals] particle engine for background worlds unavailable; their particles are dropped", t);
                return null;
            }
            ENGINES.put(level, engine);
        }
        return engine;
    }

    /** The background world's engine if it already has one (does not create one). */
    @Nullable
    public static ParticleEngine existing(ClientLevel level) {
        return isBackground(level) ? ENGINES.get(level) : null;
    }

    /** Ages and removes the background world's particles; once per client tick. */
    public static void tick(ClientLevel level) {
        ParticleEngine engine = existing(level);
        if (engine != null) engine.tick();
    }

    /** Where the player would be in {@code level} (through the nearest portal), for particle distance checks. */
    public static void setObserver(ClientLevel level, @Nullable Vec3 pos) {
        if (pos == null) OBSERVERS.remove(level);
        else OBSERVERS.put(level, pos);
    }

    @Nullable
    public static Vec3 observer(ClientLevel level) {
        return OBSERVERS.get(level);
    }

    public static void dispose(ClientLevel level) {
        ENGINES.remove(level);
        OBSERVERS.remove(level);
    }

    public static void disposeAll() {
        ENGINES.clear();
        OBSERVERS.clear();
    }

    /** The main engine's particle providers (shared; they are stateless factories). */
    private static ParticleResources resources() throws ReflectiveOperationException {
        if (resources != null) return resources;
        ParticleEngine main = Minecraft.getInstance().particleEngine;
        for (Class<?> c = main.getClass(); c != null && c != Object.class; c = c.getSuperclass()) {
            for (Field f : c.getDeclaredFields()) {
                if (f.getType() == ParticleResources.class) {
                    f.setAccessible(true);
                    resources = (ParticleResources) f.get(main);
                    if (resources != null) return resources;
                }
            }
        }
        throw new NoSuchFieldException("ParticleEngine has no ParticleResources field");
    }
}
