package com.example.tutorialmod.datagen;

import com.example.tutorialmod.TutorialMod;
import com.google.common.hash.Hashing;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;

/**
 * Generates a pixel-aligned GUI atlas with the standard recessed inventory slots.
 */
public final class ModGuiTextureProvider implements DataProvider {
    private static final int TEXTURE_SIZE = 256;
    private static final int PANEL_WIDTH = 176;
    private static final int PANEL_HEIGHT = 166;
    private static final int OUTLINE_COLOR = 0xff000000;
    private static final int PANEL_COLOR = 0xffc6c6c6;
    private static final int PANEL_SHADOW_COLOR = 0xff555555;
    private static final int HIGHLIGHT_COLOR = 0xffffffff;
    private static final int SLOT_SHADOW_COLOR = 0xff373737;
    private static final int SLOT_BACKGROUND_COLOR = 0xff8b8b8b;

    private final PackOutput output;

    public ModGuiTextureProvider(PackOutput output) {
        this.output = output;
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        BufferedImage texture = new BufferedImage(TEXTURE_SIZE, TEXTURE_SIZE, BufferedImage.TYPE_INT_ARGB);
        fill(texture, 0, 0, PANEL_WIDTH, PANEL_HEIGHT, OUTLINE_COLOR);
        fill(texture, 1, 1, PANEL_WIDTH - 2, PANEL_HEIGHT - 2, PANEL_SHADOW_COLOR);
        fill(texture, 1, 1, PANEL_WIDTH - 3, 2, HIGHLIGHT_COLOR);
        fill(texture, 1, 1, 2, PANEL_HEIGHT - 3, HIGHLIGHT_COLOR);
        fill(texture, 3, 3, PANEL_WIDTH - 6, PANEL_HEIGHT - 6, PANEL_COLOR);

        // Slot interiors match MachineMenu's item coordinates exactly.
        recessedBox(texture, 43, 34, 18, 18);
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                recessedBox(texture, 7 + column * 18, 83 + row * 18, 18, 18);
            }
        }

        for (int column = 0; column < 9; column++) {
            recessedBox(texture, 7 + column * 18, 141, 18, 18);
        }

        recessedBox(texture, 79, 34, 82, 14);

        try {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            ImageIO.write(texture, "png", bytes);
            byte[] png = bytes.toByteArray();
            cache.writeIfNeeded(
                    output.getOutputFolder(PackOutput.Target.RESOURCE_PACK)
                            .resolve(TutorialMod.MOD_ID + "/textures/gui/machine.png"),
                    png,
                    Hashing.sha1().hashBytes(png)
            );
            return CompletableFuture.completedFuture(null);
        } catch (IOException exception) {
            return CompletableFuture.failedFuture(exception);
        }
    }

    private static void recessedBox(BufferedImage texture, int x, int y, int width, int height) {
        fill(texture, x, y, width, height, SLOT_BACKGROUND_COLOR);
        fill(texture, x, y, width - 1, 1, SLOT_SHADOW_COLOR);
        fill(texture, x, y, 1, height - 1, SLOT_SHADOW_COLOR);
        fill(texture, x + 1, y + height - 1, width - 1, 1, HIGHLIGHT_COLOR);
        fill(texture, x + width - 1, y + 1, 1, height - 1, HIGHLIGHT_COLOR);
    }

    private static void fill(BufferedImage texture, int x, int y, int width, int height, int color) {
        for (int row = y; row < y + height; row++) {
            for (int column = x; column < x + width; column++) {
                texture.setRGB(column, row, color);
            }
        }
    }

    @Override
    public String getName() {
        return "Tutorial machine GUI texture";
    }
}
