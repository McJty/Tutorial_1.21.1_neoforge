package com.example.tutorialmod.datagen;

import com.example.tutorialmod.TutorialMod;
import com.example.tutorialmod.block.MultiblockBlock;
import com.example.tutorialmod.multiblock.MultiblockShapes;
import com.example.tutorialmod.registration.ModBlocks;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.client.model.generators.BlockModelBuilder;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.client.model.generators.ConfiguredModel;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

/** Each construction block draws its own piece; no giant model or special model loader is needed */
public final class ModMultiblockModelProvider extends BlockStateProvider {
    public ModMultiblockModelProvider(PackOutput output, ExistingFileHelper existingFiles) {
        super(output, TutorialMod.MOD_ID, existingFiles);
    }

    @Override
    public String getName() {
        return "Multiblock block states: " + TutorialMod.MOD_ID;
    }

    @Override
    protected void registerStatesAndModels() {
        ResourceLocation metal = mcLoc("block/iron_block");
        var platform = piece("multiblock_platform", metal);
        cuboid(platform, 0, 0, 0, 16, 8, 16, "all");
        var corner = piece("multiblock_corner", metal);
        var pillarBase = piece("multiblock_pillar_base", metal);
        for (int strip = 0; strip < 4; strip++) {
            cuboid(corner, 12 - strip * 4, 0, strip * 4, 16, 8, strip * 4 + 4, "all");
            cuboid(pillarBase, 12 - strip * 4, 0, strip * 4, 16, 8, strip * 4 + 4, "all");
        }
        cuboid(pillarBase, 6, 8, 6, 10, 16, 10, "all");
        var pillar = piece("multiblock_pillar", metal);
        cuboid(pillar, 6, 0, 6, 10, 16, 10, "all");
        var upperPillar = piece("multiblock_upper_pillar", metal);
        cuboid(upperPillar, 6, 0, 6, 10, 16, 10, "all");
        // Each upper pillar draws one quadrant of the roof. Four quadrants fill 3x3.
        // The roof is visual: the nine construction glass blocks have been consumed.
        roofQuarter(upperPillar);

        registerBase(ModBlocks.TELEPORTER_BASE.get(), "teleporter_base", mcLoc("block/lapis_block"),
                platform, pillarBase, pillar, upperPillar);
        registerBase(ModBlocks.RECEIVER_BASE.get(), "receiver_base", mcLoc("block/copper_block"),
                platform, corner, pillar, upperPillar);

        var teleporter = piece("teleporter", metal).texture("pad", mcLoc("block/purple_concrete"));
        cuboid(teleporter, 0, 0, 0, 16, 8, 16, "all");
        cuboid(teleporter, 5, 8, 2, 11, 8.1F, 14, "pad");
        cuboid(teleporter, 3, 8, 3, 13, 8.1F, 13, "pad");
        cuboid(teleporter, 2, 8, 5, 14, 8.1F, 11, "pad");
        registerSpecial(ModBlocks.TELEPORTER.get(), teleporter);

        var controller = piece("destination_controller", metal)
                .texture("slot", mcLoc("block/polished_deepslate"))
                .texture("first_slot", mcLoc("block/gold_block"));
        cuboid(controller, 0, 0, 0, 16, 8, 16, "all");
        var receiver = piece("receiver", metal)
                .texture("slot", mcLoc("block/polished_deepslate"))
                .texture("first_slot", mcLoc("block/gold_block"));
        cuboid(receiver, 0, 0, 0, 16, 8, 16, "all");
        for (int slot = 0; slot < 3; slot++) {
            int x = (3 - slot) * 4;
            // The matching gold frames mark the start of each ordered color code.
            String frame = slot == 0 ? "first_slot" : "slot";
            cuboid(controller, x - 2, 2, -0.1F, x + 2, 6, 0, frame);
            cuboid(receiver, x - 2, 8, 6, x + 2, 8.1F, 10, frame);
        }
        registerSpecial(ModBlocks.DESTINATION_CONTROLLER.get(), controller);
        registerSpecial(ModBlocks.RECEIVER.get(), receiver);
    }

    private BlockModelBuilder piece(String name, ResourceLocation texture) {
        return models().getBuilder(name).parent(models().getExistingFile(mcLoc("block/block")))
                .texture("all", texture).texture("particle", texture);
    }

    private void registerBase(Block block, String name, ResourceLocation top, ModelFile platform,
                              ModelFile corner, ModelFile pillar, ModelFile upperPillar) {
        ResourceLocation metal = mcLoc("block/iron_block");
        ModelFile construction = models().cubeBottomTop(name, metal, metal, top);
        getVariantBuilder(block).forAllStates(state -> {
            ModelFile model = construction;
            if (state.getValue(MultiblockBlock.FORMED)) {
                model = switch (state.getValue(MultiblockBlock.PART)) {
                    case PLATFORM -> platform;
                    case CORNER -> corner;
                    case PILLAR -> pillar;
                    case UPPER_PILLAR -> upperPillar;
                };
            }
            return ConfiguredModel.builder().modelFile(model)
                    .rotationY(90 * MultiblockShapes.turns(state.getValue(MultiblockBlock.FACING))).build();
        });
        simpleBlockItem(block, construction);
    }

    private void registerSpecial(Block block, ModelFile model) {
        getVariantBuilder(block).forAllStates(state -> ConfiguredModel.builder().modelFile(model)
                .rotationY(90 * MultiblockShapes.turns(state.getValue(MultiblockBlock.FACING))).build());
    }

    private void roofQuarter(BlockModelBuilder model) {
        // Tile the 24x24-pixel quarter without sampling outside a 16x16 texture.
        for (int x = 0; x < 24; x += 16) {
            for (int z = 0; z < 24; z += 16) {
                int width = Math.min(16, 24 - x);
                int depth = Math.min(16, 24 - z);
                model.element().from(x, 16, z).to(x + width, 20, z + depth)
                        .allFaces((side, face) -> {
                            face.texture("#all");
                            // UVs are local to the texture, independent of the roof's height and overhang.
                            switch (side) {
                                case UP -> face.uvs(0, 0, width, depth);
                                case DOWN -> face.uvs(0, 16 - depth, width, 16);
                                case NORTH -> face.uvs(16 - width, 12, 16, 16);
                                case SOUTH -> face.uvs(0, 12, width, 16);
                                case WEST -> face.uvs(0, 12, depth, 16);
                                case EAST -> face.uvs(16 - depth, 12, 16, 16);
                            }
                        }).end();
            }
        }
    }

    private void cuboid(BlockModelBuilder model, float x1, float y1, float z1,
                        float x2, float y2, float z2, String texture) {
        model.element().from(x1, y1, z1).to(x2, y2, z2)
                .allFaces((side, face) -> face.texture("#" + texture)).end();
    }
}
