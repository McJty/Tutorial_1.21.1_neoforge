package com.example.tutorialmod.block.entity;

import com.example.tutorialmod.block.GeneratorBlock;
import com.example.tutorialmod.registration.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;

public final class GeneratorBlockEntity extends MachineBlockEntity {
    public static final int GENERATION_RATE = 20;
    public static final int CAPACITY = 10000;

    private int burnTicks;

    public GeneratorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.GENERATOR.get(), pos, state, CAPACITY, 0, GENERATION_RATE);
    }

    @Override
    public boolean accepts(ItemStack stack) {
        return stack.getBurnTime(RecipeType.SMELTING) > 0;
    }

    public int getBurnTicks() {
        return burnTicks;
    }

    @Override
    public void serverTick() {
        if (level == null) {
            return;
        }

        // Pause fuel use when the buffer cannot hold a full tick of generation.
        boolean generating = energy.getEnergyStored() <= CAPACITY - GENERATION_RATE;
        if (generating && burnTicks == 0) {
            ItemStack fuel = inventory.getStackInSlot(0);
            int duration = fuel.getBurnTime(RecipeType.SMELTING);
            if (duration > 0) {
                ItemStack remainder = fuel.getCraftingRemainingItem();
                inventory.extractItem(0, 1, false);
                if (!remainder.isEmpty()) {
                    if (inventory.getStackInSlot(0).isEmpty()) {
                        inventory.setStackInSlot(0, remainder);
                    } else {
                        Block.popResource(level, worldPosition.above(), remainder);
                    }
                }

                burnTicks = duration;
            }
        }

        generating &= burnTicks > 0;
        if (generating) {
            burnTicks--;
            setEnergy(energy.getEnergyStored() + GENERATION_RATE);
        }

        if (getBlockState().getValue(GeneratorBlock.ON) != generating) {
            level.setBlock(
                    worldPosition,
                    getBlockState().setValue(GeneratorBlock.ON, generating),
                    Block.UPDATE_CLIENTS
            );
        }

        // Each neighbour can receive one transfer of up to GENERATION_RATE.
        Direction[] directions = Direction.values();
        int start = (int) (level.getGameTime() % directions.length);
        for (int i = 0; i < directions.length; i++) {
            Direction side = directions[(start + i) % directions.length];
            BlockPos targetPos = worldPosition.relative(side);
            if (!level.hasChunkAt(targetPos)) {
                continue;
            }

            var target = level.getCapability(Capabilities.EnergyStorage.BLOCK, targetPos, side.getOpposite());
            if (target == null || !target.canReceive()) {
                continue;
            }

            int offered = energy.extractEnergy(GENERATION_RATE, true);
            int accepted = target.receiveEnergy(offered, false);
            energy.extractEnergy(accepted, false);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("burn_ticks", burnTicks);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        burnTicks = Math.max(0, tag.getInt("burn_ticks"));
    }
}
