package com.example.tutorialmod;

import com.example.tutorialmod.block.MultiblockBlock;
import com.example.tutorialmod.block.entity.ColorControllerBlockEntity;
import com.example.tutorialmod.block.entity.TeleporterBlockEntity;
import com.example.tutorialmod.block.ColorControllerBlock;
import com.example.tutorialmod.multiblock.Multiblocks;
import com.example.tutorialmod.multiblock.ReceiverSavedData;
import com.example.tutorialmod.registration.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(TutorialMod.MOD_ID)
@PrefixGameTestTemplate(false)
public final class MultiblockGameTests {
    private static final BlockPos TELEPORTER = new BlockPos(3, 1, 4);
    private static final BlockPos RECEIVER = new BlockPos(9, 1, 4);

    @GameTest(template = "multiblocks", timeoutTicks = 20)
    public static void manualFormationChecksPatternAndChoosesClosestController(GameTestHelper helper) {
        buildTeleporter(helper);
        helper.setBlock(TELEPORTER.offset(-1, 3, -1), Blocks.AIR);
        Player player = playerNorthOf(helper, TELEPORTER);
        helper.useBlock(TELEPORTER, player);
        helper.assertBlockPresent(ModBlocks.TELEPORTER_BASE.get(), TELEPORTER);
        helper.assertTrue(!helper.getBlockState(TELEPORTER).getValue(MultiblockBlock.FORMED),
                "An invalid structure remains untouched");
        helper.setBlock(TELEPORTER.offset(-1, 3, -1), Blocks.GLASS);
        helper.useBlock(TELEPORTER.offset(-1, 2, -1), player);
        assertFormedTeleporter(helper);
        helper.assertBlockPresent(ModBlocks.DESTINATION_CONTROLLER.get(), TELEPORTER.north());
        for (BlockPos roof : Multiblocks.platform(TELEPORTER.above(3))) {
            helper.assertBlockPresent(Blocks.AIR, roof);
        }
        helper.succeed();
    }

    @GameTest(template = "multiblocks", timeoutTicks = 20)
    public static void wrongRoofOrBlockedInteriorCannotForm(GameTestHelper helper) {
        buildTeleporter(helper);
        BlockPos roof = TELEPORTER.above(3);
        var server = helper.getLevel();
        var center = helper.absolutePos(TELEPORTER);
        helper.setBlock(roof, Blocks.GLASS_PANE);
        helper.assertTrue(!Multiblocks.teleporterPatternMatches(server, center), "Glass panes fail validation");
        helper.setBlock(roof, Blocks.STONE);
        helper.assertTrue(!Multiblocks.teleporterPatternMatches(server, center), "Non-glass roof fails validation");
        helper.setBlock(roof, Blocks.GLASS);
        helper.setBlock(TELEPORTER.above(), Blocks.STONE);
        helper.assertTrue(!Multiblocks.teleporterPatternMatches(server, center), "Blocked interior fails validation");
        helper.succeed();
    }

    @GameTest(template = "multiblocks", timeoutTicks = 20)
    public static void heldBlocksPlaceWithoutSneakingAndOnlyEmptyHandsForm(GameTestHelper helper) {
        buildTeleporter(helper);
        Player player = playerNorthOf(helper, TELEPORTER);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.STONE, 2));
        BlockPos absolute = helper.absolutePos(TELEPORTER);
        var hit = new BlockHitResult(Vec3.atLowerCornerOf(absolute).add(0.5, 1, 0.5),
                Direction.UP, absolute, false);
        helper.useBlock(TELEPORTER, player, hit);
        helper.assertBlockPresent(Blocks.STONE, TELEPORTER.above());
        helper.assertBlockPresent(ModBlocks.TELEPORTER_BASE.get(), TELEPORTER);
        helper.assertTrue(!player.isShiftKeyDown(), "Normal placement does not require sneaking");
        helper.setBlock(TELEPORTER.above(), Blocks.AIR);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.IRON_INGOT));
        helper.useBlock(TELEPORTER, player, hit);
        helper.assertBlockPresent(ModBlocks.TELEPORTER_BASE.get(), TELEPORTER);
        player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        helper.useBlock(TELEPORTER, player, hit);
        assertFormedTeleporter(helper);
        helper.succeed();
    }

    @GameTest(template = "multiblocks", timeoutTicks = 20)
    public static void pillarSupportsBridgeTheGapWithoutChangingReceiverCorners(GameTestHelper helper) {
        buildTeleporter(helper);
        helper.useBlock(TELEPORTER, playerNorthOf(helper, TELEPORTER));
        for (BlockPos corner : Multiblocks.platform(TELEPORTER)) {
            if (Math.abs(corner.getX() - TELEPORTER.getX()) != 1
                    || Math.abs(corner.getZ() - TELEPORTER.getZ()) != 1) {
                continue;
            }
            var state = helper.getBlockState(corner);
            var pos = helper.absolutePos(corner);
            var shape = state.getCollisionShape(helper.getLevel(), pos);
            var support = Block.box(6, 8, 6, 10, 16, 10);
            helper.assertTrue(!net.minecraft.world.phys.shapes.Shapes.joinIsNotEmpty(support, shape,
                    net.minecraft.world.phys.shapes.BooleanOp.ONLY_FIRST), "Corner supports the entire pillar footprint");
            var receiverCorner = ModBlocks.RECEIVER_BASE.get().defaultBlockState()
                    .setValue(MultiblockBlock.FORMED, true)
                    .setValue(MultiblockBlock.PART, com.example.tutorialmod.multiblock.MultiblockPart.CORNER)
                    .setValue(MultiblockBlock.FACING, state.getValue(MultiblockBlock.FACING));
            helper.assertTrue(receiverCorner.getCollisionShape(helper.getLevel(), pos).max(Direction.Axis.Y) == 0.5,
                    "Receiver corners remain half-height without pillar supports");
        }
        helper.succeed();
    }

    @GameTest(template = "multiblocks", timeoutTicks = 20)
    public static void breakingEveryTeleporterMemberUnformsIt(GameTestHelper helper) {
        for (BlockPos broken : Multiblocks.teleporterMembers(TELEPORTER)) {
            buildTeleporter(helper);
            helper.useBlock(TELEPORTER, playerNorthOf(helper, TELEPORTER));
            assertFormedTeleporter(helper);
            helper.destroyBlock(broken);
            for (BlockPos member : Multiblocks.teleporterMembers(TELEPORTER)) {
                if (member.equals(broken)) {
                    helper.assertBlockPresent(Blocks.AIR, member);
                } else {
                    helper.assertBlockPresent(ModBlocks.TELEPORTER_BASE.get(), member);
                    helper.assertTrue(!helper.getBlockState(member).getValue(MultiblockBlock.FORMED),
                            "Every surviving part is unformed after breaking " + broken);
                }
            }
            for (BlockPos roof : Multiblocks.platform(TELEPORTER.above(3))) {
                helper.assertBlockPresent(Blocks.GLASS, roof);
            }
            helper.setBlock(broken, ModBlocks.TELEPORTER_BASE.get());
            helper.useBlock(TELEPORTER, playerNorthOf(helper, TELEPORTER));
            assertFormedTeleporter(helper);
        }
        helper.succeed();
    }

    @GameTest(template = "multiblocks", timeoutTicks = 20)
    public static void dismantlingPreservesOccupiedRoofAndDropsItsGlass(GameTestHelper helper) {
        buildTeleporter(helper);
        helper.useBlock(TELEPORTER, playerNorthOf(helper, TELEPORTER));
        BlockPos occupied = TELEPORTER.above(3);
        helper.setBlock(occupied, Blocks.STONE);
        helper.destroyBlock(TELEPORTER);
        for (BlockPos roof : Multiblocks.platform(TELEPORTER.above(3))) {
            helper.assertBlockPresent(roof.equals(occupied) ? Blocks.STONE : Blocks.GLASS, roof);
        }
        var drops = helper.getLevel().getEntitiesOfClass(ItemEntity.class,
                new AABB(helper.absolutePos(occupied)).inflate(0.5));
        int glassCount = drops.stream().filter(item -> item.getItem().is(Items.GLASS))
                .mapToInt(item -> item.getItem().getCount()).sum();
        helper.assertTrue(glassCount == 1, "The occupied roof position refunds exactly one glass item");
        helper.succeed();
    }

    @GameTest(template = "multiblocks", timeoutTicks = 20)
    public static void receiverAutomaticallyFormsOnLastPlacement(GameTestHelper helper) {
        var positions = Multiblocks.platform(RECEIVER);
        BlockPos last = positions.removeLast();
        positions.forEach(pos -> helper.setBlock(pos, ModBlocks.RECEIVER_BASE.get()));
        helper.runAfterDelay(2, () -> {
            helper.assertBlockPresent(ModBlocks.RECEIVER_BASE.get(), RECEIVER);
            helper.setBlock(last, ModBlocks.RECEIVER_BASE.get());
        });
        helper.runAfterDelay(4, () -> {
            helper.assertBlockPresent(ModBlocks.RECEIVER.get(), RECEIVER);
            for (BlockPos member : Multiblocks.platform(RECEIVER)) {
                helper.assertTrue(helper.getBlockState(member).getValue(MultiblockBlock.FORMED), "Receiver member formed");
            }
            helper.destroyBlock(RECEIVER.east());
            helper.assertBlockPresent(ModBlocks.RECEIVER_BASE.get(), RECEIVER);
            for (BlockPos roof : Multiblocks.platform(RECEIVER.above(3))) {
                helper.assertBlockPresent(Blocks.AIR, roof);
            }
            GlobalPos pos = GlobalPos.of(helper.getLevel().dimension(), helper.absolutePos(RECEIVER));
            helper.assertTrue(!hasSavedReceiver(helper, pos), "Receiver entry removed");
            helper.succeed();
        });
    }

    @GameTest(template = "multiblocks", timeoutTicks = 20)
    public static void dyeAndWoolClicksSyncAndSaveThreeOrderedColors(GameTestHelper helper) {
        buildTeleporter(helper);
        helper.useBlock(TELEPORTER, playerNorthOf(helper, TELEPORTER));
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        BlockPos controllerPos = TELEPORTER.north();
        for (int slot = 0; slot < 3; slot++) {
            ItemStack stack = new ItemStack(switch (slot) {
                case 0 -> Items.RED_DYE;
                case 1 -> Items.BLUE_WOOL;
                default -> Items.GREEN_DYE;
            }, 2);
            player.setItemInHand(InteractionHand.MAIN_HAND, stack);
            BlockPos absolute = helper.absolutePos(controllerPos);
            Vec3 point = Vec3.atLowerCornerOf(absolute).add((3 - slot) / 4.0, 0.25, 0);
            helper.useBlock(controllerPos, player, new BlockHitResult(point, Direction.NORTH, absolute, false));
            helper.assertTrue(stack.getCount() == 2, "Coloring preserves both dye and wool stacks in survival");
        }
        ColorControllerBlockEntity controller = helper.getBlockEntity(controllerPos);
        helper.assertTrue(controller.getColor1() == DyeColor.RED && controller.getColor2() == DyeColor.BLUE
                && controller.getColor3() == DyeColor.GREEN, "Three independent ordered slots");
        CompoundTag saved = controller.saveWithoutMetadata(helper.getLevel().registryAccess());
        var restored = new ColorControllerBlockEntity(controller.getBlockPos(), controller.getBlockState());
        restored.loadWithComponents(saved, helper.getLevel().registryAccess());
        helper.assertTrue(restored.getColor1() == DyeColor.RED && restored.getColor2() == DyeColor.BLUE
                && restored.getColor3() == DyeColor.GREEN, "Colors survive BE serialization");
        CompoundTag update = controller.getUpdateTag(helper.getLevel().registryAccess());
        helper.assertTrue(update.getByte("color1") == DyeColor.RED.getId()
                && update.getByte("color2") == DyeColor.BLUE.getId()
                && update.getByte("color3") == DyeColor.GREEN.getId(), "The client update contains all three dye IDs");
        for (String key : new String[] {"color1", "color2", "color3"}) {
            helper.assertTrue(saved.contains(key, net.minecraft.nbt.Tag.TAG_BYTE), "Color IDs are saved as bytes");
        }
        helper.succeed();
    }

    @GameTest(template = "multiblocks", timeoutTicks = 20)
    public static void topSlotsAndRotatedFrontSlotsUseMatchingCoordinates(GameTestHelper helper) {
        for (Direction facing : Direction.Plane.HORIZONTAL) {
            // An observer facing the controller sees its clockwise side on their left.
            Direction left = facing.getClockWise();
            for (int slot = 0; slot < 3; slot++) {
                double offset = (1 - slot) / 4.0;
                Vec3 topPoint = new Vec3(0.5 + left.getStepX() * offset, 0.5,
                        0.5 + left.getStepZ() * offset);
                Vec3 frontPoint = topPoint.add(facing.getStepX() * 0.5, -0.25, facing.getStepZ() * 0.5);
                var front = new BlockHitResult(frontPoint, facing, BlockPos.ZERO, false);
                helper.assertTrue(ColorControllerBlock.hitSlot(front, facing, false) == slot,
                        "Front slots read left to right when viewed from " + facing);
                var top = new BlockHitResult(topPoint, Direction.UP, BlockPos.ZERO, false);
                helper.assertTrue(ColorControllerBlock.hitSlot(top, facing, true) == slot,
                        "Top slots match the front order when viewed from " + facing);
            }
        }
        buildReceiver(helper, RECEIVER);
        helper.runAfterDelay(2, () -> {
            var player = helper.makeMockPlayer(GameType.SURVIVAL);
            player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.LIME_WOOL));
            BlockPos absolute = helper.absolutePos(RECEIVER);
            helper.useBlock(RECEIVER, player, new BlockHitResult(Vec3.atLowerCornerOf(absolute).add(0.5, 0.5, 0.5),
                    Direction.UP, absolute, false));
            ColorControllerBlockEntity receiver = helper.getBlockEntity(RECEIVER);
            helper.assertTrue(receiver.getColor2() == DyeColor.LIME, "Receiver top slot is clickable");
            helper.destroyBlock(RECEIVER);
            helper.succeed();
        });
    }

    @GameTest(template = "multiblocks", timeoutTicks = 30)
    public static void savedDirectoryRetainsDuplicatesAndRemovesOldCodes(GameTestHelper helper) {
        BlockPos second = TELEPORTER;
        buildReceiver(helper, RECEIVER);
        buildReceiver(helper, second);
        helper.runAfterDelay(2, () -> {
            var level = helper.getLevel();
            ColorControllerBlockEntity first = helper.getBlockEntity(RECEIVER);
            ColorControllerBlockEntity other = helper.getBlockEntity(second);
            first.setColor(0, DyeColor.RED);
            first.setColor(1, DyeColor.BLUE);
            first.setColor(2, DyeColor.GREEN);
            other.setColor(0, DyeColor.RED);
            other.setColor(1, DyeColor.BLUE);
            other.setColor(2, DyeColor.GREEN);
            var directory = ReceiverSavedData.get(level);
            var restored = ReceiverSavedData.load(directory.save(new CompoundTag(), level.registryAccess()), level.registryAccess());
            var seen = new java.util.HashSet<GlobalPos>();
            for (int i = 0; i < 100; i++) {
                seen.add(restored.randomReceiver(level, DyeColor.RED, DyeColor.BLUE, DyeColor.GREEN));
            }
            helper.assertTrue(seen.size() == 2 && !seen.contains(null), "Both receivers survive SavedData round trip and can be selected");
            helper.assertTrue(restored.randomReceiver(level, DyeColor.GREEN, DyeColor.BLUE, DyeColor.RED) == null,
                    "Receiver matching preserves color order");
            helper.assertTrue(restored.randomReceiver(level, DyeColor.RED, DyeColor.BLUE, DyeColor.YELLOW) == null,
                    "All three receiver colors must match");
            first.setColor(0, DyeColor.BLUE);
            helper.assertTrue(directory.randomReceiver(level, DyeColor.RED, DyeColor.BLUE, DyeColor.GREEN).pos().equals(helper.absolutePos(second)), "Recoloring removes old code");
            helper.destroyBlock(second);
            helper.assertTrue(directory.randomReceiver(level, DyeColor.RED, DyeColor.BLUE, DyeColor.GREEN) == null, "Breaking the center removes its saved location");
            helper.destroyBlock(RECEIVER.north());
            helper.assertTrue(directory.randomReceiver(level, first.getColor1(), first.getColor2(), first.getColor3()) == null, "Breaking a member removes its saved location");
            helper.succeed();
        });
    }

    @GameTest(template = "multiblocks", timeoutTicks = 30)
    public static void entitiesTeleportAndBlockedDestinationsAreRejected(GameTestHelper helper) {
        buildTeleporter(helper);
        helper.useBlock(TELEPORTER, playerNorthOf(helper, TELEPORTER));
        buildReceiver(helper, RECEIVER);
        helper.runAfterDelay(2, () -> {
            ColorControllerBlockEntity controller = helper.getBlockEntity(TELEPORTER.north());
            ColorControllerBlockEntity receiver = helper.getBlockEntity(RECEIVER);
            controller.setColor(0, DyeColor.PINK);
            receiver.setColor(0, DyeColor.PINK);
            var pig = helper.spawnWithNoFreeWill(EntityType.PIG, new Vec3(3.5, 1.5, 4.5));
            TeleporterBlockEntity teleporter = helper.getBlockEntity(TELEPORTER);
            teleporter.serverTick();
            helper.assertTrue(pig.position().distanceTo(helper.absoluteVec(new Vec3(9.5, 1.5, 4.5))) < 0.01,
                    "Entity arrives on the receiver platform");
            pig.discard();
            helper.setBlock(RECEIVER.above(), Blocks.STONE);
            var blockedPig = helper.spawnWithNoFreeWill(EntityType.PIG, new Vec3(3.5, 1.5, 4.5));
            teleporter.serverTick();
            helper.assertTrue(blockedPig.position().distanceTo(helper.absoluteVec(new Vec3(3.5, 1.5, 4.5))) < 0.01,
                    "A blocked destination cannot teleport an entity into stone");
            blockedPig.discard();
            helper.destroyBlock(RECEIVER);
            helper.succeed();
        });
    }

    @GameTest(template = "multiblocks", timeoutTicks = 30)
    public static void playersReceiveInvalidAndMissingDestinationMessages(GameTestHelper helper) {
        var messages = new java.util.ArrayList<net.minecraft.network.chat.Component>();
        Player player = new Player(helper.getLevel(), BlockPos.ZERO, 0,
                new com.mojang.authlib.GameProfile(java.util.UUID.randomUUID(), "message-test")) {
            @Override
            public boolean isSpectator() { return false; }
            @Override
            public boolean isCreative() { return true; }
            @Override
            public void displayClientMessage(net.minecraft.network.chat.Component message, boolean actionBar) {
                messages.add(message);
            }
        };
        helper.setBlock(TELEPORTER, ModBlocks.TELEPORTER_BASE.get());
        helper.useBlock(TELEPORTER, player);
        helper.assertTrue(messages.getLast().getString().contains("Invalid teleporter"), "Invalid pattern message");
        buildTeleporter(helper);
        helper.useBlock(TELEPORTER, playerNorthOf(helper, TELEPORTER));
        ColorControllerBlockEntity controller = helper.getBlockEntity(TELEPORTER.north());
        controller.setColor(0, DyeColor.GREEN);
        controller.setColor(1, DyeColor.CYAN);
        controller.setColor(2, DyeColor.YELLOW);
        player.setPos(helper.absoluteVec(new Vec3(3.5, 1.5, 4.5)));
        helper.getLevel().addFreshEntity(player);
        TeleporterBlockEntity teleporter = helper.getBlockEntity(TELEPORTER);
        teleporter.serverTick();
        helper.assertTrue(messages.getLast().getString().contains("No receiver with color code green / cyan / yellow"),
                "Missing receiver message includes the ordered color code");
        int count = messages.size();
        teleporter.serverTick();
        helper.assertTrue(messages.size() == count, "Messages are throttled while a player stays on the pad");
        player.discard();
        helper.succeed();
    }

    @GameTest(template = "multiblocks", timeoutTicks = 30)
    public static void serverPlayersTeleportOntoReceivers(GameTestHelper helper) {
        buildTeleporter(helper);
        helper.useBlock(TELEPORTER, playerNorthOf(helper, TELEPORTER));
        buildReceiver(helper, RECEIVER);
        helper.runAfterDelay(2, () -> {
            ColorControllerBlockEntity controller = helper.getBlockEntity(TELEPORTER.north());
            ColorControllerBlockEntity receiver = helper.getBlockEntity(RECEIVER);
            controller.setColor(0, DyeColor.ORANGE);
            receiver.setColor(0, DyeColor.ORANGE);
            var player = helper.makeMockServerPlayerInLevel();
            player.setPos(helper.absoluteVec(new Vec3(3.5, 1.5, 4.5)));
            TeleporterBlockEntity teleporter = helper.getBlockEntity(TELEPORTER);
            teleporter.serverTick();
            helper.assertTrue(player.position().distanceTo(helper.absoluteVec(new Vec3(9.5, 1.5, 4.5))) < 0.01,
                    "The ServerPlayer uses the correct teleport path");
            player.connection.disconnect(net.minecraft.network.chat.Component.literal("Test completed"));
            helper.destroyBlock(RECEIVER);
            helper.succeed();
        });
    }

    @GameTest(template = "multiblocks", timeoutTicks = 30)
    public static void mobsCanTravelToReceiversInAnotherDimension(GameTestHelper helper) {
        buildTeleporter(helper);
        helper.useBlock(TELEPORTER, playerNorthOf(helper, TELEPORTER));
        var target = helper.getLevel().getServer().getLevel(net.minecraft.world.level.Level.NETHER);
        BlockPos destination = helper.absolutePos(RECEIVER).atY(120);
        for (BlockPos pos : Multiblocks.platform(destination)) {
            target.setBlock(pos.above(), Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
            target.setBlock(pos.above(2), Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
            target.setBlock(pos, ModBlocks.RECEIVER_BASE.get().defaultBlockState(), Block.UPDATE_ALL);
        }
        Multiblocks.formReceiver(target, destination);
        var receiver = (ColorControllerBlockEntity) target.getBlockEntity(destination);
        ColorControllerBlockEntity controller = helper.getBlockEntity(TELEPORTER.north());
        receiver.setColor(0, DyeColor.BLACK);
        receiver.setColor(1, DyeColor.PURPLE);
        controller.setColor(0, DyeColor.BLACK);
        controller.setColor(1, DyeColor.PURPLE);
        var pig = helper.spawnWithNoFreeWill(EntityType.PIG, new Vec3(3.5, 1.5, 4.5));
        var id = pig.getUUID();
        TeleporterBlockEntity teleporter = helper.getBlockEntity(TELEPORTER);
        var directory = ReceiverSavedData.get(helper.getLevel());
        var found = directory.randomReceiver(helper.getLevel(), controller.getColor1(), controller.getColor2(), controller.getColor3());
        helper.assertTrue(found != null && found.dimension().equals(target.dimension()), "Cross-dimension directory lookup: " + found);
        var arrival = Vec3.atLowerCornerOf(destination).add(0.5, 0.5, 0.5);
        var box = pig.getBoundingBox().move(arrival.subtract(pig.position()));
        helper.assertTrue(target.noCollision(pig, box), "Cross-dimension arrival is clear");
        helper.assertTrue(!target.containsAnyLiquid(box), "Cross-dimension arrival is dry");
        // A newly loaded remote chunk may not yet expose its entities through getEntity.
        // Observe the actual join event instead of depending on chunk ticking timing.
        var transferred = new java.util.concurrent.atomic.AtomicReference<net.minecraft.world.entity.Entity>();
        java.util.function.Consumer<net.neoforged.neoforge.event.entity.EntityJoinLevelEvent> listener = event -> {
            if (event.getLevel() == target && event.getEntity().getUUID().equals(id)) {
                transferred.set(event.getEntity());
            }
        };
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(listener);
        try {
            teleporter.serverTick();
        } finally {
            net.neoforged.neoforge.common.NeoForge.EVENT_BUS.unregister(listener);
        }
        var moved = transferred.get();
        helper.assertTrue(moved != null && moved.getType() == EntityType.PIG && pig.isRemoved(),
                "Cross-dimension transfer recreates the mob with the same UUID");
        helper.assertTrue(moved.position().distanceTo(arrival) < 0.01,
                "Cross-dimension receiver position is preserved");
        moved.discard();
        target.destroyBlock(destination, false);
        for (BlockPos pos : Multiblocks.platform(destination)) {
            target.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
        }
        helper.succeed();
    }

    private static void buildTeleporter(GameTestHelper helper) {
        Multiblocks.teleporterMembers(TELEPORTER).forEach(pos -> helper.setBlock(pos, ModBlocks.TELEPORTER_BASE.get()));
        Multiblocks.platform(TELEPORTER.above(3)).forEach(pos -> helper.setBlock(pos, Blocks.GLASS));
    }

    private static void buildReceiver(GameTestHelper helper, BlockPos center) {
        Multiblocks.platform(center).forEach(pos -> helper.setBlock(pos, ModBlocks.RECEIVER_BASE.get()));
    }

    private static Player playerNorthOf(GameTestHelper helper, BlockPos pos) {
        Player player = helper.makeMockPlayer(GameType.CREATIVE);
        Vec3 location = helper.absoluteVec(Vec3.atLowerCornerOf(pos).add(0.5, 0, -3));
        player.setPos(location);
        return player;
    }

    private static void assertFormedTeleporter(GameTestHelper helper) {
        helper.assertBlockPresent(ModBlocks.TELEPORTER.get(), TELEPORTER);
        for (BlockPos member : Multiblocks.teleporterMembers(TELEPORTER)) {
            helper.assertTrue(helper.getBlockState(member).getValue(MultiblockBlock.FORMED), "Teleporter member formed: " + member);
        }
    }

    private static boolean hasSavedReceiver(GameTestHelper helper, GlobalPos pos) {
        var entries = ReceiverSavedData.get(helper.getLevel()).save(new CompoundTag(), helper.getLevel().registryAccess())
                .getList("receivers", net.minecraft.nbt.Tag.TAG_COMPOUND);
        for (int i = 0; i < entries.size(); i++) {
            var entry = entries.getCompound(i);
            if (entry.getLong("pos") == pos.pos().asLong()
                    && entry.getString("dimension").equals(pos.dimension().location().toString())) {
                return true;
            }
        }
        return false;
    }
}
