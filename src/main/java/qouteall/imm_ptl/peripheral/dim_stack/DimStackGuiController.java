// QUASAR STUB: parked peripheral feature (dimension stack / alternate dimensions / portal wand / command stick), see src/parked.
package qouteall.imm_ptl.peripheral.dim_stack;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.AlertScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import org.apache.commons.lang3.Validate;
import qouteall.imm_ptl.core.portal.global_portals.VerticalConnectingPortal;
import qouteall.q_misc_util.Helper;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

// QUASAR STUB: generated no-op stand-in; the original lives in src/parked (render rewrite pending).

public class DimStackGuiController {

    private static int entryCountLimit;
    private Screen parentScreen;
    public DimStackGuiModel model;
    public DimStackScreen view;
    private boolean hasConflict;
    private Supplier<List<ResourceKey<Level>>> dimensionListSupplier;
    private Consumer<DimStackInfo> finishCallback;
    public DimStackGuiController(
        Screen parentScreen,
        Supplier<List<ResourceKey<Level>>> dimensionListSupplier,
        Consumer<DimStackInfo> finishCallback
    ) {}

    public void setEnabled(boolean enabled) {}

    private void updateViewState() {}

    public boolean addEntry(int index, DimStackEntry entry) { return false; }

    public void batchAddEntries(int index, List<DimStackEntry> entries) {}

    public void removeEntry(int index) {}

    public void clear() {}

    public void editEntry(int index, DimStackEntry newEntry) {}

    public void onDragged(int selected, int mouseOver) {}

    public void initializeAsDefault() {}

    public List<ResourceKey<Level>> getDimensionList() { return null; }

    public void onFinish() {}

    private void showConflictingAlert() {}

    public void toggleEnabled() {}

    public void toggleLoop() {}

    public void setLoopEnabled(boolean cond) {}

    public void toggleGravityMode() {}

    public void setGravityTransformEnabled(boolean cond) {}

    public void setAsDefault() {}

    protected DimStackGuiController() {}

}

