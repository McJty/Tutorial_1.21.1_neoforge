package com.example.tutorialmod.block;

import com.example.tutorialmod.block.entity.PigSpawnerBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class PigSpawnerBlock extends MachineBlock {
    private static final VoxelShape PLATFORM = box(0, 0, 0, 16, 8, 16);
    private static final VoxelShape SHAPE = Shapes.or(
            PLATFORM,
            box(0, 8, 0, 2, 16, 2),
            box(14, 8, 0, 16, 16, 2),
            box(0, 8, 14, 2, 16, 16),
            box(14, 8, 14, 16, 16, 16)
    );

    public PigSpawnerBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected VoxelShape getShape(
            BlockState state,
            BlockGetter level,
            BlockPos pos,
            CollisionContext context
    ) {
        return SHAPE;
    }

    @Override
    protected VoxelShape getCollisionShape(
            BlockState state,
            BlockGetter level,
            BlockPos pos,
            CollisionContext context
    ) {
        return PLATFORM;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new PigSpawnerBlockEntity(pos, state);
    }
}
