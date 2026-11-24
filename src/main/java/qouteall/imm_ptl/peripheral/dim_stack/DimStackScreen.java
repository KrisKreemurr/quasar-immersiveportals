// QUASAR STUB: parked peripheral feature (dimension stack / alternate dimensions / portal wand / command stick), see src/parked.
package qouteall.imm_ptl.peripheral.dim_stack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import qouteall.imm_ptl.core.CHelper;
import qouteall.q_misc_util.my_util.GuiHelper;
import java.util.function.Consumer;

// QUASAR STUB: generated no-op stand-in; the original lives in src/parked (render rewrite pending).

public class DimStackScreen extends Screen {

    private static Logger LOGGER;
    private DimStackGuiController controller;
    .jetbrains.annotations.Nullable
    public Screen parent;
    private Button finishButton;
    private Button toggleButton;
    private Button addDimensionButton;
    private Button removeDimensionButton;
    private Button editButton;
    private Button helpButton;
    private Button setAsPresetButton;
    private Button loopButton;
    private Button gravityModeButton;
    private int titleY;
    public DimListWidget dimListWidget;
    private boolean isEnabled;
    public DimStackScreen(
        @Nullable Screen parent,
        DimStackGuiController controller
    ) { super(Component.translatable("imm_ptl.altius_screen")); }

    public void setLoopEnabled(boolean loopEnabled) {}

    public void setGravityTransformEnabled(boolean gravityTransformEnabled) {}

    public static Button createHelpButton(Screen parent) { return null; }

    public DimEntryWidget createDimEntryWidget(DimStackEntry entry) { return null; }

    protected void init() {}

    public void onClose() {}

    private Consumer<DimEntryWidget> getElementSelectCallback() { return null; }

    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {}

    public void setEnabled(boolean cond) {}

    private void onAddEntry() {}

    private void onRemoveEntry() {}

    private void onEditEntry() {}

    protected DimStackScreen() {}

}

