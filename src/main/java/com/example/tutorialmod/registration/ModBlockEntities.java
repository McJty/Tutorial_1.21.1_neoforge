package com.example.tutorialmod.registration;

import com.example.tutorialmod.TutorialMod;
import com.example.tutorialmod.block.entity.TutorialBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
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

    private ModBlockEntities() {
    }

    public static void register(IEventBus modEventBus) {
        BLOCK_ENTITY_TYPES.register(modEventBus);
    }
}
