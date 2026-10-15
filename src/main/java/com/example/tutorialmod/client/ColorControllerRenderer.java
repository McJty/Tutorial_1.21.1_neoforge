package com.example.tutorialmod.client;

import com.example.tutorialmod.block.MultiblockBlock;
import com.example.tutorialmod.block.entity.ColorControllerBlockEntity;
import com.example.tutorialmod.block.ColorControllerBlock;
import com.example.tutorialmod.multiblock.MultiblockShapes;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;

/** Tiny wool cubes show the three colors */
public final class ColorControllerRenderer implements BlockEntityRenderer<ColorControllerBlockEntity> {
    private final BlockRenderDispatcher blocks;

    public ColorControllerRenderer(BlockEntityRendererProvider.Context context) {
        blocks = context.getBlockRenderDispatcher();
    }

    @Override
    public void render(ColorControllerBlockEntity entity, float partialTick, PoseStack poses,
                       MultiBufferSource buffers, int light, int overlay) {
        poses.pushPose();
        poses.translate(0.5, 0, 0.5);
        poses.mulPose(Axis.YP.rotationDegrees(-90 * MultiblockShapes.turns(
                entity.getBlockState().getValue(MultiblockBlock.FACING))));
        poses.translate(-0.5, 0, -0.5);
        for (int slot = 0; slot < 3; slot++) {
            poses.pushPose();
            double x = (3 - slot) / 4.0 - 1.0 / 16;
            if (entity.isReceiver()) {
                poses.translate(x, 0.5, 7.0 / 16);
            } else {
                poses.translate(x, 3.0 / 16, -1.0 / 16);
            }
            poses.scale(0.125F, 0.125F, 0.125F);
            blocks.renderSingleBlock(ColorControllerBlock.woolBlock(entity.getColor(slot)).defaultBlockState(),
                    poses, buffers, light, overlay);
            poses.popPose();
        }
        poses.popPose();
    }
}
