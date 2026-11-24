// QUASAR STUB: parked peripheral feature (dimension stack / alternate dimensions / portal wand / command stick), see src/parked.
package qouteall.imm_ptl.peripheral;

import net.minecraft.server.permissions.Permissions;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import qouteall.imm_ptl.core.IPGlobal;
import qouteall.imm_ptl.core.commands.PortalCommand;
import qouteall.imm_ptl.peripheral.platform_specific.PeripheralModEntry;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

// QUASAR STUB: generated no-op stand-in; the original lives in src/parked (render rewrite pending).

public class CommandStickItem extends Item {

    private static Logger LOGGER;
    public static Codec<Data> DATA_CODEC;
    public static DeferredHolder<DataComponentType<?>, DataComponentType<Data>> COMPONENT_TYPE;
    public record Data(
        String command, String nameTranslationKey, List<String> descriptionTranslationKeys
    ) {

        public void serialize(CompoundTag tag) {}

        public CompoundTag toTag() { return null; }

        public static Data deserialize(CompoundTag tag) { return null; }

    }

    public static LinkedHashMap<String, Data> BUILT_IN_COMMAND_STICK_TYPES;
    public static void registerBuiltInCommandStick(Data data) {}

    public static CommandStickItem instance;
    public CommandStickItem(Properties settings) { super(settings); }

    public boolean isFoil(ItemStack stack) { return false; }


    private void doUse(Player player, ItemStack stack) {}

    private static boolean canUseCommand(Player player) { return false; }

    public void appendHoverText(
        ItemStack stack, Item.TooltipContext tooltipContext,
        List<Component> tooltip, TooltipFlag tooltipFlag
    ) {}

    public @NotNull String getDescriptionId(ItemStack stack) { return null; }

    public static void sendMessage(Player player, Component message) {}

    public static void init() {}

    public static void addIntoCreativeTag(CreativeModeTab.Output entries) {}

    public static void registerCommandStickTypes() {}

    private static Data registerPortalSubCommandStick(String name) { return null; }

    private static Data registerPortalSubCommandStick(String name, String subCommand) { return null; }


}

