package com.gleanwe.skykit;


import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;

import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;

import java.util.function.Supplier;

@Mod(SkyKit.MOD_ID)
public class SkyKit {
    public static final String MOD_ID = "skykit";
    private static final Logger LOGGER = LogUtils.getLogger();

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MOD_ID);
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MOD_ID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, MOD_ID);
    public static final DeferredRegister<CreativeModeTab> CREATIVE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MOD_ID);

    public static final Supplier<CreativeModeTab> SKY_KIT_TAB = CREATIVE_TABS.register("sky_kit_tab",
            () -> CreativeModeTab.builder().icon(() -> new ItemStack(Items.ALLIUM)).title(Component.translatable("skykit.creativetab"))
                    .displayItems(((itemDisplayParameters, output) -> {
                        ITEMS.getEntries().forEach(itemDeferredHolder -> {
                            output.accept(itemDeferredHolder.value());
                        });
                    })).build());

    public static final DeferredBlock<Block> TIER_1 = registerBlock("tier_1_block",
            () -> new GenBlock(BlockBehaviour.Properties.of()
                    .strength(1f)
                    .noTerrainParticles()
                    .sound(SoundType.GILDED_BLACKSTONE)
                    .lightLevel(state -> 15), 1));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<GenBlockEntity>> GEN_BLOCK_ENTITY = BLOCK_ENTITIES.register("gen_block_entity",
            () -> BlockEntityType.Builder.of(GenBlockEntity::new, TIER_1.get()).build(null));

    public SkyKit(IEventBus modEventBus, ModContainer modContainer) {
        ITEMS.register(modEventBus);
        BLOCKS.register(modEventBus);
        BLOCK_ENTITIES.register(modEventBus);
        CREATIVE_TABS.register(modEventBus);

        modEventBus.addListener(this::addCreative);
        //modEventBus.addListener(this::gatherData);

        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

   // private void gatherData(FMLCommonSetupEvent event) {
   //     var generator = event.getGenerator();
   //     var packOutput = event.getPackOutput();
   //     var lookupProvider = event.getLookupProvider();
   //     var existingFileHelper = event.getExistingFileHelper();

        //generator.addProvider(event.includeClient(), new ModBlockStateProvider(packOutput, existingFileHelper));
        //generator.addProvider(event.includeClient(), new ModItemModelProvider(packOutput, existingFileHelper));
        //generator.addProvider(event.includeServer(), new ModRecipeProvider(packOutput, lookupProvider));
    //}

    private static DeferredBlock<Block> registerBlock(String name, Supplier<? extends Block> supplier) {
        DeferredBlock<Block> blockDeferredHolder = BLOCKS.register(name, supplier);
        ITEMS.registerSimpleBlockItem(name, blockDeferredHolder);
        return blockDeferredHolder;
    }
    private void commonSetup(FMLCommonSetupEvent event) {
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
    }

    // You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
    @EventBusSubscriber(modid = MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            // Some client setup code
            LOGGER.info("HELLO FROM CLIENT SETUP");
            LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().getUser().getName());
        }
    }
}

