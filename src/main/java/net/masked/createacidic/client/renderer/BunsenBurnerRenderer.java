package net.masked.createacidic.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.masked.createacidic.block.entity.BunsenBurnerBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;

import java.util.List;

public class BunsenBurnerRenderer implements BlockEntityRenderer<BunsenBurnerBlockEntity> {

    public BunsenBurnerRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(BunsenBurnerBlockEntity burner, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();

        long time = burner.getLevel() != null ? burner.getLevel().getGameTime() : 0;
        float bob = (float) Math.sin((time + partialTick) / 10.0) * 0.015f;
        float rotation = ((time + partialTick) * 2f) % 360f;

        if (burner.isBrewOutputReady()) {
            List<ItemStack> results = burner.getBrewOutputResults();
            renderResultStacks(results, poseStack, bufferSource, packedLight, packedOverlay,
                    itemRenderer, bob, rotation);
            return;
        }

        ItemStackHandler bottleSlot = burner.getBottleSlot();
        ItemStack stack = bottleSlot.getStackInSlot(0);
        if (stack.isEmpty()) return;

        poseStack.pushPose();
        poseStack.translate(0.5, 1.15 + bob, 0.5);
        poseStack.scale(0.4f, 0.4f, 0.4f);
        poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(rotation));

        itemRenderer.renderStatic(stack, ItemDisplayContext.FIXED,
                packedLight, packedOverlay, poseStack, bufferSource, null, 0);

        poseStack.popPose();
    }

    private void renderResultStacks(List<ItemStack> results, PoseStack poseStack, MultiBufferSource bufferSource,
                                    int packedLight, int packedOverlay, ItemRenderer itemRenderer,
                                    float bob, float rotation) {
        if (results.isEmpty()) return;

        // spread multiple result items out in a small circle above the burner
        double radius = results.size() > 1 ? 0.15 : 0.0;
        double angleStep = 360.0 / results.size();

        for (int i = 0; i < results.size(); i++) {
            ItemStack stack = results.get(i);
            if (stack.isEmpty()) continue;

            double angle = Math.toRadians(angleStep * i);
            double x = 0.5 + radius * Math.cos(angle);
            double z = 0.5 + radius * Math.sin(angle);

            poseStack.pushPose();
            poseStack.translate(x, 1.15 + bob, z);
            poseStack.scale(0.35f, 0.35f, 0.35f);
            poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(rotation));

            itemRenderer.renderStatic(stack, ItemDisplayContext.FIXED,
                    packedLight, packedOverlay, poseStack, bufferSource, null, 0);

            poseStack.popPose();
        }
    }
}