package com.tyamizumoti.raistyl_misxi.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class ChronoItemRenderer extends BlockEntityWithoutLevelRenderer {
    private static ChronoItemRenderer INSTANCE;

    public ChronoItemRenderer() {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
    }

    public static ChronoItemRenderer getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ChronoItemRenderer();
        }
        return INSTANCE;
    }

    // 🎯 ここが毎フレーム呼ばれる描画の心臓部！
    @Override
    public void renderByItem(ItemStack pStack, ItemDisplayContext pDisplayContext, PoseStack pPoseStack, MultiBufferSource pBufferSource, int pCombinedLight, int pCombinedOverlay) {
        ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
        // アイテムの元の3Dモデル（または2Dドット絵のモデル）を取得
        BakedModel bakedModel = itemRenderer.getModel(pStack, null, null, 0);

        pPoseStack.pushPose();
        // 通常のベースとなるアイテムの見た目をまず描画
        itemRenderer.render(pStack, pDisplayContext, false, pPoseStack, pBufferSource, pCombinedLight, pCombinedOverlay, bakedModel);

        // 🌟【条件分岐】NBT（Heated）が付いている「朽ちた」状態の時だけ、追加で発光エフェクトを重ねる
        if (pStack.hasTag() && pStack.getTag().getBoolean("Heated")) {
            long time = System.currentTimeMillis();
            
            // 時間を元に、ボワァッと明滅するサイン波を作る（0.2 〜 0.8 の間をゆっくりループ）
            float alphaPulse = (float) (Math.sin(time / 250.0) * 0.3 + 0.5);
            
            // マイクラが標準で持っている「エンチャントのキラキラ（Glint）」の特殊なブレンド描画レイヤーを呼び出す
            // RenderType.glint() や、より強く光らせるための「armorGlint」などを利用可能
            VertexConsumer vertexConsumer = pBufferSource.getBuffer(RenderType.glintDirect());

            // 🎨 OpenGLのバッファに対して、計算した明滅カラー（紫〜ピンク）を毎フレーム流し込む
            // 引数: (モデル, 赤, 緑, 青, 透明度, 光量, オーバーレイ)
            // 通常のrender型に、色とアルファ値を掛け合わせて「発光しているようなブレンド」を擬似的に作ります
            int r = (int) (200 * alphaPulse);
            int g = (int) (50 * alphaPulse);
            int b = (int) (255 * alphaPulse);

            // 元のモデルの形状のまま、計算した発光カラーレイヤーを上から1枚ガツンと重ねてブレンド描画する！
            itemRenderer.renderModelLists(bakedModel, pStack, pCombinedLight, pCombinedOverlay, pPoseStack, vertexConsumer);
        }

        pPoseStack.popPose();
    }
}
