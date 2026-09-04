package com.example.tutorialmod.block;

import com.example.tutorialmod.block.entity.TutorialBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;

public final class TutorialBlock extends Block implements EntityBlock {
    public static final BooleanProperty ON = BooleanProperty.create("on");

    // EntityBlock's default ticker is null; this block entity only stores data.

    public TutorialBlock(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(ON, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(ON);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos position, BlockState state) {
        return new TutorialBlockEntity(position, state);
    }

    @Override
    protected InteractionResult useWithoutItem(
            BlockState state,
            Level level,
            BlockPos position,
            Player player,
            BlockHitResult hitResult
    ) {
        if (!player.getMainHandItem().isEmpty()) {
            return InteractionResult.PASS;
        }

        if (!level.isClientSide()
                && level.getBlockEntity(position) instanceof TutorialBlockEntity blockEntity) {
            blockEntity.toggle();
        }

        return InteractionResult.sidedSuccess(level.isClientSide());
    }
}
