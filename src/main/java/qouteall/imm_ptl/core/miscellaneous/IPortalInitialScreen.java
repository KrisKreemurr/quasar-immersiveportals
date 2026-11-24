// QUASAR STUB: GUI/render code parked (see src/parked).
package qouteall.imm_ptl.core.miscellaneous;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ImageWidget;
import net.minecraft.client.gui.components.MultiLineTextWidget;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import qouteall.imm_ptl.core.platform_specific.IPConfig;

// QUASAR STUB: generated no-op stand-in; the original lives in src/parked (render rewrite pending).

public class IPortalInitialScreen extends Screen {

    private static int PAGE_NUM;
    private Runnable onClose;
    private Button prevButton;
    private StringWidget pageNumberWidget;
    private Button iKnowButton;
    private ImageWidget iconWidget;
    private StringWidget titleWidget;
    private MultiLineTextWidget contentWidget;
    private int currentPageIndex;
    public IPortalInitialScreen(Runnable onClose) { super(Component.empty()); }

    private void onPrevious() {}

    private void onIKnow() {}

    public void onClose() {}

    public void init() {}

    protected IPortalInitialScreen() { super(net.minecraft.network.chat.Component.empty()); }

}

