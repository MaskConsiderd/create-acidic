package net.masked.createacidic.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.client.model.geom.builders.CubeDeformation;

public class LabCoatSleeveModel {

    private static ModelPart rightSleeve;
    private static ModelPart leftSleeve;

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        // Slightly inflated (0.35) over the vanilla 4x12x4 arm cuboid so the sleeve
        // reads as a layer of fabric rather than a re-skinned arm.
        root.addOrReplaceChild("right_sleeve",
                CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.35F)),
                PartPose.ZERO);

        root.addOrReplaceChild("left_sleeve",
                CubeListBuilder.create()
                        .texOffs(0, 16)
                        .addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.35F)),
                PartPose.ZERO);

        return LayerDefinition.create(mesh, 64, 32);
    }

    public static void bake(ModelPart layerRoot) {
        rightSleeve = layerRoot.getChild("right_sleeve");
        leftSleeve = layerRoot.getChild("left_sleeve");
    }

    public static void render(PoseStack poseStack, VertexConsumer buffer, int packedLight, HumanoidArm arm) {
        ModelPart part = (arm == HumanoidArm.RIGHT) ? rightSleeve : leftSleeve;
        if (part == null) return;

        part.render(poseStack, buffer, packedLight, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
    }
}