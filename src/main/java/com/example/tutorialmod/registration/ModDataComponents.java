package com.example.tutorialmod.registration;

import com.example.tutorialmod.TutorialMod;
import com.mojang.serialization.Codec;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModDataComponents {
    public static final DeferredRegister.DataComponents DATA_COMPONENTS =
            DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, TutorialMod.MOD_ID);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Boolean>> IS_ON =
            DATA_COMPONENTS.registerComponentType(
                    "is_on",
                    builder -> builder
                            .persistent(Codec.BOOL)
                            .networkSynchronized(ByteBufCodecs.BOOL)
            );

    private ModDataComponents() {
    }

    public static void register(IEventBus modEventBus) {
        DATA_COMPONENTS.register(modEventBus);
    }
}
