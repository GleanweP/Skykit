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

            if (internalBuffer.isEmpty()) {
                // Create a new ItemStack with the production rate, but don't exceed max buffer size
                int initialAmount = Math.min(productionRate, maxBufferSize);
                internalBuffer = new ItemStack(Items.COBBLESTONE, initialAmount);
            } else if (internalBuffer.is(Items.COBBLESTONE)) {
                // Add items to existing buffer, respecting max buffer size
                int canAdd = Math.min(productionRate, maxBufferSize - internalBuffer.getCount());
                if (canAdd > 0) {
                    internalBuffer.grow(canAdd);
                }
            }

            setChanged();
        }
    }

    private void pushToExternalInventory() {
        if (internalBuffer.isEmpty()) return;

        // Try to push to horizontal neighbors first
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            if (internalBuffer.isEmpty()) break;

            BlockPos targetPos = getBlockPos().relative(direction);
            var itemHandler = level.getCapability(Capabilities.ItemHandler.BLOCK, targetPos, direction.getOpposite());

            if (itemHandler != null) {
                internalBuffer = ItemHandlerHelper.insertItem(itemHandler, internalBuffer, false);
                setChanged();
            }
        }

        // Try to push above
        if (!internalBuffer.isEmpty()) {
            BlockPos abovePos = getBlockPos().above();
            var itemHandlerAbove = level.getCapability(Capabilities.ItemHandler.BLOCK, abovePos, Direction.DOWN);
            if (itemHandlerAbove != null) {
                internalBuffer = ItemHandlerHelper.insertItem(itemHandlerAbove, internalBuffer, false);
                setChanged();
            }
        }

        // Try to push below
        if (!internalBuffer.isEmpty()) {
            BlockPos belowPos = getBlockPos().below();
            var itemHandlerBelow = level.getCapability(Capabilities.ItemHandler.BLOCK, belowPos, Direction.UP);
            if (itemHandlerBelow != null) {
                internalBuffer = ItemHandlerHelper.insertItem(itemHandlerBelow, internalBuffer, false);
                setChanged();
            }
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

        // Load the buffer with special handling for large counts
        if (tag.contains("BufferItem") && tag.contains("BufferCount")) {
            String itemId = tag.getString("BufferItem");
            int count = tag.getInt("BufferCount");

            if (!itemId.isEmpty() && count > 0) {
                // Create ItemStack with the stored count (can exceed normal stack limits)
                internalBuffer = new ItemStack(Items.COBBLESTONE, count);
            }
        }

        maxBufferSize = tag.getInt("MaxBufferSize");
        if (maxBufferSize == 0) {
            updateMaxBufferSize(); // Ensure maxBufferSize is set if not present
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);

        // Save buffer using custom method to avoid max_stack_size issues
        if (!internalBuffer.isEmpty()) {
            tag.putString("BufferItem", internalBuffer.getItem().toString());
            tag.putInt("BufferCount", internalBuffer.getCount());
        }

        tag.putInt("MaxBufferSize", maxBufferSize);
    }
}
