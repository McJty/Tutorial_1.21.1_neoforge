package com.example.tutorialmod.integration.top;

import com.example.tutorialmod.TutorialMod;
import com.example.tutorialmod.block.TutorialBlock;
import com.example.tutorialmod.registration.ModBlocks;
import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.IProbeInfoProvider;
import mcjty.theoneprobe.api.ProbeMode;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public final class TutorialBlockProbeProvider implements IProbeInfoProvider {
    private static final ResourceLocation PROVIDER_ID =
            ResourceLocation.fromNamespaceAndPath(TutorialMod.MOD_ID, "tutorial_block");

    @Override
    public ResourceLocation getID() {
        return PROVIDER_ID;
    }

    @Override
    public void addProbeInfo(
            ProbeMode mode,
            IProbeInfo probeInfo,
            Player player,
            Level level,
            BlockState blockState,
            IProbeHitData hitData
    ) {
        if (!blockState.is(ModBlocks.TUTORIAL_BLOCK.get())) {
            return;
        }

        String stateKey = blockState.getValue(TutorialBlock.ON)
                ? "state.tutorialmod.on"
                : "state.tutorialmod.off";
        probeInfo.text(Component.translatable(
                "top.tutorialmod.state",
                Component.translatable(stateKey)
        ));
    }
}
