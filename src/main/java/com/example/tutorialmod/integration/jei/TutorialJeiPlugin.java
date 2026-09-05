package com.example.tutorialmod.integration.jei;

import com.example.tutorialmod.TutorialMod;
import com.example.tutorialmod.registration.ModDataComponents;
import com.example.tutorialmod.registration.ModItems;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.ingredients.subtypes.ISubtypeInterpreter;
import mezz.jei.api.ingredients.subtypes.UidContext;
import mezz.jei.api.registration.IExtraIngredientRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.ISubtypeRegistration;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.List;

@JeiPlugin
public final class TutorialJeiPlugin implements IModPlugin {
    private static final ResourceLocation PLUGIN_ID =
            ResourceLocation.fromNamespaceAndPath(TutorialMod.MOD_ID, "jei_plugin");

    @Override
    public ResourceLocation getPluginUid() {
        return PLUGIN_ID;
    }

    @Override
    public void registerItemSubtypes(ISubtypeRegistration registration) {
        registration.registerSubtypeInterpreter(
                ModItems.TUTORIAL_BLOCK_ITEM.get(),
                new TutorialBlockSubtypeInterpreter()
        );
    }

    @Override
    public void registerExtraIngredients(IExtraIngredientRegistration registration) {
        registration.addExtraItemStacks(List.of(createOnStack()));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        registration.addItemStackInfo(
                List.of(ModItems.TUTORIAL_BLOCK_ITEM.get().getDefaultInstance(), createOnStack()),
                Component.translatable("jei.tutorialmod.tutorial_block.description")
        );
    }

    private static ItemStack createOnStack() {
        ItemStack stack = ModItems.TUTORIAL_BLOCK_ITEM.get().getDefaultInstance();
        stack.set(ModDataComponents.IS_ON.get(), true);
        return stack;
    }

    private static final class TutorialBlockSubtypeInterpreter
            implements ISubtypeInterpreter<ItemStack> {
        @Override
        public Object getSubtypeData(ItemStack ingredient, UidContext context) {
            return ingredient.getOrDefault(ModDataComponents.IS_ON.get(), false);
        }

        @Override
        @SuppressWarnings("deprecation") // Required by JEI for compatibility with old saved UIDs.
        public String getLegacyStringSubtypeInfo(ItemStack ingredient, UidContext context) {
            return Boolean.toString(
                    ingredient.getOrDefault(ModDataComponents.IS_ON.get(), false)
            );
        }
    }
}
