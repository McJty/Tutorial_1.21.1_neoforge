package com.example.tutorialmod.registration;

import com.example.tutorialmod.TutorialMod;
import com.example.tutorialmod.block.entity.GeneratorBlockEntity;
import com.example.tutorialmod.block.entity.PigSpawnerBlockEntity;
import com.example.tutorialmod.block.entity.TutorialBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, TutorialMod.MOD_ID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TutorialBlockEntity>> TUTORIAL_BLOCK_ENTITY =
            BLOCK_ENTITY_TYPES.register(
                    "tutorial_block",
                    () -> BlockEntityType.Builder.of(
                            TutorialBlockEntity::new,
                            ModBlocks.TUTORIAL_BLOCK.get()
                    ).build(null)
            );

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<GeneratorBlockEntity>> GENERATOR =
            BLOCK_ENTITY_TYPES.register(
                    "generator",
                    () -> BlockEntityType.Builder.of(
                            GeneratorBlockEntity::new,
                            ModBlocks.GENERATOR.get()
                    ).build(null)
            );

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<PigSpawnerBlockEntity>> PIG_SPAWNER =
            BLOCK_ENTITY_TYPES.register(
                    "pig_spawner",
                    () -> BlockEntityType.Builder.of(
                            PigSpawnerBlockEntity::new,
                            ModBlocks.PIG_SPAWNER.get()
                    ).build(null)
            );

    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
                Capabilities.EnergyStorage.BLOCK,
                GENERATOR.get(),
                (entity, side) -> entity.energy
        );
        event.registerBlockEntity(
                Capabilities.EnergyStorage.BLOCK,
                PIG_SPAWNER.get(),
                (entity, side) -> entity.input
        );
        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                GENERATOR.get(),
                (entity, side) -> entity.inventory
        );
        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                PIG_SPAWNER.get(),
                (entity, side) -> entity.inventory
        );
    }

    private ModBlockEntities() {
    }

    public static void register(IEventBus modEventBus) {
        BLOCK_ENTITY_TYPES.register(modEventBus);
    }
}
