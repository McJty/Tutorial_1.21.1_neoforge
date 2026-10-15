package com.example.tutorialmod.multiblock;

import com.example.tutorialmod.block.entity.ColorControllerBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Stored in the Overworld, so every dimension shares the same receiver directory. */
public final class ReceiverSavedData extends SavedData {
    private static final Factory<ReceiverSavedData> FACTORY =
            new Factory<>(ReceiverSavedData::new, ReceiverSavedData::load);
    private final Map<GlobalPos, ReceiverColors> receivers = new HashMap<>();

    // Keep a snapshot of the receiver's colors, rather than a reference to its mutable BE.
    private record ReceiverColors(DyeColor color1, DyeColor color2, DyeColor color3) {
    }

    public static ReceiverSavedData get(ServerLevel level) {
        return level.getServer().overworld().getDataStorage().computeIfAbsent(FACTORY, "tutorialmod_receivers");
    }

    public void setReceiver(GlobalPos pos, DyeColor color1, DyeColor color2, DyeColor color3) {
        ReceiverColors colors = new ReceiverColors(color1, color2, color3);
        ReceiverColors previous = receivers.put(pos, colors);
        if (!colors.equals(previous)) {
            setDirty();
        }
    }

    public void removeReceiver(GlobalPos pos) {
        if (receivers.remove(pos) != null) {
            setDirty();
        }
    }

    public GlobalPos randomReceiver(ServerLevel source, DyeColor color1, DyeColor color2, DyeColor color3) {
        ReceiverColors wanted = new ReceiverColors(color1, color2, color3);
        List<GlobalPos> candidates = new ArrayList<>();
        receivers.forEach((pos, colors) -> {
            if (colors.equals(wanted)) {
                candidates.add(pos);
            }
        });
        while (!candidates.isEmpty()) {
            GlobalPos destination = candidates.remove(source.random.nextInt(candidates.size()));
            ServerLevel target = source.getServer().getLevel(destination.dimension());
            if (target == null) {
                continue;
            }
            // Load only a selected candidate, rather than requiring all receivers to stay loaded
            target.getChunkAt(destination.pos());
            if (target.getBlockEntity(destination.pos()) instanceof ColorControllerBlockEntity receiver
                    && receiver.isReceiver()) {
                ReceiverColors actual = new ReceiverColors(receiver.getColor1(), receiver.getColor2(), receiver.getColor3());
                if (actual.equals(wanted)) {
                    return destination;
                }
                setReceiver(destination, receiver.getColor1(), receiver.getColor2(), receiver.getColor3());
            } else {
                // World edits can leave stale entries. Repair them when encountered
                removeReceiver(destination);
            }
        }
        return null;
    }

    public static ReceiverSavedData load(CompoundTag tag, HolderLookup.Provider registries) {
        ReceiverSavedData data = new ReceiverSavedData();
        ListTag entries = tag.getList("receivers", Tag.TAG_COMPOUND);
        for (int i = 0; i < entries.size(); i++) {
            CompoundTag entry = entries.getCompound(i);
            ResourceLocation dimension = ResourceLocation.tryParse(entry.getString("dimension"));
            if (dimension != null) {
                ReceiverColors colors = new ReceiverColors(
                        DyeColor.byId(entry.getByte("color1")),
                        DyeColor.byId(entry.getByte("color2")),
                        DyeColor.byId(entry.getByte("color3")));
                data.receivers.put(GlobalPos.of(ResourceKey.create(Registries.DIMENSION, dimension),
                        BlockPos.of(entry.getLong("pos"))), colors);
            }
        }
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        ListTag entries = new ListTag();
        receivers.forEach((pos, colors) -> {
            CompoundTag entry = new CompoundTag();
            entry.putString("dimension", pos.dimension().location().toString());
            entry.putLong("pos", pos.pos().asLong());
            entry.putByte("color1", (byte) colors.color1().getId());
            entry.putByte("color2", (byte) colors.color2().getId());
            entry.putByte("color3", (byte) colors.color3().getId());
            entries.add(entry);
        });
        tag.put("receivers", entries);
        return tag;
    }
}
