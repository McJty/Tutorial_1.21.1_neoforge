package com.example.tutorialmod.registration;

import com.example.tutorialmod.TutorialMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModCreativeTabs {
    private static final DeferredRegister<CreativeModeTab> CREATIVE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, TutorialMod.MOD_ID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> TUTORIAL_MOD =
            CREATIVE_TABS.register("tutorialmod", () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.tutorialmod"))
                    .icon(() -> new ItemStack(ModItems.TELEPORTER_BASE.get()))
                    .displayItems((parameters, output) -> {
                        output.accept(ModItems.TUTORIAL_BLOCK_ITEM.get());
                        output.accept(ModItems.GENERATOR.get());
                        output.accept(ModItems.PIG_SPAWNER.get());
                        output.accept(ModItems.TELEPORTER_BASE.get());
                        output.accept(ModItems.RECEIVER_BASE.get());
                    })
                    .build());

    public static void register(IEventBus modEventBus) {
        CREATIVE_TABS.register(modEventBus);
    }

    private ModCreativeTabs() {
    }
}
