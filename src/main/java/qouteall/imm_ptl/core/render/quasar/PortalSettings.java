package qouteall.imm_ptl.core.render.quasar;

import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
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
    
    private PortalSettings() {}
    
    private static Path file() {
        return Minecraft.getInstance().gameDirectory.toPath().resolve("config").resolve("quasar-portals.properties");
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
