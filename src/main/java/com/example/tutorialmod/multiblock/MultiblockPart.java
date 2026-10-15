package com.example.tutorialmod.multiblock;

import net.minecraft.util.StringRepresentable;

import java.util.Locale;

/** Visual roles of the original construction blocks after formation */
public enum MultiblockPart implements StringRepresentable {
    PLATFORM, CORNER, PILLAR, UPPER_PILLAR;

    @Override
    public String getSerializedName() {
        return name().toLowerCase(Locale.ROOT);
    }
}
