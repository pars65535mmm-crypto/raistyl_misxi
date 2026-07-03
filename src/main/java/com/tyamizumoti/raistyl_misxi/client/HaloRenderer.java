package com.tyamizumoti.raistyl_misxi.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.client.ICurioRenderer;

public class HaloRenderer implements ICurioRenderer {

    @Override
    public <T extends LivingEntity, M extends EntityModel<T>> void render(
            ItemStack stack,
            SlotContext slotContext,
            PoseStack poseStack,
            RenderLayerParent<T, M> renderLayerParent,
            MultiBufferSource renderTypeBuffer,
            int light,
            float limbSwing,
            float limbSwingAmount,
            float partialTicks,
            float ageInTicks,
            float netHeadYaw,
            float headPitch) {

        LivingEntity entity = slotContext.entity();
        
        poseStack.pushPose();

        // 1. 首の動きに同期させる
        if (renderLayerParent.getModel() instanceof net.minecraft.client.model.HumanoidModel<?> humanoidModel) {
            ICurioRenderer.followHeadRotations(entity, humanoidModel.head);
        }
        
        // スニーク補正
        ICurioRenderer.translateIfSneaking(poseStack, entity);
        ICurioRenderer.rotateIfSneaking(poseStack, entity);

        // 2. 位置の微調整（頭の中心の、ちょっと上に浮かせる）
        poseStack.translate(0.0D, -1.0D, 0.0D);

        // 3. スケール（大きさ）の調整
        poseStack.scale(0.9F, 0.9F, 0.9F);

        // 4. テクスチャを水平に寝かせる
        poseStack.mulPose(Axis.XP.rotationDegrees(90F));
        poseStack.mulPose(Axis.ZP.rotationDegrees(180F));

        // 5. 【追加】ゆっくり自動回転させる（引数の ageInTicks を使用して滑らかに）
        // 1.0F を変更すると回転速度が変わります
        float rotationAngle = ageInTicks * 1.0F; 
        poseStack.mulPose(Axis.ZP.rotationDegrees(rotationAngle));

        // 6. 【変更】フワフワ上下アニメーションの強化
        // * 0.05F で揺れる周期（速さ）、* 0.06F で揺れる幅（高さ）を調整しています
        float bobbing = (float) Math.sin(ageInTicks * 0.05F) * 0.06F;
        // 水平に寝かせた（XP.90度）後のため、Z軸方向への移動が世界（頭上）の上下移動になります
        poseStack.translate(0.0D, 0.0D, bobbing);

        // 7. 描画
        Minecraft.getInstance().getItemRenderer().renderStatic(
                stack,
                ItemDisplayContext.NONE, 
                light,
                OverlayTexture.NO_OVERLAY,
                poseStack,
                renderTypeBuffer,
                entity.level(),
                entity.getId()
        );

        poseStack.popPose();
    }
}
