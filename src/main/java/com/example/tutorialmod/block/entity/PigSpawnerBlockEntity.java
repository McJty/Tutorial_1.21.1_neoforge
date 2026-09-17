package com.example.tutorialmod.block.entity;

import com.example.tutorialmod.registration.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.energy.IEnergyStorage;

public final class PigSpawnerBlockEntity extends MachineBlockEntity {
    public static final int CAPACITY = GeneratorBlockEntity.GENERATION_RATE * 20 * 10;

    private boolean previewReady;
    private Pig previewPig;

    public final IEnergyStorage input = new IEnergyStorage() {
        @Override
        public int receiveEnergy(int amount, boolean simulate) {
            return energy.receiveEnergy(Math.min(amount, GeneratorBlockEntity.GENERATION_RATE), simulate);
        }

        @Override
        public int extractEnergy(int amount, boolean simulate) {
            return 0;
        }

        @Override
        public int getEnergyStored() {
            return energy.getEnergyStored();
        }

        @Override
        public int getMaxEnergyStored() {
            return CAPACITY;
        }

        @Override
        public boolean canExtract() {
            return false;
        }

        @Override
        public boolean canReceive() {
            return true;
        }
    };

    public PigSpawnerBlockEntity(BlockPos pos, BlockState state) {
        super(
                ModBlockEntities.PIG_SPAWNER.get(),
                pos,
                state,
                CAPACITY,
                GeneratorBlockEntity.GENERATION_RATE,
                0
        );
    }

    @Override
    public boolean accepts(ItemStack stack) {
        return stack.is(ItemTags.PIG_FOOD);
    }

    private boolean hasPreviewResources() {
        return energy.getEnergyStored() > 0 && accepts(inventory.getStackInSlot(0));
    }

    public boolean shouldShowPreview() {
        return level != null && level.isClientSide ? previewReady : hasPreviewResources();
    }

    public Pig getPreviewPig() {
        if (previewPig == null && level != null && level.isClientSide) {
            // The vanilla pig renderer draws visible invisible mobs with translucent blending.
            previewPig = new Pig(EntityType.PIG, level) {
                @Override
                public boolean isInvisibleTo(Player player) {
                    return false;
                }
            };
            previewPig.setInvisible(true);
        }
        return previewPig;
    }

    @Override
    public void setChanged() {
        super.setChanged();
        if (level instanceof ServerLevel server) {
            boolean ready = hasPreviewResources();
            if (ready != previewReady) {
                previewReady = ready;
                server.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
            }
        }
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        tag.putBoolean("preview_ready", hasPreviewResources());
        return tag;
    }

    @Override
    public void onDataPacket(Connection connection, ClientboundBlockEntityDataPacket packet,
                             HolderLookup.Provider registries) {
        previewReady = packet.getTag().getBoolean("preview_ready");
    }

    @Override
    public void handleUpdateTag(CompoundTag tag, HolderLookup.Provider registries) {
        previewReady = tag.getBoolean("preview_ready");
    }

    @Override
    public void serverTick() {
        if (!(level instanceof ServerLevel server) || energy.getEnergyStored() != CAPACITY
                || !accepts(inventory.getStackInSlot(0))) {
            return;
        }

        var pig = EntityType.PIG.create(server);
        if (pig == null) {
            return;
        }

        pig.moveTo(
                worldPosition.getX() + 0.5,
                worldPosition.getY() + 0.5,
                worldPosition.getZ() + 0.5,
                server.random.nextFloat() * 360,
                0
        );
        if (!server.noCollision(pig) || server.containsAnyLiquid(pig.getBoundingBox())
                || !server.getEntities(pig, pig.getBoundingBox()).isEmpty()) {
            return;
        }

        if (server.addFreshEntity(pig)) {
            inventory.extractItem(0, 1, false);
            setEnergy(0);
        }
    }
}
