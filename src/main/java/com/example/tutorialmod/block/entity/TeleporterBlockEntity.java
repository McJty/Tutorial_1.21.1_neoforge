package com.example.tutorialmod.block.entity;

import com.example.tutorialmod.block.MultiblockBlock;
import com.example.tutorialmod.multiblock.ReceiverSavedData;
import com.example.tutorialmod.registration.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.portal.DimensionTransition;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;

public final class TeleporterBlockEntity extends BlockEntity {
    private final Map<UUID, Long> retryAt = new HashMap<>();

    public TeleporterBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.TELEPORTER.get(), pos, state);
    }

    public void serverTick() {
        if (!(level instanceof ServerLevel server) || !getBlockState().getValue(MultiblockBlock.FORMED)) {
            return;
        }
        BlockPos controllerPos = worldPosition.relative(getBlockState().getValue(MultiblockBlock.FACING));
        if (!(server.getBlockEntity(controllerPos) instanceof ColorControllerBlockEntity controller)) {
            return;
        }
        AABB surface = new AABB(worldPosition.getX(), worldPosition.getY() + 0.5, worldPosition.getZ(),
                worldPosition.getX() + 1, worldPosition.getY() + 0.7, worldPosition.getZ() + 1);
        var present = new HashSet<UUID>();
        for (Entity entity : server.getEntitiesOfClass(Entity.class, surface,
                entity -> entity.isAlive() && !entity.isPassenger()
                        && entity.getY() >= worldPosition.getY() + 0.49
                        && entity.getY() <= worldPosition.getY() + 0.7)) {
            present.add(entity.getUUID());
            if (server.getGameTime() >= retryAt.getOrDefault(entity.getUUID(), 0L)) {
                retryAt.put(entity.getUUID(), server.getGameTime() + 20);
                teleport(server, entity, controller);
            }
        }
        retryAt.keySet().retainAll(present);
    }

    private void teleport(ServerLevel source, Entity entity, ColorControllerBlockEntity controller) {
        var destination = ReceiverSavedData.get(source).randomReceiver(source,
                controller.getColor1(), controller.getColor2(), controller.getColor3());
        if (destination == null) {
            message(entity, "message.tutorialmod.no_receiver", controller);
            return;
        }
        ServerLevel target = source.getServer().getLevel(destination.dimension());
        Vec3 arrival = Vec3.atLowerCornerOf(destination.pos()).add(0.5, 0.5, 0.5);
        AABB arrivalBox = entity.getBoundingBox().move(arrival.subtract(entity.position()));
        if (!target.getWorldBorder().isWithinBounds(arrivalBox) || !target.noCollision(entity, arrivalBox)
                || target.containsAnyLiquid(arrivalBox)) {
            message(entity, "message.tutorialmod.receiver_obstructed", controller);
            return;
        }
        Entity moved = entity.changeDimension(new DimensionTransition(target, arrival, Vec3.ZERO,
                entity.getYRot(), entity.getXRot(), DimensionTransition.PLACE_PORTAL_TICKET));
        if (moved != null) {
            moved.fallDistance = 0;
            target.playSound(null, destination.pos(), SoundEvents.PORTAL_TRAVEL, SoundSource.BLOCKS, 0.25F, 1.0F);
        }
    }

    private static void message(Entity entity, String key, ColorControllerBlockEntity controller) {
        if (entity instanceof Player player) {
            String colors = controller.getColor1().getName() + " / " + controller.getColor2().getName()
                    + " / " + controller.getColor3().getName();
            player.displayClientMessage(Component.translatable(key, colors), true);
        }
    }
}
