package qouteall.imm_ptl.core.render.quasar;

import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ScrollableLayout;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

import java.util.function.IntConsumer;
import java.util.function.IntFunction;
import java.util.function.IntSupplier;

/**
 * Quasar MC: Video Settings → Portals. Every change applies on the next frame and is saved
 * to config/quasar-portals.properties. Opened from Quasium's Video Settings sidebar.
 */
public class PortalSettingsScreen extends Screen {
    private static final int WIDTH = 310;
    private final Screen parent;
    private final HeaderAndFooterLayout layout = new HeaderAndFooterLayout(this);
    private ScrollableLayout scrollArea;
    
    public PortalSettingsScreen(Screen parent) {
        super(Component.translatable("quasar_portals.options.title"));
        this.parent = parent;
    }
    
    @Override
    protected void init() {
        layout.addTitleHeader(getTitle(), font);
        LinearLayout rows = LinearLayout.vertical().spacing(4);
        rows.defaultCellSetting().alignHorizontallyCenter();
        
        rows.addChild(slider("live", 0, PortalViewRenderer.MAX_PORTALS, 1,
            () -> PortalViewRenderer.liveLimit, v -> PortalViewRenderer.liveLimit = v,
            v -> v == 0 ? Component.translatable("quasar_portals.options.live.off")
                : Component.translatable("quasar_portals.options.live.value", v)));
        rows.addChild(slider("update", 1, 8, 1,
            () -> PortalViewRenderer.maxUpdateEvery, v -> PortalViewRenderer.maxUpdateEvery = v,
            v -> v == 1 ? Component.translatable("quasar_portals.options.update.every")
                : Component.translatable("quasar_portals.options.update.value", v)));
        rows.addChild(slider("min_rate", 20, 240, 10,
            () -> PortalViewRenderer.minUpdatesPerSecond, v -> PortalViewRenderer.minUpdatesPerSecond = v,
            v -> Component.translatable("quasar_portals.options.min_rate.value", v)));
        rows.addChild(slider("resolution", 10, 100, 5,
            PortalSettings::resolutionPercent, v -> PortalViewRenderer.resolutionScale = v / 100f,
            v -> Component.literal(v + "%")));
        rows.addChild(slider("distance", 0, 32, 1,
            () -> Math.min(32, PortalViewRenderer.renderDistance), v -> PortalViewRenderer.renderDistance = v,
            v -> v == 0 ? Component.translatable("quasar_portals.options.distance.unlimited")
                : Component.translatable("quasar_portals.options.distance.value", v)));
        rows.addChild(Button.builder(translucentLabel(), b -> {
            PortalSettings.translucent = !PortalSettings.translucent;
            PortalSettings.save();
            b.setMessage(translucentLabel());
        }).tooltip(Tooltip.create(Component.translatable("quasar_portals.options.water_glass.tooltip"))).width(WIDTH).build());
        rows.addChild(Button.builder(cloudsLabel(), b -> {
            PortalSettings.clouds = !PortalSettings.clouds;
            PortalSettings.save();
            b.setMessage(cloudsLabel());
        }).tooltip(Tooltip.create(Component.translatable("quasar_portals.options.clouds.tooltip"))).width(WIDTH).build());
        rows.addChild(Button.builder(outlineLabel(), b -> {
            PortalPlaceholderOutline.enabled = !PortalPlaceholderOutline.enabled;
            PortalSettings.save();
            b.setMessage(outlineLabel());
        }).tooltip(Tooltip.create(Component.translatable("quasar_portals.options.outlines.tooltip"))).width(WIDTH).build());
        rows.addChild(Button.builder(Component.translatable("quasar_portals.options.reset"), b -> {
            PortalSettings.resetDefaults();
            rebuildWidgets();
        }).width(WIDTH).build());
        
        scrollArea = new ScrollableLayout(minecraft, rows, layout.getContentHeight());
        layout.addToContents(scrollArea);
        layout.addToFooter(Button.builder(CommonComponents.GUI_DONE, b -> onClose()).width(200).build());
        layout.visitWidgets(this::addRenderableWidget);
        repositionElements();
    }
    
    @Override
    protected void repositionElements() {
        scrollArea.arrangeElements();
        scrollArea.setMaxHeight(layout.getContentHeight());
        layout.arrangeElements();
    }
    
    @Override
    public void onClose() {
        PortalSettings.save();
        minecraft.gui.setScreen(parent);
    }
    
    private static Component translucentLabel() {
        return CommonComponents.optionNameValue(Component.translatable("quasar_portals.options.water_glass"),
            PortalSettings.translucent ? CommonComponents.OPTION_ON : CommonComponents.OPTION_OFF);
    }
    
    private static Component cloudsLabel() {
        return CommonComponents.optionNameValue(Component.translatable("quasar_portals.options.clouds"),
            PortalSettings.clouds ? CommonComponents.OPTION_ON : CommonComponents.OPTION_OFF);
    }
    
    private static Component outlineLabel() {
        return CommonComponents.optionNameValue(Component.translatable("quasar_portals.options.outlines"),
            PortalPlaceholderOutline.enabled ? CommonComponents.OPTION_ON : CommonComponents.OPTION_OFF);
    }
    
    private static AbstractSliderButton slider(String key, int min, int max, int step,
                                               IntSupplier getter, IntConsumer setter, IntFunction<Component> label) {
        Slider s = new Slider(key, min, max, step, getter, setter, label);
        s.setTooltip(Tooltip.create(Component.translatable("quasar_portals.options." + key + ".tooltip")));
        return s;
    }
    
    private static final class Slider extends AbstractSliderButton {
        private final String key;
        private final int min, max, step;
        private final IntSupplier getter;
        private final IntConsumer setter;
        private final IntFunction<Component> label;
        
        Slider(String key, int min, int max, int step, IntSupplier getter, IntConsumer setter, IntFunction<Component> label) {
            super(0, 0, WIDTH, 20, Component.empty(), (getter.getAsInt() - min) / (double) (max - min));
            this.key = key;
            this.min = min;
            this.max = max;
            this.step = step;
            this.getter = getter;
            this.setter = setter;
            this.label = label;
            updateMessage();
        }
        
        private int current() {
            int raw = (int) Math.round(min + value * (max - min));
            return Math.max(min, Math.min(max, min + Math.round((raw - min) / (float) step) * step));
        }
        
        @Override
        protected void updateMessage() {
            setMessage(CommonComponents.optionNameValue(
                Component.translatable("quasar_portals.options." + key), label.apply(current())));
        }
        
        @Override
        protected void applyValue() {
            int v = current();
            if (v != getter.getAsInt()) setter.accept(v);
        }
    }
}
