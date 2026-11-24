// QUASAR STUB: GUI/render code parked (see src/parked).
package qouteall.q_misc_util.my_util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.network.chat.Component;
import java.util.Arrays;

// QUASAR STUB: generated no-op stand-in; the original lives in src/parked (render rewrite pending).

public class GuiHelper {

    public static LayoutFunc layoutButtonHorizontally(AbstractWidget widget) { return null; }

    public static LayoutFunc layoutRectHorizontally(Rect widget) { return null; }

    public static LayoutFunc layoutButtonVertically(AbstractWidget widget) { return null; }

    public static LayoutFunc layoutRectVertically(Rect widget) { return null; }

    public static void layout(
        int from, int to,
        LayoutElement... elements
    ) {}

    public static LayoutElement blankSpace(int length) { return null; }

    public static LayoutElement elasticBlankSpace() { return null; }

    public interface LayoutFunc {

        void apply(int from, int to);

    }

    public static LayoutFunc combine(LayoutFunc... args) { return null; }

    public static class Rect {

        public float xMin;
        public float yMin;
        public float xMax;
        public float yMax;
        public Rect(float xMin, float yMin, float xMax, float yMax) {}

        public Rect() { this(0, 0, 0, 0); }

        public void renderTextLeft(Component text, GuiGraphicsExtractor guiGraphics) {}

    }

    public static class LayoutElement {

        public boolean fixedLength;
        public int length;
        public LayoutFunc apply;
        public LayoutElement(boolean fixedLength, int length, LayoutFunc apply) {}

        protected LayoutElement() {}

    }

    public static LayoutElement elastic(int weight, LayoutFunc layoutFunc) { return null; }

    public static LayoutElement fixedLength(int length, LayoutFunc layoutFunc) { return null; }

}

