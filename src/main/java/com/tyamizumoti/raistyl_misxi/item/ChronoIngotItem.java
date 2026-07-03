package com.tyamizumoti.raistyl_misxi.item;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class ChronoIngotItem extends Item {
    public ChronoIngotItem(Properties pProperties) {
        super(pProperties);
    }

    // 🌟 完全版クロノインゴットの名前を「宇宙・時空グラデーション」で輝かせる
    @Override
    public Component getName(ItemStack pStack) {
        String text = "クロノインゴット";
        MutableComponent component = Component.literal("");

        // 現在のシステム時間（ミリ秒）を取得してアニメーションの基準にする
        long time = System.currentTimeMillis();

        for (int i = 0; i < text.length(); i++) {
            // 文字ごとに時間をずらして、右から左へ流れる滑らかなウネウネ感を作る
            double phase = (time / 250.0) - (i * 0.5); 
            
            // 🌌 時空を超越した「純白 ➔ 水色 ➔ 神聖な青 ➔ 宇宙の紫」へ変化する神秘的なRGB計算
            int r = (int) (Math.sin(phase) * 60 + 150);       // 90 〜 210
            int g = (int) (Math.sin(phase + 2) * 55 + 180);   // 125 〜 235
            int b = (int) (Math.sin(phase + 4) * 30 + 225);   // 195 〜 255
            
            // RGB値を24bitカラーに結合（0〜255の範囲に安全に収める）
            int rgb = (Math.max(0, Math.min(255, r)) << 16) | 
                      (Math.max(0, Math.min(255, g)) << 8) | 
                      Math.max(0, Math.min(255, b));

            // 1文字ずつ色を適用して、太字（Bold）で結合！
            component.append(Component.literal(String.valueOf(text.charAt(i)))
                .withStyle(Style.EMPTY.withColor(TextColor.fromRgb(rgb)).withBold(true)));
        }

        return component;
    }
}
