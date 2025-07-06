package com.gleanwe.skykit;


import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.AnvilBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
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
            () -> CreativeModeTab.builder().icon(() -> new ItemStack(SkyKit.MOAI_TOTEM.get())).title(Component.translatable("skykit.creativetab"))
                    .displayItems(((itemDisplayParameters, output) -> {
                        ITEMS.getEntries().forEach(itemDeferredHolder -> {
                            output.accept(itemDeferredHolder.value());
                        });
                    })).build());

    public static final DeferredBlock<Block> TIER_1 = registerBlock("tier_1_block",
            () -> new GenBlock(BlockBehaviour.Properties.of()
                    .strength(1f)
                    .sound(SoundType.GILDED_BLACKSTONE)
                    .lightLevel(state -> 15), 1));

    public static final DeferredBlock<Block> TIER_2 = registerBlock("tier_2_block",
            () -> new GenBlock(BlockBehaviour.Properties.of()
                    .strength(1f)
                    .sound(SoundType.METAL)
                    .lightLevel(state -> 15), 2));

    public static final DeferredBlock<Block> TIER_3 = registerBlock("tier_3_block",
            () -> new GenBlock(BlockBehaviour.Properties.of()
                    .strength(1f)
                    .sound(SoundType.METAL)
                    .lightLevel(state -> 15), 3));

    public static final DeferredBlock<Block> TIER_4 = registerBlock("tier_4_block",
            () -> new GenBlock(BlockBehaviour.Properties.of()
                    .strength(1f)
                    .sound(SoundType.METAL)
                    .lightLevel(state -> 15), 4));

    public static final DeferredBlock<Block> TIER_5 = registerBlock("tier_5_block",
            () -> new GenBlock(BlockBehaviour.Properties.of()
                    .strength(1f)
                    .sound(SoundType.METAL)
                    .lightLevel(state -> 15), 5));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<GenBlockEntity>> GEN_BLOCK_ENTITY = BLOCK_ENTITIES.register("gen_block_entity",
            () -> BlockEntityType.Builder.of(GenBlockEntity::new,
                    TIER_1.get(), TIER_2.get(), TIER_3.get(), TIER_4.get(), TIER_5.get()).build(null));

    public static final DeferredBlock<Block> MOAI_MACHINE = registerBlock("moai_machine",
            () -> new MoaiMachine(BlockBehaviour.Properties.of()
                    .strength(1f)
                    .sound(SoundType.METAL)
                    .requiresCorrectToolForDrops()));

    public static final DeferredBlock<Block> MOAI_TOTEM = registerBlock("moai_totem",
            () -> new MoaiTotem(BlockBehaviour.Properties.of()
                    .strength(1f)
                    .sound(SoundType.METAL)
                    .requiresCorrectToolForDrops()));

    public SkyKit(IEventBus modEventBus, ModContainer modContainer) {
        ITEMS.register(modEventBus);
        BLOCKS.register(modEventBus);
        BLOCK_ENTITIES.register(modEventBus);
        CREATIVE_TABS.register(modEventBus);

        modEventBus.addListener(this::addCreative);

        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

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

        @SubscribeEvent
        public static void registerBlockColors(RegisterColorHandlersEvent.Block event) {
            event.register((state, level, pos, tintIndex) -> {
                if (level != null && pos != null && tintIndex == 0) {
                    return BiomeColors.getAverageWaterColor(level, pos);
                }
                return 0x3F76E4; // Default water color
            }, TIER_1.get(), TIER_2.get(), TIER_3.get(), TIER_4.get(), TIER_5.get());
        }

        @SubscribeEvent
        public static void registerItemColors(RegisterColorHandlersEvent.Item event) {
            event.register((stack, tintIndex) -> {
                if (tintIndex == 0) {
                    var player = Minecraft.getInstance().player;
                    var level = Minecraft.getInstance().level;
                    if (level != null && player != null) {
                        return BiomeColors.getAverageWaterColor(level, player.blockPosition());
                    }
                }
                return 0x3F76E4; // Default water color
            }, TIER_1.get(), TIER_2.get(), TIER_3.get(), TIER_4.get(), TIER_5.get());
        }
    }
}

