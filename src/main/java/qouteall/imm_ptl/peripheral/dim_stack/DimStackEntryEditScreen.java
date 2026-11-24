// QUASAR STUB: parked peripheral feature (dimension stack / alternate dimensions / portal wand / command stick), see src/parked.
package qouteall.imm_ptl.peripheral.dim_stack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import qouteall.q_misc_util.Helper;
import qouteall.q_misc_util.my_util.GuiHelper;
import java.util.OptionalInt;

// QUASAR STUB: generated no-op stand-in; the original lives in src/parked (render rewrite pending).

public class DimStackEntryEditScreen extends Screen {

    private DimStackScreen parent;
    private DimEntryWidget editing;
    private EditBox scaleField;
    private Button flipButton;
    private EditBox horizontalRotationField;
    private EditBox topYField;
    private EditBox bottomYField;
    private EditBox bedrockBlockField;
    private Button connectsPreviousButton;
    private Button connectsNextButton;
    private Button finishButton;
    private GuiHelper.Rect scaleLabelRect;
    private GuiHelper.Rect flipLabelRect;
    private GuiHelper.Rect horizontalRotationLabelRect;
    private GuiHelper.Rect topYLabelRect;
    private GuiHelper.Rect bottomYLabelRect;
    private GuiHelper.Rect bedrockLabelRect;
    private GuiHelper.Rect connectsPreviousRect;
    private GuiHelper.Rect connectsNextRect;
    private Button helpButton;
    protected DimStackEntryEditScreen(
        DimStackScreen parent,
        DimEntryWidget editing,
        Runnable callback
    ) { super(Component.literal("you cannot see me")); }

    public void tick() {}

    protected void init() {}

    public void onClose() {}



}

