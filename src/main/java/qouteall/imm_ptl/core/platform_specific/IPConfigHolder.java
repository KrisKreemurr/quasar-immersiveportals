package qouteall.imm_ptl.core.platform_specific;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.neoforged.fml.loading.FMLPaths;
import qouteall.q_misc_util.Helper;

import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Quasar MC: replaces Cloth Config's AutoConfig holder. Same file (config/immersive_portals.json) and the same
 * Gson field layout, so existing configs keep loading; there is no config screen.
 */
public final class IPConfigHolder {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private final Path file = FMLPaths.CONFIGDIR.get().resolve("immersive_portals.json");
    private IPConfig config;

    public IPConfigHolder() {
        load();
    }

    public IPConfig getConfig() {
        return config;
    }

    public void setConfig(IPConfig config) {
        this.config = config;
    }

    public void load() {
        IPConfig loaded = null;
        if (Files.exists(file)) {
            try (Reader r = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
                loaded = GSON.fromJson(r, IPConfig.class);
            } catch (Exception e) {
                Helper.err("Failed to read Immersive Portals config, using defaults: " + e);
            }
        }
        config = loaded != null ? loaded : new IPConfig();
        save();
    }

    public void save() {
        try {
            Files.createDirectories(file.getParent());
            try (Writer w = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
                GSON.toJson(config, w);
            }
        } catch (Exception e) {
            Helper.err("Failed to write Immersive Portals config: " + e);
        }
        config.onConfigChanged();
    }
}
