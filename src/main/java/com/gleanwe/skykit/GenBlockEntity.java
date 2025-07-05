package com.gleanwe.skykit;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.ItemHandlerHelper;

public class GenBlockEntity extends BlockEntity {
    private ItemStack internalBuffer = ItemStack.EMPTY;
    private int maxBufferSize;

    public GenBlockEntity(BlockPos pos, BlockState blockState) {
        super(SkyKit.GEN_BLOCK_ENTITY.get(), pos, blockState);
        updateMaxBufferSize();
    }

    private void updateMaxBufferSize() {
        if (getBlockState().getBlock() instanceof GenBlock genBlock) {
            this.maxBufferSize = Config.baseBufferSize * (int) Math.pow(2, genBlock.tier - 1);
        } else {
            this.maxBufferSize = Config.baseBufferSize; // Fallback if block is not a GenBlock
        }
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, GenBlockEntity blockEntity) {
        if (level.getGameTime() % Config.tickRate == 0) {
            blockEntity.generateItems();
            blockEntity.pushToExternalInventory();
        }
    }

    private void generateItems() {
        if (getBlockState().getBlock() instanceof GenBlock genBlock) {
            int productionRate = (int) Math.pow(2, genBlock.tier - 1);
            ItemStack cobblestone = new ItemStack(Items.COBBLESTONE, productionRate);

            if (internalBuffer.isEmpty()) {
                internalBuffer = cobblestone;
            } else if (ItemStack.isSameItemSameComponents(internalBuffer, cobblestone)) {
                int canAdd = Math.min(productionRate, maxBufferSize - internalBuffer.getCount());
                internalBuffer.grow(canAdd);
            }

            setChanged();
        }
    }

    private void pushToExternalInventory() {
        if (internalBuffer.isEmpty()) return;

        for (Direction direction : Direction.Plane.HORIZONTAL) {
            BlockPos targetPos = getBlockPos().relative(direction);
            var itemHandler = level.getCapability(Capabilities.ItemHandler.BLOCK, targetPos, direction.getOpposite());

            if (itemHandler != null) {
                ItemStack remaining = ItemHandlerHelper.insertItem(itemHandler, internalBuffer, false);
                internalBuffer = remaining;
                setChanged();

                if (internalBuffer.isEmpty()) {
                    break; // Stop if the buffer is empty
                }
            }
        }

        BlockPos abovePos = getBlockPos().above();
        var itemHandlerAbove = level.getCapability(Capabilities.ItemHandler.BLOCK, abovePos, Direction.DOWN);
        if (itemHandlerAbove != null) {
            ItemStack remaining = ItemHandlerHelper.insertItem(itemHandlerAbove, internalBuffer, false);
            internalBuffer = remaining;
            setChanged();
        }

        BlockPos belowPos = getBlockPos().below();
        var itemHandlerBelow = level.getCapability(Capabilities.ItemHandler.BLOCK, belowPos, Direction.UP);
        if (itemHandlerBelow != null) {
            ItemStack remaining = ItemHandlerHelper.insertItem(itemHandlerBelow, internalBuffer, false);
            internalBuffer = remaining;
            setChanged();
        }
    }

    public ItemStack getBuffer() {
        return internalBuffer;
    }

    public void clearBuffer() {
        internalBuffer = ItemStack.EMPTY;
        setChanged();
    }

    public int getMaxBufferSize() {
        return maxBufferSize;
    }

    public int getCurrentBufferSize() {
        return internalBuffer.getCount();
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("Buffer")) {
            internalBuffer = ItemStack.parseOptional(registries, tag.getCompound("Buffer"));
        }
        maxBufferSize = tag.getInt("MaxBufferSize");
        if (maxBufferSize == 0) {
            updateMaxBufferSize(); // Ensure maxBufferSize is set if not present
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (!internalBuffer.isEmpty()) {
            tag.put("Buffer", internalBuffer.saveOptional(registries));
        }
        tag.putInt("MaxBufferSize", maxBufferSize);
    }
}
