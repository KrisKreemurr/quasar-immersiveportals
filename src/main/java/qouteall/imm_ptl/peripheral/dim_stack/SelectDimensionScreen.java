// QUASAR STUB: parked peripheral feature (dimension stack / alternate dimensions / portal wand / command stick), see src/parked.
package qouteall.imm_ptl.peripheral.dim_stack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import java.util.List;
import java.util.function.Consumer;

// QUASAR STUB: generated no-op stand-in; the original lives in src/parked (render rewrite pending).

public class SelectDimensionScreen extends Screen {

    public DimStackScreen parent;
    private DimListWidget dimListWidget;
    private Button confirmButton;
    private Consumer<ResourceKey<Level>> outerCallback;
    private List<ResourceKey<Level>> dimensionList;
    protected SelectDimensionScreen(
        DimStackScreen parent,
        Consumer<ResourceKey<Level>> callback,
        List<ResourceKey<Level>> dimensionList
    ) { super(Component.translatable("imm_ptl.select_dimension")); }

    protected void init() {}

    public void onClose() {}

    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {}

    protected SelectDimensionScreen() {}

}

