package com.tyamizumoti.raistyl_misxi; // パッケージ名をあなたの環境（同一階層）に修正しました

import com.mojang.blaze3d.vertex.PoseStack;
import com.tyamizumoti.raistyl_misxi.item.CrashWeaponItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Random;

@Mod.EventBusSubscriber(modid = "raistyl_misxi", value = Dist.CLIENT)
public class ClientRenderEvents {
    private static final Random RANDOM = new Random();

    // 🎒 インベントリ（または任意の画面）が描画される直前に画面をブレさせる
    @SubscribeEvent
    public static void onScreenRenderPre(ScreenEvent.Render.Pre event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        // 画面が「インベントリ画面」である時だけ発動させたい場合のチェック
        if (!(event.getScreen() instanceof InventoryScreen)) return;

        // インベントリに対象のアイテムがあるか確認
        boolean hasWeapon = false;
        for (int i = 0; i < mc.player.getInventory().getContainerSize(); i++) {
            if (mc.player.getInventory().getItem(i).getItem() instanceof CrashWeaponItem) {
                hasWeapon = true;
                break;
            }
        }

        // アイテムを持っていたら、描画マトリクス（PoseStack）の位置をランダムにガタガタ震わせる
        if (hasWeapon) {
            PoseStack poseStack = event.getGuiGraphics().pose(); // 1.20.1ではGuiGraphicsからPoseStackを取得します
            double offsetX = (RANDOM.nextDouble() - 0.5) * 2.4;
            double offsetY = (RANDOM.nextDouble() - 0.5) * 2.4;
            
            poseStack.pushPose(); // 現在の描画状態を記憶
            poseStack.translate(offsetX, offsetY, 0); // 座標をズラす
        }
    }

    // 🎒 描画が終わった後にズラしたマトリクスを元に戻す
    @SubscribeEvent
    public static void onScreenRenderPost(ScreenEvent.Render.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        if (!(event.getScreen() instanceof InventoryScreen)) return;

        boolean hasWeapon = false;
        for (int i = 0; i < mc.player.getInventory().getContainerSize(); i++) {
            if (mc.player.getInventory().getItem(i).getItem() instanceof CrashWeaponItem) {
                hasWeapon = true;
                break;
            }
        }
        
        if (hasWeapon) {
            event.getGuiGraphics().pose().popPose(); // 記憶した元の位置に戻す
        }
    }
}
