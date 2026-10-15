package com.example.tutorialmod.block.entity;

import com.example.tutorialmod.multiblock.ReceiverSavedData;
import com.example.tutorialmod.registration.ModBlockEntities;
import com.example.tutorialmod.registration.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/** One small BE type is shared by the destination controller and receiver. */
public final class ColorControllerBlockEntity extends BlockEntity {
    private DyeColor color1 = DyeColor.WHITE;
    private DyeColor color2 = DyeColor.WHITE;
    private DyeColor color3 = DyeColor.WHITE;

    public ColorControllerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.COLOR_CONTROLLER.get(), pos, state);
    }

    public DyeColor getColor1() {
        return color1;
    }

    public DyeColor getColor2() {
        return color2;
    }

    public DyeColor getColor3() {
        return color3;
    }

    public DyeColor getColor(int slot) {
        return switch (slot) {
            case 0 -> color1;
            case 1 -> color2;
            case 2 -> color3;
            default -> throw new IllegalArgumentException("Invalid color slot: " + slot);
        };
    }

    public boolean isReceiver() {
        return getBlockState().is(ModBlocks.RECEIVER.get());
    }

    public boolean setColor(int slot, DyeColor color) {
        if (slot < 0 || slot >= 3) {
            return false;
        }
        if (getColor(slot) == color) {
            return false;
        }
        switch (slot) {
            case 0 -> color1 = color;
            case 1 -> color2 = color;
            case 2 -> color3 = color;
        }
        setChanged();
        if (level instanceof ServerLevel server) {
            registerReceiver(server);
            server.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
        return true;
    }

    private void registerReceiver(ServerLevel server) {
        if (isReceiver()) {
            ReceiverSavedData.get(server).setReceiver(GlobalPos.of(server.dimension(), worldPosition),
                    color1, color2, color3);
        }
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (level instanceof ServerLevel server) {
            registerReceiver(server);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putByte("color1", (byte) color1.getId());
        tag.putByte("color2", (byte) color2.getId());
        tag.putByte("color3", (byte) color3.getId());
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        color1 = DyeColor.byId(tag.getByte("color1"));
        color2 = DyeColor.byId(tag.getByte("color2"));
        color3 = DyeColor.byId(tag.getByte("color3"));
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
