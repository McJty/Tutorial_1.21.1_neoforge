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
        add(ModBlocks.TELEPORTER_BASE.get(), "Teleporter Base");
        add(ModBlocks.RECEIVER_BASE.get(), "Receiver Base");
        add(ModBlocks.TELEPORTER.get(), "Teleporter");
        add(ModBlocks.DESTINATION_CONTROLLER.get(), "Destination Controller");
        add(ModBlocks.RECEIVER.get(), "Receiver");
        add("itemGroup.tutorialmod", "Tutorial Mod");
        add("message.tutorialmod.teleporter_formed", "Teleporter formed!");
        add("message.tutorialmod.invalid_teleporter",
                "Invalid teleporter. Required:\n- 3x3 base\n- Four corner pillars, each two blocks tall\n- 3x3 glass-block roof\nKeep the interior clear.");
        add("message.tutorialmod.no_receiver", "No receiver with color code %s.");
        add("message.tutorialmod.receiver_obstructed", "The receiver with color code %s is obstructed.");
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
