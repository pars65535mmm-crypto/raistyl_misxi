package com.tyamizumoti.raistyl_misxi.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.tyamizumoti.raistyl_misxi.MasterOutputBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;

public class MasterOutputCoreRenderer implements BlockEntityRenderer<MasterOutputBlockEntity> {

    public MasterOutputCoreRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    public void render(MasterOutputBlockEntity entity, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int light, int overlay) {

        ItemStack stack = entity.inventory.getStackInSlot(0);
        if (stack.isEmpty()) return;

        Level level = entity.getLevel();
        if (level == null) return;

        poseStack.pushPose();

        poseStack.translate(0.5, 0.8, 0.5);

        double floatOffset = Math.sin((level.getGameTime() + partialTick) * 0.03) * 0.1 + 0.15;
        poseStack.translate(0, floatOffset, 0);

        float rotation = (level.getGameTime() + partialTick) * 1.2f;
        poseStack.mulPose(Axis.YP.rotationDegrees(rotation));

        poseStack.scale(0.8f, 0.8f, 0.8f);

        ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
        int combinedLight = getLightLevel(level, entity.getBlockPos());
        itemRenderer.renderStatic(
            stack,
            ItemDisplayContext.FIXED,
            combinedLight,
            OverlayTexture.NO_OVERLAY,
            poseStack,
            bufferSource,
            level,
            0
        );

        poseStack.popPose();
    }

    private int getLightLevel(Level level, BlockPos pos) {
        int blockLight = level.getBrightness(LightLayer.BLOCK, pos);
        int skyLight = level.getBrightness(LightLayer.SKY, pos);
        return LightTexture.pack(blockLight, skyLight);
    }
}