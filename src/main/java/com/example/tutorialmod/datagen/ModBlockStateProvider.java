package com.example.tutorialmod.datagen;

import com.example.tutorialmod.TutorialMod;
import com.example.tutorialmod.block.GeneratorBlock;
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
        registerTutorialBlock();
        registerGenerator();
        registerPigSpawner();
    }

    private void registerGenerator() {
        ResourceLocation baseTexture = modLoc("block/block_texture");
        ModelFile generatorOff = models().orientable(
                "generator",
                baseTexture,
                modLoc("block/generator"),
                baseTexture
        );
        ModelFile generatorOn = models().orientable(
                "generator_on",
                baseTexture,
                modLoc("block/generator_on"),
                baseTexture
        );
        horizontalBlock(
                ModBlocks.GENERATOR.get(),
                state -> state.getValue(GeneratorBlock.ON) ? generatorOn : generatorOff
        );
        simpleBlockItem(ModBlocks.GENERATOR.get(), generatorOff);
    }

    private void registerPigSpawner() {
        var spawner = models().getBuilder("pig_spawner")
                .parent(models().getExistingFile(mcLoc("block/block")))
                .texture("all", modLoc("block/spawner"))
                .texture("particle", modLoc("block/spawner"));
        spawner.element()
                .from(0, 0, 0)
                .to(16, 8, 16)
                .allFaces((side, face) -> face.texture("#all"))
                .end();

        for (int x : new int[] {0, 14}) {
            for (int z : new int[] {0, 14}) {
                spawner.element()
                        .from(x, 8, z)
                        .to(x + 2, 16, z + 2)
                        .allFaces((side, face) -> face.texture("#all"))
                        .end();
            }
        }

        simpleBlockWithItem(ModBlocks.PIG_SPAWNER.get(), spawner);
    }

    private void registerTutorialBlock() {
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
