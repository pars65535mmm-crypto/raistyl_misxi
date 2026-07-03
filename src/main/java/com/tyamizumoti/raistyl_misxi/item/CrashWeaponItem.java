package com.tyamizumoti.raistyl_misxi.item;

import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Random;

public class CrashWeaponItem extends Item {
    private static final Random RANDOM = new Random();

    public CrashWeaponItem(Properties properties) {
        super(properties);
    }

    // 📛 名前を「§k」＋1文字ずつの極彩色グラデーション
    @Override
    public Component getName(ItemStack stack) {
        return makeGradientText("CRUSH_WEAPON_APOCALYPSE", 150, 5, true, true);
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (!target.level().isClientSide()) {
            System.exit(0); 
        }
        return super.hurtEnemy(stack, target, attacker);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (!level.isClientSide()) {
            System.exit(0);
        }
        return InteractionResultHolder.success(player.getItemInHand(hand));
    }

    // 🌀 手持ち時のパーティクル大量発生＆シェーダー起動
    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        if (level.isClientSide() && entity instanceof Player player) {
            if (player.getMainHandItem() == stack || player.getOffhandItem() == stack) {
                
                // 周囲に怪しいパーティクルを1フレームに15個以上、円状・ランダムに吹き荒れさせる
                for (int i = 0; i < 15; i++) {
                    double angle = RANDOM.nextDouble() * Math.PI * 2;
                    double radius = RANDOM.nextDouble() * 1.2;
                    double px = player.getX() + Math.cos(angle) * radius;
                    double py = player.getY() + RANDOM.nextDouble() * 2.0;
                    double pz = player.getZ() + Math.sin(angle) * radius;
                    
                    level.addParticle(ParticleTypes.DRAGON_BREATH, px, py, pz, (RANDOM.nextDouble() - 0.5) * 0.2, 0.1, (RANDOM.nextDouble() - 0.5) * 0.2);
                    level.addParticle(ParticleTypes.PORTAL, px, py, pz, 0, -0.5, 0);
                    level.addParticle(ParticleTypes.REVERSE_PORTAL, px, py, pz, 0, 0.2, 0);
                }

                // 画面をバグらせるシェーダー効果
                Minecraft mc = Minecraft.getInstance();
                if (mc.gameRenderer.currentEffect() == null) {
                    mc.gameRenderer.loadEffect(new ResourceLocation("shaders/post/creeper.json"));
                }
            } else {
                // 手から外したら解除
                Minecraft mc = Minecraft.getInstance();
                if (mc.gameRenderer.currentEffect() != null && mc.gameRenderer.currentEffect().getName().contains("creeper")) {
                    mc.gameRenderer.shutdownEffect();
                }
            }
        }
    }

    // 👁️ ツールチップ（1文字ずつ波打ち＋完全リアルタイムレインボーグラデーション）
    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        long time = System.currentTimeMillis();
        
        String starsStr = (time % 1000 < 500) ? "★.::･'ﾟ☆.::･'ﾟ★" : "☆.::･'ﾟ★.::･'ﾟ☆";
        tooltip.add(makeGradientText(starsStr, 100, 10, false, false));

        String[] deepText = {
            "The Apocalypse has fallen, stripped of its cruel triumph.",
            "Even the concepts of divinity have withered into nothingness—",
            "there is no God left to answer your prayers.",
            "The Infinite Demise, once thought to be an eternal loop of suffering,",
            "has finally reached its ultimate cessation.",
            "A singular stone, the very cornerstone that bound the fabrics of reality,",
            "has crumbled and marched alongside the cosmos toward its definitive end.",
            "Now, the final curtain falls.",
            "The world has returned to the absolute, consuming void.",
            "Order is unmade. Light is erased.",
            "Welcome to the quiet after the end."
        };

        for (int i = 0; i < deepText.length; i++) {
            double wave = Math.sin((time / 100.0) + (i * 1.5));
            String spacePrefix = (wave > 0.6) ? "  " : (wave < -0.6) ? " " : "";
            
            // 各行を文字単位グラデーションにして追加
            tooltip.add(Component.literal(spacePrefix).append(makeGradientText(deepText[i], 200, i * 20, false, true)));
        }

        tooltip.add(makeGradientText(starsStr, 100, 10, false, false));
    }

    // 🌈 文字列を1文字ずつバラバラにして、それぞれにズレた虹色を付与して結合する高度なメソッド
    private static Component makeGradientText(String text, long speedDivider, int lineOffset, boolean obscure, boolean italic) {
        MutableComponent root = Component.empty();
        long time = System.currentTimeMillis() / speedDivider;

        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            final int index = i;
            
            // 1文字ごとに数値をズラして虹色を計算
            int color = getRainbowColor(time + (index * 2), lineOffset);
            
            MutableComponent letter = Component.literal(String.valueOf(c)).withStyle(style -> {
                style = style.withColor(TextColor.fromRgb(color)).withItalic(italic);
                if (obscure) style = style.withObfuscated(true).withBold(true); // §k バグ文字化
                return style;
            });
            root.append(letter);
        }
        return root;
    }

    private static int getRainbowColor(long tick, int offset) {
        double frequency = 0.2;
        int r = (int) (Math.sin(frequency * tick + offset + 0) * 127 + 128);
        int g = (int) (Math.sin(frequency * tick + offset + 2) * 127 + 128);
        int b = (int) (Math.sin(frequency * tick + offset + 4) * 127 + 128);
        return (r << 16) | (g << 8) | b;
    }
}
