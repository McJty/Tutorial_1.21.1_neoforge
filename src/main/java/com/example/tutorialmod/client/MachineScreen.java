package com.example.tutorialmod.client;

import com.example.tutorialmod.TutorialMod;
import com.example.tutorialmod.menu.MachineMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

/**
 * Shared textured inventory screen for the generator and pig spawner.
 */
public final class MachineScreen extends AbstractContainerScreen<MachineMenu> {
    private static final ResourceLocation BACKGROUND_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(TutorialMod.MOD_ID, "textures/gui/machine.png");
    private static final int TEXTURE_SIZE = 256;
    private static final int ENERGY_BAR_COLOR = 0xffd9ad32;
    private static final int TEXT_COLOR = 0xff404040;

    public MachineScreen(MachineMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int x = leftPos;
        int y = topPos;

        graphics.blit(
                BACKGROUND_TEXTURE,
                x, y,
                0, 0,
                imageWidth, imageHeight,
                TEXTURE_SIZE, TEXTURE_SIZE
        );

        int width = menu.getCapacity() == 0 ? 0 : 80 * menu.getEnergy() / menu.getCapacity();
        graphics.fill(x + 80, y + 35, x + 80 + width, y + 47, ENERGY_BAR_COLOR);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        super.renderLabels(graphics, mouseX, mouseY);
        graphics.drawString(
                font,
                Component.translatable(menu.isGenerator() ? "gui.tutorialmod.fuel" : "gui.tutorialmod.food"),
                36, 23,
                TEXT_COLOR,
                false
        );
        graphics.drawString(
                font,
                Component.translatable("gui.tutorialmod.energy", menu.getEnergy(), menu.getCapacity()),
                78, 52,
                TEXT_COLOR,
                false
        );
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }
}
