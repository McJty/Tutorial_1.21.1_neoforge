package com.example.tutorialmod.client;

import com.example.tutorialmod.block.entity.PigSpawnerBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.phys.AABB;

public final class PigSpawnerRenderer implements BlockEntityRenderer<PigSpawnerBlockEntity> {
    private final EntityRenderDispatcher entities;

    public PigSpawnerRenderer(BlockEntityRendererProvider.Context context) {
        entities = context.getEntityRenderer();
    }

    @Override
    public void render(PigSpawnerBlockEntity spawner, float partialTick, PoseStack pose,
                       MultiBufferSource buffers, int light, int overlay) {
        if (!spawner.shouldShowPreview()) {
            return;
        }

        var pig = spawner.getPreviewPig();
        if (pig == null) {
            return;
        }

        float time = spawner.getLevel().getGameTime() + partialTick;
        pose.pushPose();
        pose.translate(0.5, 0.75 + Math.sin(time * 0.12) * 0.06, 0.5);
        pose.mulPose(Axis.YP.rotationDegrees(time * 4.0F));
        pose.scale(0.8F, 0.8F, 0.8F);
        entities.getRenderer(pig).render(pig, 0.0F, partialTick, pose, buffers, light);
        pose.popPose();
    }

    @Override
    public AABB getRenderBoundingBox(PigSpawnerBlockEntity spawner) {
        return new AABB(spawner.getBlockPos()).expandTowards(0, 1.5, 0);
    }
}
