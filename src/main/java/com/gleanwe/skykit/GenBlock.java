package com.gleanwe.skykit;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

import java.util.List;


public class GenBlock extends BaseEntityBlock {
    public static final MapCodec<GenBlock> CODEC = RecordCodecBuilder.mapCodec(builder ->
            builder.group(
                    propertiesCodec(),
                    Codec.INT.fieldOf("tier").forGetter(block -> block.tier)
            ).apply(builder, GenBlock::new));

    public final int tier;

    public GenBlock(Properties properties, int tier) {
        super(properties);
        this.tier = tier;
    }

    @Override
    public MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new GenBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide ? null : createTickerHelper(type, SkyKit.GEN_BLOCK_ENTITY.get(), GenBlockEntity::serverTick);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (level.getBlockEntity(pos) instanceof GenBlockEntity blockEntity) {
            if (!blockEntity.getBuffer().isEmpty()) {
                var itemInHand = player.getItemInHand(InteractionHand.MAIN_HAND);
                boolean isShiftClick = player.isShiftKeyDown();

                if (ItemStack.isSameItemSameComponents(itemInHand, blockEntity.getBuffer())) {
                    int addedCount;
                    if (!isShiftClick) {
                        addedCount = Math.min(itemInHand.getMaxStackSize() - itemInHand.getCount(), blockEntity.getBuffer().getCount());
                    } else {
                        addedCount = Math.min(1, Math.min(itemInHand.getMaxStackSize() - itemInHand.getCount(), blockEntity.getBuffer().getCount()));
                    }
                    itemInHand.grow(addedCount);
                    blockEntity.getBuffer().shrink(addedCount);
                } else if (itemInHand.isEmpty()) {
                    var addedStack = blockEntity.getBuffer().copy();
                    if (!isShiftClick) {
                        addedStack.setCount(Math.min(addedStack.getCount(), addedStack.getMaxStackSize()));
                    } else {
                        addedStack.setCount(1);
                    }

                    player.setItemInHand(InteractionHand.MAIN_HAND, addedStack);

                    if (addedStack.getCount() >= blockEntity.getBuffer().getCount()) {
                        blockEntity.clearBuffer();
                    } else {
                        blockEntity.getBuffer().shrink(addedStack.getCount());
                    }
                }
            }
            return InteractionResult.sidedSuccess(level.isClientSide());
        }
        return super.useWithoutItem(state, level, pos, player, hitResult);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        tooltip.add(Component.translatable("tooltip.skykit.genblock.tier", this.tier).withStyle(ChatFormatting.GOLD));
        int productionRate = (int) Math.pow(2, this.tier - 1);
        tooltip.add(Component.translatable("tooltip.skykit.genblock.production", productionRate).withStyle(ChatFormatting.GRAY));
    }
}
