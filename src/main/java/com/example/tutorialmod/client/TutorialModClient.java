package com.example.tutorialmod.client;

import com.example.tutorialmod.TutorialMod;
import com.example.tutorialmod.registration.ModBlockEntities;
import com.example.tutorialmod.registration.ModDataComponents;
import com.example.tutorialmod.registration.ModItems;
import com.example.tutorialmod.registration.ModMenus;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

@EventBusSubscriber(modid = TutorialMod.MOD_ID, value = Dist.CLIENT)
public final class TutorialModClient {
    private static final ResourceLocation IS_ON_PROPERTY =
            ResourceLocation.fromNamespaceAndPath(TutorialMod.MOD_ID, "is_on");

    private TutorialModClient() {
    }

    @SubscribeEvent
    private static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(ModMenus.MACHINE.get(), MachineScreen::new);
    }

    @SubscribeEvent
    private static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(ModBlockEntities.PIG_SPAWNER.get(), PigSpawnerRenderer::new);
    }

    @SubscribeEvent
    private static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> ItemProperties.register(
                ModItems.TUTORIAL_BLOCK_ITEM.get(),
                IS_ON_PROPERTY,
                (stack, level, entity, seed) ->
                        stack.getOrDefault(ModDataComponents.IS_ON.get(), false) ? 1.0F : 0.0F
        ));
    }
}
