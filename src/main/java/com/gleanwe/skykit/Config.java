package com.gleanwe.skykit;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

@EventBusSubscriber(modid = SkyKit.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    private static final ModConfigSpec.IntValue TICK_RATE = BUILDER
            .comment("Number of ticks between each generation tick for the generator block.")
            .defineInRange("tickRate", 20, 1, 600);

    private static final ModConfigSpec.IntValue BASE_BUFFER_SIZE = BUILDER
            .comment("Base buffer size for the generator block.")
            .defineInRange("baseBufferSize", 64, 1, 2048);

    static final ModConfigSpec SPEC = BUILDER.build();

    public static int tickRate;
    public static int baseBufferSize;

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
        tickRate = TICK_RATE.get();
        baseBufferSize = BASE_BUFFER_SIZE.get();
    }
}
