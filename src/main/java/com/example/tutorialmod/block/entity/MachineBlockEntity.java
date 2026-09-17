package com.example.tutorialmod.block.entity;

import com.example.tutorialmod.menu.MachineMenu;
import com.example.tutorialmod.registration.ModDataComponents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.energy.IEnergyStorage;

public abstract class MachineBlockEntity extends BlockEntity implements MenuProvider {
    private final int capacity;
    private final int receiveRate;
    private final int extractRate;
    private int storedEnergy;

    public final IEnergyStorage energy = new IEnergyStorage() {
        @Override
        public int receiveEnergy(int amount, boolean simulate) {
            int received = Math.clamp(amount, 0, Math.min(receiveRate, capacity - storedEnergy));
            if (!simulate && received > 0) {
                setEnergy(storedEnergy + received);
            }
            return received;
        }

        @Override
        public int extractEnergy(int amount, boolean simulate) {
            int extracted = Math.clamp(amount, 0, Math.min(extractRate, storedEnergy));
            if (!simulate && extracted > 0) {
                setEnergy(storedEnergy - extracted);
            }
            return extracted;
        }

        @Override
        public int getEnergyStored() {
            return storedEnergy;
        }

        @Override
        public int getMaxEnergyStored() {
            return capacity;
        }

        @Override
        public boolean canExtract() {
            return extractRate > 0;
        }

        @Override
        public boolean canReceive() {
            return receiveRate > 0;
        }
    };

    public final ItemStackHandler inventory = new ItemStackHandler(1) {
        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return accepts(stack);
        }

        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }
    };

    public final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> energy.getEnergyStored();
                case 1 -> energy.getMaxEnergyStored();
                case 2 -> MachineBlockEntity.this instanceof GeneratorBlockEntity generator ? generator.getBurnTicks() : 0;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
        }

        @Override
        public int getCount() {
            return 3;
        }
    };

    protected MachineBlockEntity(
            BlockEntityType<?> type,
            BlockPos pos,
            BlockState state,
            int capacity,
            int receive,
            int extract
    ) {
        super(type, pos, state);
        this.capacity = capacity;
        receiveRate = receive;
        extractRate = extract;
    }

    public void setEnergy(int amount) {
        int next = Math.clamp(amount, 0, capacity);
        if (storedEnergy != next) {
            storedEnergy = next;
            setChanged();
        }
    }

    public abstract boolean accepts(ItemStack stack);

    public abstract void serverTick();

    @Override
    public Component getDisplayName() {
        return getBlockState().getBlock().getName();
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory playerInventory, Player player) {
        return new MachineMenu(id, playerInventory, this);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("energy", energy.getEnergyStored());
        tag.put("inventory", inventory.serializeNBT(registries));
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        setEnergy(tag.getInt("energy"));
        inventory.deserializeNBT(registries, tag.getCompound("inventory"));
    }

    @Override
    protected void collectImplicitComponents(DataComponentMap.Builder components) {
        super.collectImplicitComponents(components);
        components.set(ModDataComponents.ENERGY.get(), energy.getEnergyStored());
    }

    @Override
    protected void applyImplicitComponents(DataComponentInput components) {
        super.applyImplicitComponents(components);
        setEnergy(components.getOrDefault(ModDataComponents.ENERGY.get(), 0));
    }

    @SuppressWarnings("deprecation")
    @Override
    public void removeComponentsFromTag(CompoundTag tag) {
        super.removeComponentsFromTag(tag);
        tag.remove("energy");
    }
}
