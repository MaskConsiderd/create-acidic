package net.masked.createacidic.client.screen;

import net.masked.createacidic.CreateAcidic;
import net.masked.createacidic.menu.BunsenBurnerMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class BunsenBurnerScreen extends AbstractContainerScreen<BunsenBurnerMenu> {

    private static final ResourceLocation TEXTURE =
            new ResourceLocation(CreateAcidic.MODID, "textures/gui/bunsen_burner_gui.png");

    // Flame icon sprite location on the texture sheet (outside the visible panel,
    // same convention as vanilla's furnace flame meter)
    private static final int FLAME_ICON_U = 176;
    private static final int FLAME_ICON_V = 0;
    private static final int FLAME_ICON_WIDTH = 14;
    private static final int FLAME_ICON_HEIGHT = 14;

    // On-screen position for the flame meter, next to the fuel slot (slot is at 79,44)
    private static final int FLAME_SCREEN_X = 80;
    private static final int FLAME_SCREEN_Y = 28;

    public BunsenBurnerScreen(BunsenBurnerMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 176;
        this.imageHeight = 166;
        this.inventoryLabelY = this.imageHeight - 94;
        this.titleLabelX = 50;
        this.titleLabelY = 6;
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;
        guiGraphics.blit(TEXTURE, x, y, 0, 0, imageWidth, imageHeight);

        renderFlameMeter(guiGraphics, x, y);
    }

    private void renderFlameMeter(GuiGraphics guiGraphics, int panelX, int panelY) {
        float fraction = getFuelFraction();
        if (fraction <= 0f) return;

        int revealHeight = Math.round(FLAME_ICON_HEIGHT * fraction);
        if (revealHeight <= 0) return;

        int destX = panelX + FLAME_SCREEN_X;
        int destY = panelY + FLAME_SCREEN_Y + (FLAME_ICON_HEIGHT - revealHeight);
        int srcY = FLAME_ICON_V + (FLAME_ICON_HEIGHT - revealHeight);

        guiGraphics.blit(TEXTURE, destX, destY, FLAME_ICON_U, srcY,
                FLAME_ICON_WIDTH, revealHeight);
    }

    private float getFuelFraction() {
        int fuel = menu.blockEntity.getFuelPoints();
        float max = net.masked.createacidic.block.entity.BunsenBurnerBlockEntity.FUEL_PER_LAVA_BUCKET;
        return Math.min(1f, fuel / max);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        renderTooltip(guiGraphics, mouseX, mouseY);
    }
}