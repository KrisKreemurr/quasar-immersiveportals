package qouteall.q_misc_util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

/**
 * Multi-line on-screen hints (the portal wand's instructions). Quasar MC: restored for 26.x - drawn from
 * NeoForge's RenderGuiEvent.Post (see {@code WandPreviewRendering}) with {@link GuiGraphicsExtractor#text}.
 */
public class CustomTextOverlay {
    
    public static record Entry(
        Component component,
        long clearingTime
    ) {}
    
    private static final TreeMap<String, Entry> ENTRIES = new TreeMap<>();
    private static List<FormattedCharSequence> linesCache;
    private static int linesCacheWidth = -1;
    
    public static void putText(Component component, double durationSeconds, String key) {
        ENTRIES.put(key, new Entry(component, System.nanoTime() + Helper.secondToNano(durationSeconds)));
        linesCache = null;
    }
    
    public static void putText(Component component, double durationSeconds) {
        putText(component, durationSeconds, "5_defaultKey");
    }
    
    public static void putText(Component component, String key) {
        putText(component, 0.2, key);
    }
    
    public static void putText(Component component) {
        putText(component, 0.2, "5_defaultKey");
    }
    
    public static boolean remove(String key) {
        boolean removed = ENTRIES.remove(key) != null;
        if (removed) linesCache = null;
        return removed;
    }
    
    public static void render(GuiGraphicsExtractor gui) {
        long now = System.nanoTime();
        if (ENTRIES.entrySet().removeIf(e -> e.getValue().clearingTime < now)) {
            linesCache = null;
        }
        if (ENTRIES.isEmpty()) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        Font font = mc.font;
        int width = mc.getWindow().getGuiScaledWidth();
        int height = mc.getWindow().getGuiScaledHeight();
        if (linesCache == null || linesCacheWidth != width) {
            List<FormattedCharSequence> lines = new ArrayList<>();
            for (Entry entry : ENTRIES.values()) {
                lines.addAll(font.split(entry.component(), width - 20));
            }
            linesCache = lines;
            linesCacheWidth = width;
        }
        int y = (int) (height * 0.75);
        for (FormattedCharSequence line : linesCache) {
            gui.text(font, line, (width - font.width(line)) / 2, y, 0xFFFFFFFF, true);
            y += font.lineHeight + 1;
        }
    }
}
