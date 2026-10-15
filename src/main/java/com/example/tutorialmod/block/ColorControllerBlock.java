package com.example.tutorialmod.block;

import com.example.tutorialmod.block.entity.ColorControllerBlockEntity;
import com.example.tutorialmod.multiblock.MultiblockShapes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public final class ColorControllerBlock extends MultiblockBlock implements EntityBlock {
    private final boolean receiver;

    public ColorControllerBlock(Properties properties, boolean receiver) {
        super(properties);
        this.receiver = receiver;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ColorControllerBlockEntity(pos, state);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                               Player player, InteractionHand hand, BlockHitResult hit) {
        var color = colorFromItem(stack);
        int slot = hitSlot(hit, state.getValue(FACING), receiver);
        if (color == null || slot < 0) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof ColorControllerBlockEntity controller) {
            controller.setColor(slot, color);
        }
        return ItemInteractionResult.sidedSuccess(level.isClientSide);
    }

    private static DyeColor colorFromItem(ItemStack stack) {
        if (stack.getItem() instanceof DyeItem dye) {
            return dye.getDyeColor();
        }
        if (stack.is(ItemTags.WOOL) && stack.getItem() instanceof BlockItem wool) {
            ResourceLocation id = BuiltInRegistries.BLOCK.getKey(wool.getBlock());
            String path = id.getPath();
            // Vanilla wool names follow the pattern <color>_wool.
            if (id.getNamespace().equals("minecraft") && path.endsWith("_wool")) {
                String colorName = path.substring(0, path.length() - "_wool".length());
                return DyeColor.byName(colorName, null);
            }
        }
        return null;
    }

    public static Block woolBlock(DyeColor color) {
        return BuiltInRegistries.BLOCK.get(
                ResourceLocation.withDefaultNamespace(color.getName() + "_wool"));
    }

    /** Shared with the renderer: slot centers at x=12, 8, 4, left to right when viewed from north. */
    public static int hitSlot(BlockHitResult hit, Direction facing, boolean receiver) {
        Vec3 local = hit.getLocation().subtract(Vec3.atLowerCornerOf(hit.getBlockPos()));
        // Inverse of the model's clockwise rotation around the block center.
        double x = local.x;
        double z = local.z;
        // This loop rotates the click back to the model's default orientation (facing north) so that the slot positions are consistent.
        for (int turn = 0; turn < MultiblockShapes.turns(facing); turn++) {
            double nextX = z;
            z = 1 - x;
            x = nextX;
        }
        if (receiver) {
            // Indicators are on top
            if (hit.getDirection() != Direction.UP || z < 6.0 / 16 || z > 10.0 / 16) {
                return -1;
            }
        } else if (hit.getDirection() != facing || local.y < 2.0 / 16 || local.y > 6.0 / 16) {
            return -1;
        }
        for (int slot = 0; slot < 3; slot++) {
            if (Math.abs(x - (3 - slot) / 4.0) <= 2.0 / 16) {
                return slot;
            }
        }
        return -1;
    }
}
