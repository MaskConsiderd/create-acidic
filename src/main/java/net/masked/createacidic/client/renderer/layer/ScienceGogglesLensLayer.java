package net.masked.createacidic.client.renderer.layer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.masked.createacidic.item.ScienceGogglesItem;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

/**
 * Draws only the tinted lens pixels on top of the (now-punched-through) base
 * armor texture, using a translucent render type so the lens actually blends
 * instead of being rounded to opaque by vanilla's cutout armor rendering.
 */
public class ScienceGogglesLensLayer<T extends LivingEntity, M extends HumanoidModel<T>>
        extends RenderLayer<T, M> {

    // Separate texture: everything transparent except the tinted lens pixels.
    private static final ResourceLocation LENS_TEXTURE =
            new ResourceLocation("acidic", "textures/models/armor/scientist_wear_lens.png");

    public ScienceGogglesLensLayer(RenderLayerParent<T, M> parent) {
        super(parent);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight,
                       T entity, float limbSwing, float limbSwingAmount, float partialTick,
                       float ageInTicks, float netHeadYaw, float headPitch) {

        ItemStack helmet = entity.getItemBySlot(EquipmentSlot.HEAD);
        if (!(helmet.getItem() instanceof ScienceGogglesItem)) return;

        M model = getParentModel();

        VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.entityTranslucent(LENS_TEXTURE));

        model.head.render(poseStack, vertexConsumer, packedLight,
                OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
        model.hat.render(poseStack, vertexConsumer, packedLight,
                OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
    }
}