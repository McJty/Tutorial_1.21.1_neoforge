package com.example.tutorialmod.multiblock;

import com.example.tutorialmod.block.MultiblockBlock;
import com.example.tutorialmod.registration.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;
import java.util.HashSet;
import java.util.Set;

public final class Multiblocks {
    // onRemove also runs for the blocks we restore during dismantling
    private static final Set<GlobalPos> UNFORMING = new HashSet<>();

    public static void formTeleporter(ServerLevel level, BlockPos clicked, Player player) {
        // A clicked block might be on the platform or either level of a corner pillar
        for (int down = 0; down <= 2; down++) {
            for (BlockPos center : platform(clicked.below(down))) {
                if (teleporterPatternMatches(level, center)) {
                    Direction front = closestControllerFace(center, player);
                    form(level, center, false, front);
                    player.displayClientMessage(Component.translatable("message.tutorialmod.teleporter_formed"), true);
                    return;
                }
            }
        }
        player.displayClientMessage(Component.translatable("message.tutorialmod.invalid_teleporter"), false);
    }

    private static Direction closestControllerFace(BlockPos center, Player player) {
        Direction closest = Direction.NORTH;
        double bestDistance = Double.MAX_VALUE;
        for (Direction side : new Direction[] {Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST}) {
            double distance = player.distanceToSqr(center.relative(side).getCenter());
            if (distance < bestDistance) {
                closest = side;
                bestDistance = distance;
            }
        }
        return closest;
    }

    public static void formReceiver(ServerLevel level, BlockPos placed) {
        for (BlockPos center : platform(placed)) {
            if (platform(center).stream().allMatch(pos -> unformed(level, pos, ModBlocks.RECEIVER_BASE.get()))) {
                form(level, center, true, Direction.NORTH);
                return;
            }
        }
    }

    public static boolean teleporterPatternMatches(ServerLevel level, BlockPos center) {
        if (!teleporterMembers(center).stream().allMatch(pos -> unformed(level, pos, ModBlocks.TELEPORTER_BASE.get()))) {
            return false;
        }
        for (BlockPos pos : platform(center.above(3))) {
            if (!level.getBlockState(pos).is(Blocks.GLASS)) {
                return false;
            }
        }
        // Keep the interior clear so the player can walk onto the teleporter.
        for (int y = 1; y <= 2; y++) {
            for (BlockPos pos : platform(center.above(y))) {
                if (!isCorner(center, pos) && !level.getBlockState(pos).isAir()) {
                    return false;
                }
            }
        }
        return true;
    }

    private static boolean unformed(ServerLevel level, BlockPos pos, Block block) {
        BlockState state = level.getBlockState(pos);
        return state.is(block) && !state.getValue(MultiblockBlock.FORMED);
    }

    private static void form(ServerLevel level, BlockPos center, boolean receiver, Direction front) {
        Block base = receiver ? ModBlocks.RECEIVER_BASE.get() : ModBlocks.TELEPORTER_BASE.get();
        List<BlockPos> members = receiver ? platform(center) : teleporterMembers(center);
        for (BlockPos pos : members) {
            MultiblockPart part = MultiblockPart.PLATFORM;
            Direction facing = front;
            if (isCorner(center, pos)) {
                part = MultiblockPart.CORNER;
                if (pos.getY() == center.getY() + 1) {
                    part = MultiblockPart.PILLAR;
                } else if (pos.getY() == center.getY() + 2) {
                    part = MultiblockPart.UPPER_PILLAR;
                }
                facing = cornerFacing(center, pos);
            }
            level.setBlock(pos, base.defaultBlockState().setValue(MultiblockBlock.FORMED, true)
                    .setValue(MultiblockBlock.PART, part).setValue(MultiblockBlock.FACING, facing), Block.UPDATE_CLIENTS);
        }
        if (!receiver) {
            for (BlockPos roof : platform(center.above(3))) {
                level.setBlock(roof, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
            }
            level.setBlock(center.relative(front), formedState(ModBlocks.DESTINATION_CONTROLLER.get(), front), Block.UPDATE_ALL);
        }
        level.setBlock(center, formedState(receiver ? ModBlocks.RECEIVER.get() : ModBlocks.TELEPORTER.get(), front),
                Block.UPDATE_ALL);
        if (receiver) {
            ReceiverSavedData.get(level).setReceiver(GlobalPos.of(level.dimension(), center),
                    DyeColor.WHITE, DyeColor.WHITE, DyeColor.WHITE);
        }
    }

    private static BlockState formedState(Block block, Direction facing) {
        return block.defaultBlockState().setValue(MultiblockBlock.FORMED, true).setValue(MultiblockBlock.FACING, facing);
    }

    public static void memberRemoved(ServerLevel level, BlockPos broken, BlockState oldState) {
        boolean receiver = oldState.is(ModBlocks.RECEIVER_BASE.get()) || oldState.is(ModBlocks.RECEIVER.get());
        Block centerBlock = receiver ? ModBlocks.RECEIVER.get() : ModBlocks.TELEPORTER.get();
        // The center itself has already been replaced when onRemove runs
        if (oldState.is(centerBlock)) {
            unform(level, broken, broken, receiver);
            return;
        }
        for (int down = 0; down <= (receiver ? 0 : 2); down++) {
            for (BlockPos center : platform(broken.below(down))) {
                if (level.getBlockState(center).is(centerBlock)
                        && (receiver ? platform(center) : teleporterMembers(center)).contains(broken)) {
                    unform(level, center, broken, receiver);
                    return;
                }
            }
        }
    }

    private static void unform(ServerLevel level, BlockPos center, BlockPos broken, boolean receiver) {
        GlobalPos key = GlobalPos.of(level.dimension(), center);
        if (!UNFORMING.add(key)) {
            return;
        }
        try {
            restoreMembers(level, center, broken, receiver);
        } finally {
            UNFORMING.remove(key);
        }
    }

    private static void restoreMembers(ServerLevel level, BlockPos center, BlockPos broken, boolean receiver) {
        if (receiver) {
            ReceiverSavedData.get(level).removeReceiver(GlobalPos.of(level.dimension(), center));
        }
        Block base = receiver ? ModBlocks.RECEIVER_BASE.get() : ModBlocks.TELEPORTER_BASE.get();
        // Reset the center first so later member callbacks no longer find it.
        if (!center.equals(broken)) {
            level.setBlock(center, base.defaultBlockState(), Block.UPDATE_ALL);
        }
        for (BlockPos pos : receiver ? platform(center) : teleporterMembers(center)) {
            if (pos.equals(broken) || pos.equals(center)) {
                continue;
            }
            BlockState state = level.getBlockState(pos);
            if (state.getBlock() instanceof MultiblockBlock && state.getValue(MultiblockBlock.FORMED)) {
                level.setBlock(pos, base.defaultBlockState(), Block.UPDATE_ALL);
            }
        }
        if (!receiver) {
            for (BlockPos roof : platform(center.above(3))) {
                if (level.isEmptyBlock(roof)) {
                    level.setBlock(roof, Blocks.GLASS.defaultBlockState(), Block.UPDATE_ALL);
                } else {
                    // Preserve anything placed here while the teleporter was formed.
                    Block.popResource(level, roof, new ItemStack(Blocks.GLASS));
                }
            }
        }
    }

    public static List<BlockPos> platform(BlockPos center) {
        List<BlockPos> positions = new ArrayList<>(9);
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                positions.add(center.offset(x, 0, z));
            }
        }
        return positions;
    }

    public static List<BlockPos> teleporterMembers(BlockPos center) {
        List<BlockPos> positions = platform(center);
        for (int x = -1; x <= 1; x += 2) {
            for (int z = -1; z <= 1; z += 2) {
                positions.add(center.offset(x, 1, z));
                positions.add(center.offset(x, 2, z));
            }
        }
        return positions;
    }

    private static boolean isCorner(BlockPos center, BlockPos pos) {
        return Math.abs(pos.getX() - center.getX()) == 1 && Math.abs(pos.getZ() - center.getZ()) == 1;
    }

    private static Direction cornerFacing(BlockPos center, BlockPos pos) {
        if (pos.getZ() < center.getZ()) {
            return pos.getX() < center.getX() ? Direction.NORTH : Direction.EAST;
        }
        return pos.getX() > center.getX() ? Direction.SOUTH : Direction.WEST;
    }

    private Multiblocks() {
    }
}
