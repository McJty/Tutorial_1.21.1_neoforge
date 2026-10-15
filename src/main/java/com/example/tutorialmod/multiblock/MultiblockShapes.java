package com.example.tutorialmod.multiblock;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/** Models and collision use the same dimensions (in sixteenths of a block) */
public final class MultiblockShapes {
    public static final VoxelShape PLATFORM = Block.box(0, 0, 0, 16, 8, 16);
    private static final VoxelShape[] CORNERS = new VoxelShape[4];
    private static final VoxelShape[] PILLARS = new VoxelShape[4];
    private static final VoxelShape[] PILLAR_BASES = new VoxelShape[4];

    static {
        for (int turn = 0; turn < 4; turn++) {
            VoxelShape corner = Shapes.empty();
            // Four small steps approximate the diagonal edges of an octagon
            for (int strip = 0; strip < 4; strip++) {
                corner = Shapes.or(corner, rotatedBox(12 - strip * 4, 0, strip * 4,
                        16, 8, strip * 4 + 4, turn));
            }
            CORNERS[turn] = corner;
            PILLARS[turn] = rotatedBox(6, 0, 6, 10, 16, 10, turn);
            // Bridge the half-block gap between the platform and the first pillar block
            PILLAR_BASES[turn] = Shapes.or(corner, rotatedBox(6, 8, 6, 10, 16, 10, turn));
        }
    }

    public static int turns(Direction facing) {
        return switch (facing) {
            case EAST -> 1;
            case SOUTH -> 2;
            case WEST -> 3;
            default -> 0;
        };
    }

    public static VoxelShape shape(MultiblockPart part, Direction facing, boolean supportsPillar) {
        return switch (part) {
            case PLATFORM -> PLATFORM;
            case CORNER -> supportsPillar ? PILLAR_BASES[turns(facing)] : CORNERS[turns(facing)];
            case PILLAR, UPPER_PILLAR -> PILLARS[turns(facing)];
        };
    }

    private static VoxelShape rotatedBox(double x1, double y1, double z1,
                                         double x2, double y2, double z2, int turns) {
        for (int i = 0; i < turns; i++) {
            double nextX1 = 16 - z2;
            double nextX2 = 16 - z1;
            z1 = x1;
            z2 = x2;
            x1 = nextX1;
            x2 = nextX2;
        }
        return Block.box(x1, y1, z1, x2, y2, z2);
    }

    private MultiblockShapes() {
    }
}
