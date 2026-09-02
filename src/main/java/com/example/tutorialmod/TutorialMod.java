package com.example.tutorialmod;

import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;

/**
 * The main class for the mod.
 *
 * <p>NeoForge creates this class when it finds the {@link Mod} annotation.</p>
 */
@Mod(TutorialMod.MOD_ID)
public final class TutorialMod {
    // This must match mod_id in gradle.properties.
    public static final String MOD_ID = "tutorialmod";

    private static final Logger LOGGER = LogUtils.getLogger();

    public TutorialMod(IEventBus modEventBus) {
        // We will use modEventBus in later tutorials to register blocks and items.
        LOGGER.info("Tutorial Mod is loading!");
    }
}
