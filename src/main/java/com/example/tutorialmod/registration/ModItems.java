package com.example.tutorialmod.registration;

import com.example.tutorialmod.TutorialMod;
import net.minecraft.world.item.BlockItem;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(TutorialMod.MOD_ID);

    public static final DeferredItem<BlockItem> TUTORIAL_BLOCK_ITEM =
            ITEMS.registerSimpleBlockItem(ModBlocks.TUTORIAL_BLOCK);

    private ModItems() {
    }

    public static void register(IEventBus modEventBus) {
        ITEMS.register(modEventBus);
    }
}
