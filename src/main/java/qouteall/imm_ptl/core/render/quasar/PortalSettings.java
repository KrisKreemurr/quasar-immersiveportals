package qouteall.imm_ptl.core.render.quasar;

import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/** Quasar MC: portal view settings, saved to config/quasar-portals.properties (Video Settings → Portals, or /quasarportals). */
public final class PortalSettings {
    private static final Logger LOGGER = LogUtils.getLogger();
    
    public static final int DEFAULT_LIVE = 3, DEFAULT_DISTANCE = 8, DEFAULT_RES = 50,
        DEFAULT_UPDATE_EVERY = 4, DEFAULT_MIN_RATE = 60;
    
    /** clouds through portals (Video Settings → Portals → Clouds Through Portals) */
    public static volatile boolean clouds = true;
    /** water, glass and other translucent blocks through portals */
    public static volatile boolean translucent = true;
    /** mobs, players and items through portals, and how far (chunks) from the portal camera */
    public static volatile boolean entities = true;
    /** particles (smoke, splashes, crits...) through portals */
    public static volatile boolean particles = true;
    /** chests, signs, banners, beds and other block entities through portals (within the entity distance) */
    public static volatile boolean blockEntities = true;
    /** skip redrawing portals hidden behind walls/terrain (the game's chunk visibility search) */
    public static volatile boolean occlusion = true;
    /** far LOD terrain through the nearest portals (needs Quasar Horizons) */
    public static volatile boolean farTerrain = true;
    /** pixels per side of each far-terrain cube face */
    public static final int DEFAULT_FAR_RESOLUTION = 512;
    public static volatile int farResolution = DEFAULT_FAR_RESOLUTION;
    public static final int DEFAULT_ENTITY_DISTANCE = 4;
    public static volatile int entityDistance = DEFAULT_ENTITY_DISTANCE;

    private PortalSettings() {}
    
    private static Path file() {
        return net.neoforged.fml.loading.FMLPaths.CONFIGDIR.get().resolve("quasar-portals.properties"); // available during mod construction (Minecraft isn't yet)
    }
    
    public static void load() {
        Path f = file();
        if (!Files.exists(f)) return;
        Properties p = new Properties();
        try (Reader r = Files.newBufferedReader(f)) {
            p.load(r);
            PortalViewRenderer.liveLimit = clamp(p, "livePortals", DEFAULT_LIVE, 0, PortalViewRenderer.MAX_PORTALS);
            PortalViewRenderer.renderDistance = clamp(p, "renderDistance", DEFAULT_DISTANCE, 0, 64);
            PortalViewRenderer.resolutionScale = clamp(p, "resolutionPercent", DEFAULT_RES, 10, 100) / 100f;
            PortalViewRenderer.maxUpdateEvery = clamp(p, "maxUpdateEvery", DEFAULT_UPDATE_EVERY, 1, 8);
            PortalViewRenderer.minUpdatesPerSecond = clamp(p, "minUpdatesPerSecond", DEFAULT_MIN_RATE, 20, 240);
            farResolution = clamp(p, "farResolution", DEFAULT_FAR_RESOLUTION, 64, 1024);
            farTerrain = !"false".equalsIgnoreCase(p.getProperty("farTerrain", "true").trim());
            occlusion = !"false".equalsIgnoreCase(p.getProperty("hideBehindWalls", "true").trim());
            particles = !"false".equalsIgnoreCase(p.getProperty("particles", "true").trim());
            blockEntities = !"false".equalsIgnoreCase(p.getProperty("blockEntities", "true").trim());
            entities = !"false".equalsIgnoreCase(p.getProperty("entities", "true").trim());
            entityDistance = clamp(p, "entityDistance", DEFAULT_ENTITY_DISTANCE, 1, 16);
            translucent = !"false".equalsIgnoreCase(p.getProperty("waterAndGlass", "true").trim());
            clouds = !"false".equalsIgnoreCase(p.getProperty("clouds", "true").trim());
            PortalPlaceholderOutline.enabled = !"false".equalsIgnoreCase(p.getProperty("outlines", "true").trim());
        }
        catch (Exception e) {
            LOGGER.warn("[Quasar Portals] could not read {}", f, e);
        }
    }
    
    public static void save() {
        Path f = file();
        Properties p = new Properties();
        p.setProperty("livePortals", Integer.toString(PortalViewRenderer.liveLimit));
        p.setProperty("renderDistance", Integer.toString(PortalViewRenderer.renderDistance));
        p.setProperty("resolutionPercent", Integer.toString(resolutionPercent()));
        p.setProperty("maxUpdateEvery", Integer.toString(PortalViewRenderer.maxUpdateEvery));
        p.setProperty("minUpdatesPerSecond", Integer.toString(PortalViewRenderer.minUpdatesPerSecond));
        p.setProperty("farResolution", Integer.toString(farResolution));
        p.setProperty("farTerrain", Boolean.toString(farTerrain));
        p.setProperty("hideBehindWalls", Boolean.toString(occlusion));
        p.setProperty("particles", Boolean.toString(particles));
        p.setProperty("blockEntities", Boolean.toString(blockEntities));
        p.setProperty("entities", Boolean.toString(entities));
        p.setProperty("entityDistance", Integer.toString(entityDistance));
        p.setProperty("waterAndGlass", Boolean.toString(translucent));
        p.setProperty("clouds", Boolean.toString(clouds));
        p.setProperty("outlines", Boolean.toString(PortalPlaceholderOutline.enabled));
        try {
            Files.createDirectories(f.getParent());
            try (Writer w = Files.newBufferedWriter(f)) {
                p.store(w, "Quasar Portals: portal view settings (Video Settings -> Portals)");
            }
        }
        catch (Exception e) {
            LOGGER.warn("[Quasar Portals] could not save {}", f, e);
        }
    }
    
    public static int resolutionPercent() {
        return Math.round(PortalViewRenderer.resolutionScale * 100);
    }
    
    public static void resetDefaults() {
        PortalViewRenderer.liveLimit = DEFAULT_LIVE;
        PortalViewRenderer.renderDistance = DEFAULT_DISTANCE;
        PortalViewRenderer.resolutionScale = DEFAULT_RES / 100f;
        PortalViewRenderer.maxUpdateEvery = DEFAULT_UPDATE_EVERY;
        PortalViewRenderer.minUpdatesPerSecond = DEFAULT_MIN_RATE;
        PortalPlaceholderOutline.enabled = true;
        clouds = true;
        translucent = true;
        entities = true;
        particles = true;
        blockEntities = true;
        occlusion = true;
        farTerrain = true;
        farResolution = DEFAULT_FAR_RESOLUTION;
        entityDistance = DEFAULT_ENTITY_DISTANCE;
        save();
    }
    
    private static int clamp(Properties p, String key, int def, int min, int max) {
        try {
            return Math.max(min, Math.min(max, Integer.parseInt(p.getProperty(key, Integer.toString(def)).trim())));
        }
        catch (NumberFormatException e) {
            return def;
        }
    }
}
