// QUASAR STUB: parked peripheral feature (dimension stack / alternate dimensions / portal wand / command stick), see src/parked.
package qouteall.imm_ptl.peripheral.dim_stack;

import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import qouteall.imm_ptl.core.CHelper;
import qouteall.imm_ptl.core.McHelper;
import qouteall.q_misc_util.my_util.DQuaternion;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

// QUASAR STUB: generated no-op stand-in; the original lives in src/parked (render rewrite pending).

public class DimEntryWidget extends ContainerObjectSelectionList.Entry<DimEntryWidget> {

    private static Logger LOGGER;
    public enum ArrowType {

        none, enabled, conflicting;
    }

    public ResourceKey<Level> dimension;
    public DimListWidget parent;
    private Consumer<DimEntryWidget> selectCallback;
    private Identifier dimIconPath;
    private Component dimensionName;
    public DimStackEntry entry;
    public int entryIndex;
    ArrowType arrowToPrevious;
    ArrowType arrowToNext;
    public static int widgetHeight;
    public @NotNull List<? extends NarratableEntry> narratables() { return null; }

    public DimEntryWidget(
        ResourceKey<Level> dimension,
        DimListWidget parent,
        Consumer<DimEntryWidget> selectCallback,
        @Nullable DimStackEntry entry
    ) {}

    private List<GuiEventListener> children;
    public @NotNull List<? extends GuiEventListener> children() { return null; }

    public void render(
        int index,
        int y,
        int x,
        int rowWidth,
        int itemHeight,
        int mouseX,
        int mouseY,
        boolean bl,
        float delta
    ) {}

    private Component getText1() { return null; }

    private Component getText2() { return null; }

    public boolean mouseClicked(double mouseX, double mouseY, int button) { return false; }

    protected DimEntryWidget() {}

}

