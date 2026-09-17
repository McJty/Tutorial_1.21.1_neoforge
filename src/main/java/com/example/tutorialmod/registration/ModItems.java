package com.example.tutorialmod.registration;

import com.example.tutorialmod.TutorialMod;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(TutorialMod.MOD_ID);

    public static final DeferredItem<BlockItem> TUTORIAL_BLOCK_ITEM = ITEMS.register(
            "tutorial_block",
            () -> new BlockItem(
                    ModBlocks.TUTORIAL_BLOCK.get(),
                    new Item.Properties()
                            .component(ModDataComponents.IS_ON.get(), false)
            )
    );

    public static final DeferredItem<BlockItem> GENERATOR = ITEMS.registerSimpleBlockItem(ModBlocks.GENERATOR);
    public static final DeferredItem<BlockItem> PIG_SPAWNER = ITEMS.registerSimpleBlockItem(ModBlocks.PIG_SPAWNER);

    private ModItems() {
    }

    public static void register(IEventBus modEventBus) {
        ITEMS.register(modEventBus);
    }
}
