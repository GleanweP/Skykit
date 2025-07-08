package com.gleanwe.skykit;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class MoaiTotem extends FallingBlock {
    public static final MapCodec<MoaiTotem> CODEC = simpleCodec(MoaiTotem::new);
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;

    // Base components from JSON model (NORTH facing)
    private static final VoxelShape BASE = Block.box(4.002, 0.002, 6.002, 11.998, 3.998, 10.998);
    private static final VoxelShape MAIN_BODY = Block.box(4, 3, 5, 12, 14, 11);
    private static final VoxelShape NOSE = Block.box(6, 4, 4, 10, 5, 5);
    private static final VoxelShape LEFT_ARM = Block.box(3, 5.07596, 7.86824, 4, 16.07596, 10.86824);
    private static final VoxelShape RIGHT_ARM = Block.box(12, 5, 7, 13, 16, 10);
    private static final VoxelShape HEAD_LOWER = Block.box(4, 14, 4, 12, 18, 11);
    private static final VoxelShape HEAD_UPPER = Block.box(4, 18, 6, 12, 20, 11);
    private static final VoxelShape BACK_LOWER = Block.box(4, 3, 11, 12, 14, 12);
    private static final VoxelShape BACK_UPPER = Block.box(4, 14, 11, 12, 18, 13);
    private static final VoxelShape NOSE_ROTATED = Block.box(6, 5.70639, 2.77332, 10, 13.70639, 5.77332);

    // Combined shapes for different orientations
    private static final VoxelShape NORTH_SHAPE = Shapes.or(BASE, MAIN_BODY, NOSE, LEFT_ARM, RIGHT_ARM,
            HEAD_LOWER, HEAD_UPPER, BACK_LOWER, BACK_UPPER, NOSE_ROTATED);

    private static final VoxelShape EAST_SHAPE = createEastShape();
    private static final VoxelShape SOUTH_SHAPE = createSouthShape();
    private static final VoxelShape WEST_SHAPE = createWestShape();

    private static final float FALL_DAMAGE_PER_DISTANCE = 20.0F;
    private static final int FALL_DAMAGE_MAX = 300;

    public MapCodec<MoaiTotem> codec() {
        return CODEC;
    }

    public MoaiTotem(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState((BlockState)((BlockState)this.stateDefinition.any()).setValue(FACING, Direction.NORTH));
    }

    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return (BlockState)this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        Direction direction = (Direction)state.getValue(FACING);
        return switch (direction) {
            case NORTH -> NORTH_SHAPE;
            case EAST -> EAST_SHAPE;
            case SOUTH -> SOUTH_SHAPE;
            case WEST -> WEST_SHAPE;
            default -> NORTH_SHAPE;
        };
    }

    protected void falling(FallingBlockEntity fallingEntity) {
        fallingEntity.setHurtsEntities(FALL_DAMAGE_PER_DISTANCE, FALL_DAMAGE_MAX);
    }

    public void onLand(Level level, BlockPos pos, BlockState state, BlockState replaceableState, FallingBlockEntity fallingBlock) {
        if (!fallingBlock.isSilent()) {
            level.levelEvent(1031, pos, 0);
        }
    }

    public void onBrokenAfterFall(Level level, BlockPos pos, FallingBlockEntity fallingBlock) {
        if (!fallingBlock.isSilent()) {
            level.levelEvent(1029, pos, 0);
        }
    }

    public DamageSource getFallDamageSource(Entity entity) {
        return entity.damageSources().fallingBlock(entity);
    }

    protected BlockState rotate(BlockState state, Rotation rot) {
        return state.setValue(FACING, rot.rotate(state.getValue(FACING)));
    }

    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(new Property[]{FACING});
    }

    protected boolean isPathfindable(BlockState state, PathComputationType pathComputationType) {
        return false;
    }

    public int getDustColor(BlockState state, BlockGetter reader, BlockPos pos) {
        return state.getMapColor(reader, pos).col;
    }

    // Create shape for EAST facing (90 degrees clockwise from NORTH)
    private static VoxelShape createEastShape() {
        VoxelShape rotatedBase = Block.box(6.002, 0.002, 4.002, 10.998, 3.998, 11.998);
        VoxelShape rotatedMainBody = Block.box(5, 3, 4, 11, 14, 12);
        VoxelShape rotatedNose = Block.box(11, 4, 6, 12, 5, 10);
        VoxelShape rotatedLeftArm = Block.box(5.13176, 5.07596, 3, 8.13176, 16.07596, 4);
        VoxelShape rotatedRightArm = Block.box(6, 5, 12, 9, 16, 13);
        VoxelShape rotatedHeadLower = Block.box(5, 14, 4, 12, 18, 12);
        VoxelShape rotatedHeadUpper = Block.box(5, 18, 4, 11, 20, 12);
        VoxelShape rotatedBackLower = Block.box(4, 3, 4, 5, 14, 12);
        VoxelShape rotatedBackUpper = Block.box(3, 14, 4, 5, 18, 12);
        VoxelShape rotatedNoseRotated = Block.box(10.22668, 5.70639, 6, 13.22668, 13.70639, 10);

        return Shapes.or(rotatedBase, rotatedMainBody, rotatedNose, rotatedLeftArm,
                rotatedRightArm, rotatedHeadLower, rotatedHeadUpper, rotatedBackLower,
                rotatedBackUpper, rotatedNoseRotated);
    }

    // Create shape for SOUTH facing (180 degrees from NORTH)
    private static VoxelShape createSouthShape() {
        VoxelShape rotatedBase = Block.box(4.002, 0.002, 5.002, 11.998, 3.998, 9.998);
        VoxelShape rotatedMainBody = Block.box(4, 3, 5, 12, 14, 11);
        VoxelShape rotatedNose = Block.box(6, 4, 11, 10, 5, 12);
        VoxelShape rotatedLeftArm = Block.box(12, 5.07596, 5.13176, 13, 16.07596, 8.13176);
        VoxelShape rotatedRightArm = Block.box(3, 5, 6, 4, 16, 9);
        VoxelShape rotatedHeadLower = Block.box(4, 14, 5, 12, 18, 12);
        VoxelShape rotatedHeadUpper = Block.box(4, 18, 5, 12, 20, 10);
        VoxelShape rotatedBackLower = Block.box(4, 3, 4, 12, 14, 5);
        VoxelShape rotatedBackUpper = Block.box(4, 14, 3, 12, 18, 5);
        VoxelShape rotatedNoseRotated = Block.box(6, 5.70639, 10.22668, 10, 13.70639, 13.22668);

        return Shapes.or(rotatedBase, rotatedMainBody, rotatedNose, rotatedLeftArm,
                rotatedRightArm, rotatedHeadLower, rotatedHeadUpper, rotatedBackLower,
                rotatedBackUpper, rotatedNoseRotated);
    }

    // Create shape for WEST facing (270 degrees clockwise from NORTH)
    private static VoxelShape createWestShape() {
        VoxelShape rotatedBase = Block.box(5.002, 0.002, 4.002, 9.998, 3.998, 11.998);
        VoxelShape rotatedMainBody = Block.box(5, 3, 4, 11, 14, 12);
        VoxelShape rotatedNose = Block.box(4, 4, 6, 5, 5, 10);
        VoxelShape rotatedLeftArm = Block.box(7.86824, 5.07596, 12, 10.86824, 16.07596, 13);
        VoxelShape rotatedRightArm = Block.box(7, 5, 3, 10, 16, 4);
        VoxelShape rotatedHeadLower = Block.box(4, 14, 4, 11, 18, 12);
        VoxelShape rotatedHeadUpper = Block.box(5, 18, 4, 11, 20, 12);
        VoxelShape rotatedBackLower = Block.box(11, 3, 4, 12, 14, 12);
        VoxelShape rotatedBackUpper = Block.box(11, 14, 4, 13, 18, 12);
        VoxelShape rotatedNoseRotated = Block.box(2.77332, 5.70639, 6, 5.77332, 13.70639, 10);

        return Shapes.or(rotatedBase, rotatedMainBody, rotatedNose, rotatedLeftArm,
                rotatedRightArm, rotatedHeadLower, rotatedHeadUpper, rotatedBackLower,
                rotatedBackUpper, rotatedNoseRotated);
    }
}