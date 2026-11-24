// QUASAR STUB: parked peripheral feature (dimension stack / alternate dimensions / portal wand / command stick), see src/parked.
package qouteall.imm_ptl.peripheral.wand;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.Codec;
import de.nick1st.imm_ptl.events.ClientCleanupEvent;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import qouteall.imm_ptl.core.IPCGlobal;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import qouteall.imm_ptl.core.IPMcHelper;
import qouteall.imm_ptl.core.block_manipulation.BlockManipulationServer;
import qouteall.imm_ptl.peripheral.platform_specific.PeripheralModEntry;
import java.util.ArrayList;
import java.util.List;

// QUASAR STUB: generated no-op stand-in; the original lives in src/parked (render rewrite pending).

public class PortalWandItem extends Item {

    public static PortalWandItem instance;
    public static Codec<Mode> MODE_CODEC;
    public static DeferredHolder<DataComponentType<?>, DataComponentType<Mode>> COMPONENT_TYPE;
    public static void init() {}

    public static void initClient() {}

    public static void addIntoCreativeTag(CreativeModeTab.Output entries) {}

    public static enum Mode {

        CREATE_PORTAL, DRAG_PORTAL, COPY_PORTAL;
        public static Mode FALLBACK;
        public static Mode fromTag(CompoundTag tag) { return null; }

        public static Mode fromStr(String mode) { return null; }

        public Mode next() { return null; }

        public CompoundTag toTag() { return null; }

        public String toStr() { return null; }

        public MutableComponent getText() { return null; }

    }

    public PortalWandItem(Properties properties) { super(properties); }

    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) { return null; }

    private void onUseClient(Mode mode) {}

    public void appendHoverText(
        ItemStack stack, Item.TooltipContext tooltipContext,
        List<Component> tooltip, TooltipFlag tooltipFlag
    ) {}

    public Component getName(ItemStack stack) { return null; }

    public static void showSettings(Player player) {}

    private static boolean instructionInformed;
    private static void updateDisplay(ItemStack itemStack) {}

    public static void clientRender(
        net.minecraft.client.player.LocalPlayer player, ItemStack itemStack, PoseStack poseStack, MultiBufferSource.BufferSource bufferSource,
        double camX, double camY, double camZ
    ) {}

    protected PortalWandItem() {}

}

