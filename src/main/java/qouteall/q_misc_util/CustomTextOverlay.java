// QUASAR STUB: GUI/render code parked (see src/parked).
package qouteall.q_misc_util;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.jetbrains.annotations.Nullable;
import java.util.TreeMap;

// QUASAR STUB: generated no-op stand-in; the original lives in src/parked (render rewrite pending).

public class CustomTextOverlay {

    public static record Entry(
        Component component,
        long clearingTime
    ) {

    }

    private static TreeMap<String, Entry> ENTRIES;
    private static boolean renderAtBottomCenter;
    private static MultiLineLabel multiLineLabelCache;
    public static void putText(Component component, double durationSeconds, String key) {}

    public static void putText(Component component, double durationSeconds) {}

    public static void putText(Component component, String key) {}

    public static void putText(Component component) {}

    public static boolean remove(String key) { return false; }

    public static void render(GuiGraphicsExtractor guiGraphics, DeltaTracker deltaTracker) {}

}

