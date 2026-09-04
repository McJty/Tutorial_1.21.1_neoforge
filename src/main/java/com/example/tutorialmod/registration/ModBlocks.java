package com.example.tutorialmod.registration;

import com.example.tutorialmod.TutorialMod;
import com.example.tutorialmod.block.TutorialBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(TutorialMod.MOD_ID);

    public static final DeferredBlock<TutorialBlock> TUTORIAL_BLOCK = BLOCKS.registerBlock(
            "tutorial_block",
            TutorialBlock::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .strength(3.0F)
                    .sound(SoundType.METAL)
    );

    private ModBlocks() {
    }

    public static void register(IEventBus modEventBus) {
        BLOCKS.register(modEventBus);
    }
}
