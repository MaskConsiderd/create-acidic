package net.masked.createacidic.client.event;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.masked.createacidic.registry.ModItems;
import net.masked.createacidic.client.model.LabCoatSleeveModel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;

import net.minecraftforge.client.event.RenderArmEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "acidic", bus = Mod.EventBusSubscriber.Bus.FORGE, value = net.minecraftforge.api.distmarker.Dist.CLIENT)
public class FirstPersonSleeveHandler {

    private static final ResourceLocation SLEEVE_TEXTURE =
            new ResourceLocation("acidic", "textures/models/armor/lab_coat_sleeve.png");

    @SubscribeEvent
    public static void onRenderArm(RenderArmEvent event) {
        var player = event.getPlayer();
        var chest = player.getItemBySlot(EquipmentSlot.CHEST);
        if (chest.getItem() != ModItems.LAB_COAT.get())
            return;

        VertexConsumer buffer = event.getMultiBufferSource()
                .getBuffer(RenderType.entityCutoutNoCull(SLEEVE_TEXTURE));

        boolean isRightArm = event.getArm() == HumanoidArm.RIGHT;

        double xOffset = isRightArm ? -7.5 / 16.0 : 7.5 / 16.0;
        double yOffset = isRightArm ? 2.0 / 16.0 : 3.0 / 16.0; // tune this for left arm
        double zOffset = 0.0 / 16.0;

        event.getPoseStack().pushPose();
        event.getPoseStack().translate(xOffset, yOffset, zOffset);

        LabCoatSleeveModel.render(event.getPoseStack(), buffer, event.getPackedLight(), event.getArm());

        event.getPoseStack().popPose();

        // Not cancelling the event: vanilla still renders the bare arm/hand+item
        // underneath, we're just layering the sleeve cuboid on top of it.
    }
}