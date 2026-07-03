package com.tyamizumoti.raistyl_misxi.item;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class HaloItem extends Item {
    private final String tooltipText;

    public HaloItem(Properties properties, String tooltipText) {
        super(properties);
        this.tooltipText = tooltipText;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        if (tooltipText == null || tooltipText.isEmpty()) return;

        MutableComponent gradientComponent = Component.empty();
        long time = System.currentTimeMillis() / 50; // 時間経過で色を滑らかに変化させる

        // 文字列を1文字ずつバラして、それぞれに微妙に違う色（グラデーション）を当てる
        for (int i = 0; i < tooltipText.length(); i++) {
            char c = tooltipText.charAt(i);
            
            // サイン波を使って、白(#FFFFFF)から薄いグレーや淡い光色(#E0E8FF)へ変化させるグラデーション計算
            int phase = (int) (Math.sin((time + i * 2) * 0.1) * 30);
            int r = 225 + phase; // 225〜255の間で変化（白ベース）
            int g = 235 + phase;
            int b = 255;         // 青みを少し強めて神秘的な白に

            int colorRgb = (r << 16) | (g << 8) | b;
            
            MutableComponent charComponent = Component.literal(String.valueOf(c))
                    .withStyle(Style.EMPTY.withColor(TextColor.fromRgb(colorRgb)));
            
            gradientComponent.append(charComponent);
        }

        tooltip.add(gradientComponent);
    }
}