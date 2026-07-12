package net.masked.createacidic.client.overlay;

import net.masked.createacidic.api.IHaveScienceGoggleInformation;
import net.masked.createacidic.item.ScienceGogglesItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

import net.minecraftforge.client.gui.overlay.IGuiOverlay;

import java.util.ArrayList;
import java.util.List;

public class ScienceGogglesOverlayRenderer {

    public static final IGuiOverlay OVERLAY = ScienceGogglesOverlayRenderer::renderOverlay;

    private static int hoverTicks = 0;

    private static void renderOverlay(net.minecraftforge.client.gui.overlay.ForgeGui gui, GuiGraphics graphics,
                                      float partialTicks, int width, int height) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.options.hideGui || mc.player == null || mc.level == null)
            return;
        if (mc.gameMode != null && mc.gameMode.getPlayerMode() == GameType.SPECTATOR)
            return;

        if (!ScienceGogglesItem.isWearingGoggles(mc.player)) {
            hoverTicks = 0;
            return;
        }

        HitResult hit = mc.hitResult;
        if (!(hit instanceof BlockHitResult blockHit)) {
            hoverTicks = 0;
            return;
        }

        BlockEntity be = mc.level.getBlockEntity(blockHit.getBlockPos());
        if (!(be instanceof IHaveScienceGoggleInformation info)) {
            hoverTicks = 0;
            return;
        }

        List<Component> tooltip = new ArrayList<>();
        boolean isSneaking = mc.player.isShiftKeyDown();
        boolean added = info.addToScienceGoggleTooltip(tooltip, isSneaking);

        if (!added || tooltip.isEmpty()) {
            hoverTicks = 0;
            return;
        }

        hoverTicks++;

        int textWidth = 0;
        for (Component line : tooltip)
            textWidth = Math.max(textWidth, mc.font.width(line));

        int lineHeight = 10;
        int textHeight = 8 + (tooltip.size() > 1 ? 2 + (tooltip.size() - 1) * lineHeight : 0);

        int posX = width / 2 + 20;
        int posY = height / 2 - 30;
        posX = Math.min(posX, width - textWidth - 24);
        posY = Math.min(posY, height - textHeight - 20);

        float fade = net.minecraft.util.Mth.clamp((hoverTicks + partialTicks) / 24f, 0f, 1f);

        graphics.pose().pushPose();
        graphics.pose().translate(0, 0, 400);

        int bgAlpha = (int) (0xF0 * fade) << 24;
        int background = bgAlpha | 0x100010;
        int borderTop = ((int) (0x50 * fade) << 24) | 0x5000FF;
        int borderBot = ((int) (0x28 * fade) << 24) | 0x5000FF;

        graphics.fillGradient(posX - 4, posY - 4, posX + textWidth + 4, posY + textHeight + 4, background, background);
        graphics.fillGradient(posX - 4, posY - 4, posX + textWidth + 4, posY - 3, borderTop, borderTop);
        graphics.fillGradient(posX - 4, posY + textHeight + 3, posX + textWidth + 4, posY + textHeight + 4, borderBot, borderBot);
        graphics.fillGradient(posX - 4, posY - 3, posX - 3, posY + textHeight + 3, borderTop, borderBot);
        graphics.fillGradient(posX + textWidth + 3, posY - 3, posX + textWidth + 4, posY + textHeight + 3, borderTop, borderBot);

        ItemStack icon = new ItemStack(net.masked.createacidic.registry.ModItems.SCIENCE_GOGGLES.get());
        graphics.renderItem(icon, posX - 20, posY - 2);

        int textAlpha = (int) (0xFF * fade) << 24;
        int y = posY;
        for (int i = 0; i < tooltip.size(); i++) {
            Component line = tooltip.get(i);
            graphics.drawString(mc.font, line, posX, y, 0xFFFFFF | textAlpha, true);
            y += lineHeight;
            if (i == 0 && tooltip.size() > 1)
                y += 2;
        }

        graphics.pose().popPose();
    }
}