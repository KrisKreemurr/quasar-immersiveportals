package qouteall.imm_ptl.peripheral;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.chunk.ChunkGenerator;
import qouteall.imm_ptl.core.McHelper;
import qouteall.imm_ptl.peripheral.alternate_dimension.ChaosBiomeSource;
import qouteall.imm_ptl.peripheral.alternate_dimension.ErrorTerrainGenerator;
import qouteall.imm_ptl.peripheral.alternate_dimension.FormulaGenerator;
import qouteall.imm_ptl.peripheral.alternate_dimension.NormalSkylandGenerator;
import qouteall.imm_ptl.peripheral.dim_stack.DimStackManagement;
import qouteall.imm_ptl.peripheral.portal_generation.IntrinsicPortalGeneration;
import qouteall.imm_ptl.peripheral.wand.ClientPortalWandPortalDrag;
import qouteall.imm_ptl.peripheral.wand.PortalWandInteraction;
import qouteall.imm_ptl.peripheral.wand.PortalWandItem;

import java.util.function.BiConsumer;

public class PeripheralModMain {

    // TODO @Nick1st - Rework registry (Best would be at fabrics side)
    public static Block portalHelperBlock;

    public static final CreativeModeTab TAB = CreativeModeTab.builder()
            .icon(() -> new ItemStack(BuiltInRegistries.ITEM.getValue(Identifier.fromNamespaceAndPath("immersive_portals", "portal_helper")))) // Quasar MC: wand parked
            .title(Component.translatable("imm_ptl.item_group"))
            .displayItems((enabledFeatures, entries) -> {
                PortalWandItem.addIntoCreativeTag(entries);
                
                CommandStickItem.addIntoCreativeTag(entries);
                
                entries.accept(BuiltInRegistries.ITEM.getValue(Identifier.fromNamespaceAndPath("immersive_portals", "portal_helper")));
            })
            .build();
    
    //@OnlyIn(Dist.CLIENT)
    public static void initClient() {
        IPOuterClientMisc.initClient();
        
        PortalWandItem.initClient();
        
        ClientPortalWandPortalDrag.init();
    }
    
    public static void init() {
        FormulaGenerator.init();
        
        IntrinsicPortalGeneration.init();
        
        DimStackManagement.init();

        // TODO @Nick1st - DynDim removal
//        AlternateDimensions.init();
//
//        DimensionAPI.suppressExperimentalWarningForNamespace("immersive_portals");

        PortalWandItem.init();
        
        CommandStickItem.init();
        
        PortalWandInteraction.init();
        
        CommandStickItem.registerCommandStickTypes();

    }
    
    public static void registerItems(BiConsumer<Identifier, Item> regFunc) {
        regFunc.accept(
            McHelper.newResourceLocation("immersive_portals", "portal_helper"),
                new PortalHelperItem(PeripheralModMain.portalHelperBlock,
                    new Item.Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, net.minecraft.resources.Identifier.fromNamespaceAndPath("immersive_portals", "portal_helper"))).useBlockDescriptionPrefix())
        );

        // Quasar MC: command stick and portal wand are parked (not registered) until they are ported.
    }
    
    public static void registerBlocks(BiConsumer<Identifier, Block> regFunc) {
        Block block = new Block(BlockBehaviour.Properties.of().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.BLOCK, net.minecraft.resources.Identifier.fromNamespaceAndPath("immersive_portals", "portal_helper")))
            .noOcclusion().isRedstoneConductor((a, b, c) -> false));
        regFunc.accept(
            McHelper.newResourceLocation("immersive_portals", "portal_helper"),
                block
        );
        portalHelperBlock = block;
    }

    public static void registerChunkGenerators(
        BiConsumer<Identifier, MapCodec<? extends ChunkGenerator>> regFunc
    ) {
        // Quasar MC: alternate-dimension generators are parked.
    }

    public static void registerBiomeSources(
        BiConsumer<Identifier, MapCodec<? extends BiomeSource>> regFunc
    ) {
        // Quasar MC: alternate-dimension biome source is parked.
    }

    public static void registerCreativeTabs(
        BiConsumer<Identifier, CreativeModeTab> regFunc
    ) {
        regFunc.accept(
            McHelper.newResourceLocation("immersive_portals", "general"),
            TAB
        );
    }
}
