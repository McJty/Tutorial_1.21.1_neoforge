package com.example.tutorialmod;

import com.example.tutorialmod.block.GeneratorBlock;
import com.example.tutorialmod.block.entity.GeneratorBlockEntity;
import com.example.tutorialmod.block.entity.MachineBlockEntity;
import com.example.tutorialmod.block.entity.PigSpawnerBlockEntity;
import com.example.tutorialmod.registration.ModBlocks;
import com.example.tutorialmod.registration.ModDataComponents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(TutorialMod.MOD_ID)
@PrefixGameTestTemplate(false)
public final class MachineGameTests {
    @GameTest(template = "empty", timeoutTicks = 220)
    public static void chargesForTenSeconds(GameTestHelper helper) {
        helper.setBlock(2, 1, 2, ModBlocks.GENERATOR.get());
        helper.setBlock(3, 1, 2, ModBlocks.PIG_SPAWNER.get());
        GeneratorBlockEntity generator = helper.getBlockEntity(new BlockPos(2, 1, 2));
        PigSpawnerBlockEntity spawner = helper.getBlockEntity(new BlockPos(3, 1, 2));
        generator.inventory.setStackInSlot(0, new ItemStack(Items.COAL));
        spawner.inventory.setStackInSlot(0, new ItemStack(Items.CARROT, 2));
        helper.runAfterDelay(199, () -> {
            helper.assertEntityNotPresent(EntityType.PIG);
            helper.assertTrue(
                    spawner.energy.getEnergyStored() >= 3960,
                    "Buffer should be nearly full after 199 ticks"
            );
        });
        helper.runAfterDelay(201, () -> {
            helper.assertEntitiesPresent(EntityType.PIG, 1);
            helper.assertTrue(spawner.inventory.getStackInSlot(0).getCount() == 1, "One food per pig");
            helper.assertTrue(spawner.energy.getEnergyStored() <= 40, "Spawning empties the buffer");
            helper.succeed();
        });
    }

    @GameTest(template = "empty")
    public static void requiresFoodPowerAndSpace(GameTestHelper helper) {
        BlockPos pos = new BlockPos(3, 1, 3);
        helper.setBlock(pos, ModBlocks.PIG_SPAWNER.get());
        PigSpawnerBlockEntity spawner = helper.getBlockEntity(pos);
        spawner.setEnergy(PigSpawnerBlockEntity.CAPACITY);
        spawner.serverTick();
        helper.assertEntityNotPresent(EntityType.PIG);
        helper.assertTrue(!spawner.shouldShowPreview(), "Power alone should not show a pig preview");
        helper.assertTrue(spawner.energy.getEnergyStored() == 4000, "No food must preserve energy");
        spawner.inventory.setStackInSlot(0, new ItemStack(Items.CARROT));
        spawner.setEnergy(3999);
        spawner.serverTick();
        helper.assertEntityNotPresent(EntityType.PIG);
        helper.assertTrue(spawner.shouldShowPreview(), "Food and partial power show a pig preview");
        helper.assertTrue(spawner.getUpdateTag(helper.getLevel().registryAccess()).getBoolean("preview_ready"),
                "The preview state is sent to clients");
        helper.assertTrue(
                spawner.inventory.getStackInSlot(0).getCount() == 1,
                "Partial energy must preserve food"
        );
        spawner.setEnergy(4000);
        helper.setBlock(pos.above(), Blocks.STONE);
        spawner.serverTick();
        helper.assertEntityNotPresent(EntityType.PIG);
        helper.assertTrue(spawner.energy.getEnergyStored() == 4000, "Blocked spawn must preserve energy");
        helper.assertTrue(spawner.shouldShowPreview(), "The preview remains while spawning is blocked");
        helper.setBlock(pos.above(), Blocks.AIR);
        spawner.serverTick();
        helper.assertEntitiesPresent(EntityType.PIG, 1);
        helper.assertTrue(!spawner.shouldShowPreview(), "The preview disappears when the pig spawns");
        var absolutePos = helper.absolutePos(pos);
        var pigs = helper.getLevel().getEntities(
                EntityType.PIG,
                new AABB(absolutePos).inflate(2),
                pig -> true
        );
        helper.assertTrue(
                pigs.size() == 1 && Math.abs(pigs.getFirst().getY() - (absolutePos.getY() + 0.5)) < 0.01,
                "Pig should stand on the half-height platform"
        );
        helper.assertTrue(
                spawner.getBlockState().getCollisionShape(helper.getLevel(), absolutePos).max(Direction.Axis.Y) == 0.5,
                "Spawner collision should match a bottom slab"
        );
        helper.assertTrue(
                spawner.energy.getEnergyStored() == 0 && spawner.inventory.getStackInSlot(0).isEmpty(),
                "Successful spawn consumes both resources"
        );
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void inputLimitAppliesPerPush(GameTestHelper helper) {
        BlockPos pos = new BlockPos(3, 1, 3);
        helper.setBlock(pos, ModBlocks.PIG_SPAWNER.get());
        var level = helper.getLevel();
        var north = level.getCapability(
                Capabilities.EnergyStorage.BLOCK,
                helper.absolutePos(pos),
                Direction.NORTH
        );
        var south = level.getCapability(
                Capabilities.EnergyStorage.BLOCK,
                helper.absolutePos(pos),
                Direction.SOUTH
        );
        helper.assertTrue(north != null && south != null, "Energy capability on each side");
        helper.assertTrue(north.receiveEnergy(1000, true) == 20, "Simulated transfer is capped");
        helper.assertTrue(north.receiveEnergy(1000, false) == 20, "Simulation does not transfer power");
        helper.assertTrue(north.receiveEnergy(1000, false) == 20, "A second push can also transfer power");
        helper.assertTrue(south.receiveEnergy(1000, false) == 20, "Another side can transfer power too");
        PigSpawnerBlockEntity spawner = helper.getBlockEntity(pos);
        helper.assertTrue(spawner.energy.getEnergyStored() == 60, "Three pushes each add 20 power");
        helper.assertTrue(south.extractEnergy(1000, false) == 0, "Spawner does not export power");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void lootAndSavePreserveEnergy(GameTestHelper helper) {
        for (Block block : new Block[]{ModBlocks.GENERATOR.get(), ModBlocks.PIG_SPAWNER.get()}) {
            BlockPos pos = new BlockPos(2, 1, 2);
            helper.setBlock(pos, block);
            MachineBlockEntity machine = helper.getBlockEntity(pos);
            machine.setEnergy(1234);
            machine.inventory.setStackInSlot(
                    0,
                    new ItemStack(block == ModBlocks.GENERATOR.get() ? Items.COAL : Items.CARROT, 3)
            );
            var saved = machine.saveWithoutMetadata(helper.getLevel().registryAccess());
            var drops = Block.getDrops(
                    machine.getBlockState(),
                    helper.getLevel(),
                    helper.absolutePos(pos),
                    machine
            );
            helper.assertTrue(drops.size() == 1 && drops.getFirst().getOrDefault(ModDataComponents.ENERGY.get(), 0) == 1234,
                    "Generated loot copies stored energy");
            helper.setBlock(pos, Blocks.AIR);
            helper.setBlock(pos, block);
            MachineBlockEntity restored = helper.getBlockEntity(pos);
            restored.applyComponentsFromItemStack(drops.getFirst());
            helper.assertTrue(
                    restored.energy.getEnergyStored() == 1234,
                    "Placement component hook restores energy"
            );
            restored.loadWithComponents(saved, helper.getLevel().registryAccess());
            helper.assertTrue(restored.energy.getEnergyStored() == 1234 && restored.inventory.getStackInSlot(0).getCount() == 3,
                    "World save restores inventory and energy");
        }

        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 20)
    public static void pushesToEveryNeighbour(GameTestHelper helper) {
        BlockPos center = new BlockPos(3, 2, 3);
        helper.setBlock(center, ModBlocks.GENERATOR.get());
        GeneratorBlockEntity generator = helper.getBlockEntity(center);
        generator.inventory.setStackInSlot(0, new ItemStack(Items.OAK_PLANKS));
        generator.setEnergy(GeneratorBlockEntity.GENERATION_RATE * Direction.values().length);
        for (Direction side : Direction.values()) {
            helper.setBlock(center.relative(side), ModBlocks.PIG_SPAWNER.get());
        }

        helper.runAfterDelay(1, () -> {
            for (Direction side : Direction.values()) {
                PigSpawnerBlockEntity consumer = helper.getBlockEntity(center.relative(side));
                helper.assertTrue(
                        consumer.energy.getEnergyStored() == GeneratorBlockEntity.GENERATION_RATE,
                        "Every neighbour receives a full transfer: " + side
                );
            }

            helper.assertTrue(
                    generator.getBlockState().getValue(GeneratorBlock.ON),
                    "Fuel turns on the generator"
            );
            helper.succeed();
        });
    }
}
