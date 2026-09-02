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
    }
}
