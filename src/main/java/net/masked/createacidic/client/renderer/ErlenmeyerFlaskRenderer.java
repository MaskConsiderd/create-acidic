package net.masked.createacidic.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.masked.createacidic.block.entity.ErlenmeyerFlaskBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.items.ItemStackHandler;

public class ErlenmeyerFlaskRenderer implements BlockEntityRenderer<ErlenmeyerFlaskBlockEntity> {

    // How far to inset the fluid quad from the glass walls, in block units (1/16 = 1 pixel).
    // Prevents z-fighting/flickering against the flask's glass model.
    private static final float WALL_INSET = 1f / 16f;

    // Height of a single texture tile in block units. Fluid textures are authored as
    // 16x16 (or 16xN) tiles meant to repeat every block, same as vanilla water/lava.
    private static final float TILE_HEIGHT = 1f;

    public ErlenmeyerFlaskRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(ErlenmeyerFlaskBlockEntity flask, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        ItemStackHandler inventory = flask.getInventory();
        ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();

        long time = flask.getLevel() != null ? flask.getLevel().getGameTime() : 0;
        float bob = (float) Math.sin((time + partialTick) / 10.0) * 0.02f;
        float rotation = ((time + partialTick) * 2f) % 360f;

        // Bulb center, roughly mid-height of the base (before the neck begins)
        double centerX = 0.5;
        double centerZ = 0.5;
        double baseY = 0.28;

        renderFluid(flask.getFluidTank().getFluid(), flask.getFluidTank().getFluidAmount(),
                ErlenmeyerFlaskBlockEntity.TANK_CAPACITY_MB, poseStack, bufferSource, packedLight);

        renderStackInFlask(inventory.getStackInSlot(0), poseStack, bufferSource, packedLight, packedOverlay,
                itemRenderer, centerX, baseY + bob, centerZ, rotation);
    }

    private void renderStackInFlask(ItemStack stack, PoseStack poseStack, MultiBufferSource bufferSource,
                                    int packedLight, int packedOverlay, ItemRenderer itemRenderer,
                                    double x, double y, double z, float rotationDegrees) {
        if (stack.isEmpty()) return;

        poseStack.pushPose();
        poseStack.translate(x, y, z);
        poseStack.scale(0.4f, 0.4f, 0.4f);
        poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(rotationDegrees));

        itemRenderer.renderStatic(stack, net.minecraft.world.item.ItemDisplayContext.FIXED,
                packedLight, packedOverlay, poseStack, bufferSource, null, 0);

        poseStack.popPose();
    }

    /** Renders the fluid as a tiled, inset "pool" inside the flask's bulb, scaled to fill %. */
    private void renderFluid(FluidStack fluidStack, int amount, int capacity,
                             PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        if (fluidStack.isEmpty() || amount <= 0) return;

        IClientFluidTypeExtensions extensions = IClientFluidTypeExtensions.of(fluidStack.getFluid());
        TextureAtlasSprite sprite = Minecraft.getInstance()
                .getTextureAtlas(InventoryMenu.BLOCK_ATLAS)
                .apply(extensions.getStillTexture(fluidStack));
        TextureAtlasSprite flowingSprite = Minecraft.getInstance()
                .getTextureAtlas(InventoryMenu.BLOCK_ATLAS)
                .apply(extensions.getFlowingTexture(fluidStack));

        int tint = extensions.getTintColor(fluidStack);
        float a = ((tint >> 24) & 0xFF) / 255f;
        float r = ((tint >> 16) & 0xFF) / 255f;
        float g = ((tint >> 8) & 0xFF) / 255f;
        float b = (tint & 0xFF) / 255f;
        if (a == 0f) a = 1f; // some fluids leave alpha at 0 in their tint and rely on texture alpha

        float fillPercent = Math.min(1f, (float) amount / capacity);

        // Bulb interior bounds (matches BASE voxel shape: 4,0,4 -> 12,9,12 in /16ths),
        // inset slightly on every side so the fluid doesn't sit flush against the glass.
        float minX = 4f / 16f + WALL_INSET;
        float maxX = 12f / 16f - WALL_INSET;
        float minZ = 4f / 16f + WALL_INSET;
        float maxZ = 12f / 16f - WALL_INSET;
        float floorY = 1f / 16f + WALL_INSET;
        float ceilingY = 9f / 16f - WALL_INSET;

        float fluidTop = floorY + (ceilingY - floorY) * fillPercent;
        float fluidHeight = fluidTop - floorY;
        if (fluidHeight <= 0f) return;

        VertexConsumer buffer = bufferSource.getBuffer(RenderType.translucent());

        poseStack.pushPose();
        var pose = poseStack.last().pose();

        float u0 = sprite.getU0();
        float u1 = sprite.getU1();
        float v0 = sprite.getV0();
        float v1 = sprite.getV1();

        float fu0 = flowingSprite.getU0();
        float fu1 = flowingSprite.getU1();
        float fv0 = flowingSprite.getV0();
        float fv1 = flowingSprite.getV1();

        // Top face (looking down into the flask) - uses the STILL texture, rendered with both
// winding orders since RenderType.translucent() culls backfaces, and camera angle can
// flip which winding is visible.
        putQuad(buffer, pose, minX, fluidTop, minZ, maxX, fluidTop, minZ, maxX, fluidTop, maxZ, minX, fluidTop, maxZ,
                r, g, b, a, u0, v0, u1, v1, packedLight);
        putQuad(buffer, pose, minX, fluidTop, maxZ, maxX, fluidTop, maxZ, maxX, fluidTop, minZ, minX, fluidTop, minZ,
                r, g, b, a, u0, v0, u1, v1, packedLight);

// Bottom face (looking up into the flask from below) - uses the STILL texture, same as the
// top, also rendered with both winding orders to avoid backface culling from below.
        putQuad(buffer, pose, minX, floorY, minZ, minX, floorY, maxZ, maxX, floorY, maxZ, maxX, floorY, minZ,
                r, g, b, a, u0, v0, u1, v1, packedLight);
        putQuad(buffer, pose, maxX, floorY, minZ, maxX, floorY, maxZ, minX, floorY, maxZ, minX, floorY, minZ,
                r, g, b, a, u0, v0, u1, v1, packedLight);

        // Side faces: use the FLOWING texture, tiled in TILE_HEIGHT-sized chunks from the floor
        // up, like vanilla water. Full tiles use the entire sprite; the topmost partial tile
        // crops the texture from the BOTTOM of the sprite upward, so it looks like the liquid
        // is "filling into" a fixed-size tile rather than a stretched image.
        float y = floorY;
        while (y < fluidTop) {
            float segmentHeight = Math.min(TILE_HEIGHT, fluidTop - y);
            boolean isPartialTile = segmentHeight < TILE_HEIGHT - 1.0e-4f;

            float segV0;
            float segV1;
            if (isPartialTile) {
                // Crop from the bottom of the texture upward: the visible fraction of the tile
                // is (segmentHeight / TILE_HEIGHT), so we take that fraction of the V range
                // starting from fv1 (bottom) going toward fv0 (top).
                float fraction = segmentHeight / TILE_HEIGHT;
                segV0 = fv1 - (fv1 - fv0) * fraction;
                segV1 = fv1;
            } else {
                segV0 = fv0;
                segV1 = fv1;
            }

            float segBottom = y;
            float segTop = y + segmentHeight;

            putQuad(buffer, pose, minX, segBottom, minZ, minX, segTop, minZ, maxX, segTop, minZ, maxX, segBottom, minZ,
                    r, g, b, a, fu0, segV0, fu1, segV1, packedLight);
            putQuad(buffer, pose, maxX, segBottom, maxZ, maxX, segTop, maxZ, minX, segTop, maxZ, minX, segBottom, maxZ,
                    r, g, b, a, fu0, segV0, fu1, segV1, packedLight);
            putQuad(buffer, pose, maxX, segBottom, minZ, maxX, segTop, minZ, maxX, segTop, maxZ, maxX, segBottom, maxZ,
                    r, g, b, a, fu0, segV0, fu1, segV1, packedLight);
            putQuad(buffer, pose, minX, segBottom, maxZ, minX, segTop, maxZ, minX, segTop, minZ, minX, segBottom, minZ,
                    r, g, b, a, fu0, segV0, fu1, segV1, packedLight);

            y += segmentHeight;
        }

        poseStack.popPose();
    }

    private void putQuad(VertexConsumer buffer, org.joml.Matrix4f pose,
                         float x1, float y1, float z1, float x2, float y2, float z2,
                         float x3, float y3, float z3, float x4, float y4, float z4,
                         float r, float g, float b, float a,
                         float u0, float v0, float u1, float v1, int packedLight) {
        buffer.vertex(pose, x1, y1, z1).color(r, g, b, a).uv(u0, v0).overlayCoords(0, 10).uv2(packedLight).normal(0, 1, 0).endVertex();
        buffer.vertex(pose, x2, y2, z2).color(r, g, b, a).uv(u0, v1).overlayCoords(0, 10).uv2(packedLight).normal(0, 1, 0).endVertex();
        buffer.vertex(pose, x3, y3, z3).color(r, g, b, a).uv(u1, v1).overlayCoords(0, 10).uv2(packedLight).normal(0, 1, 0).endVertex();
        buffer.vertex(pose, x4, y4, z4).color(r, g, b, a).uv(u1, v0).overlayCoords(0, 10).uv2(packedLight).normal(0, 1, 0).endVertex();
    }
}