package com.example.tutorialmod.datagen;

import com.example.tutorialmod.TutorialMod;
import com.example.tutorialmod.block.TutorialBlock;
import com.example.tutorialmod.registration.ModBlocks;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public final class ModBlockStateProvider extends BlockStateProvider {
    public ModBlockStateProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, TutorialMod.MOD_ID, existingFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        ResourceLocation baseTexture = modLoc("block/block_texture");

        ModelFile offModel = models().cubeBottomTop(
                "tutorial_block_off",
                baseTexture,
                baseTexture,
                modLoc("block/block_texture_off")
        );
        ModelFile onModel = models().cubeBottomTop(
                "tutorial_block_on",
                baseTexture,
                baseTexture,
                modLoc("block/block_texture_on")
        );

        getVariantBuilder(ModBlocks.TUTORIAL_BLOCK.get())
                .partialState()
                .with(TutorialBlock.ON, false)
                .modelForState()
                .modelFile(offModel)
                .addModel()
                .partialState()
                .with(TutorialBlock.ON, true)
                .modelForState()
                .modelFile(onModel)
                .addModel();

        itemModels().getBuilder("tutorial_block")
                .parent(offModel)
                .override()
                .predicate(modLoc("is_on"), 1.0F)
                .model(onModel)
                .end();
    }
}
