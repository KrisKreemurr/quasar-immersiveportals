// QUASAR STUB: parked peripheral feature (dimension stack / alternate dimensions / portal wand / command stick), see src/parked.
package qouteall.imm_ptl.peripheral.dim_stack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractSelectionList;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import org.jetbrains.annotations.Nullable;
import qouteall.q_misc_util.Helper;

// QUASAR STUB: generated no-op stand-in; the original lives in src/parked (render rewrite pending).

public class DimListWidget {

    public static int ROW_WIDTH;
    public interface DraggingCallback {

        void run(int selectedIndex, int mouseOnIndex);

    }

    public Screen parent;
    private Type type;
    private DraggingCallback draggingCallback;
    public enum Type {

        mainDimensionList, addDimensionList;
    }

    public DimListWidget(
        int width,
        int height,
        int top,
        int itemHeight,
        Screen parent,
        Type type,
        @Nullable DraggingCallback draggingCallback
    ) { super(Minecraft.getInstance(), width, height, top, itemHeight); }

    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) { return false; }

    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {}

    public int getRowWidth() { return 0; }

    protected int getScrollbarPosition() { return 0; }



}

