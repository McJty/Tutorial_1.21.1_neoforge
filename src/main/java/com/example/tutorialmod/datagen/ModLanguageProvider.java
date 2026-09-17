package com.example.tutorialmod.datagen;

import com.example.tutorialmod.TutorialMod;
import com.example.tutorialmod.registration.ModBlocks;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.LanguageProvider;

public final class ModLanguageProvider extends LanguageProvider {
    public ModLanguageProvider(PackOutput output) {
        super(output, TutorialMod.MOD_ID, "en_us");
    }

    @Override
    protected void addTranslations() {
        add(ModBlocks.TUTORIAL_BLOCK.get(), "Tutorial Block");
        add(ModBlocks.GENERATOR.get(), "Generator");
        add(ModBlocks.PIG_SPAWNER.get(), "Pig Spawner");
        add("gui.tutorialmod.fuel", "Fuel");
        add("gui.tutorialmod.food", "Food");
        add("gui.tutorialmod.energy", "%s / %s FE");
        add("jei.tutorialmod.tutorial_block.description",
                "Right-click with an empty hand to toggle this block on or off. Its state is preserved when broken and placed again.");
        add("top.tutorialmod.state", "State: %s");
        add("state.tutorialmod.on", "On");
        add("state.tutorialmod.off", "Off");
    }
}
