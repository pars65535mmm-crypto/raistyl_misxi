package com.tyamizumoti.raistyl_misxi.item;

import com.tyamizumoti.raistyl_misxi.effect.ModEffects;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class DisastrovaSwordItem extends SwordItem {
    public DisastrovaSwordItem(Properties properties) {
        super(Tiers.NETHERITE, 11, -2.1F, properties); 
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (!target.level().isClientSide() && attacker instanceof Player player) {
            // ⭐ 防御貫通（防具、魔法耐性を完全無視してダイレクトに削る）
            target.setHealth(target.getHealth() - 15.0F);
            target.hurt(target.damageSources().inWall(), 1.0F);

            // 🧪 5重のカスタムデバフを容赦なく同時付与！
            target.addEffect(new MobEffectInstance(MobEffects.POISON, 200, 1)); // 毒
            target.addEffect(new MobEffectInstance(ModEffects.SLEEP.get(), 100, 0)); // 眠り
            target.addEffect(new MobEffectInstance(ModEffects.BLOCKSOUL_FIRE.get(), 200, 1)); // 黒焔
            target.addEffect(new MobEffectInstance(ModEffects.BLEEDING.get(), 200, 1)); // 出血
            target.addEffect(new MobEffectInstance(ModEffects.CONFUSION.get(), 200, 0)); // 混乱

            if (target.level() instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypes.DRAGON_BREATH, target.getX(), target.getY() + 1.0, target.getZ(), 20, 0.3, 0.5, 0.3, 0.1);
            }
        }
        return super.hurtEnemy(stack, target, attacker);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);
        if (player.isShiftKeyDown()) {
            if (level.isClientSide()) {
                Vec3 look = player.getViewVector(1.0F);
                player.setDeltaMovement(look.x * 1.8, look.y * 1.8, look.z * 1.8);
            } else {
                player.startFallFlying();
            }
            return InteractionResultHolder.consume(itemstack);
        } else {
            if (!level.isClientSide()) {
                Vec3 look = player.getViewVector(1.0F);
                AreaEffectCloud breathCloud = new AreaEffectCloud(level, player.getX() + look.x * 2, player.getY() + 1.2, player.getZ() + look.z * 2);
                breathCloud.setOwner(player);
                breathCloud.setParticle(ParticleTypes.DRAGON_BREATH);
                breathCloud.setRadius(3.0F);
                breathCloud.setDuration(100);
                breathCloud.addEffect(new MobEffectInstance(MobEffects.HARM, 1, 1));
                level.addFreshEntity(breathCloud);
            }
            return InteractionResultHolder.success(itemstack);
        }
    }

    // 🎨 ツールチップをパープル＆ピンクのリアルタイムグラデーションにするハック！
    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        long time = System.currentTimeMillis() / 100;

        String description = "ドラゴンブレスと刃が合一せし終焉の災厄。触れし者の命を五重の呪縛が蝕む。";
        
        // 1文字ずつ色を変えてグラデーションComponentを組み立てる
        Component gradientText = Component.empty();
        for (int i = 0; i < description.length(); i++) {
            final int index = i;
            String letter = String.valueOf(description.charAt(i));
            gradientText = Component.literal(gradientText.getString() + letter).withStyle(style -> 
                style.withColor(TextColor.fromRgb(getPurplePinkRainbow(time + index)))
            );
        }
        tooltip.add(gradientText);
    }

    // 竜剣ディザストロワ専用：ピンク〜パープルに揺れ動くカラーチェンジャー
    private int getPurplePinkRainbow(long tick) {
        int r = (int) (Math.sin(0.1 * tick + 0) * 40 + 215); // 175〜255 (赤多め)
        int g = (int) (Math.sin(0.1 * tick + 2) * 30 + 50);  // 20〜80 (緑抑えめ)
        int b = (int) (Math.sin(0.1 * tick + 4) * 50 + 205); // 155〜255 (青多め)
        return (r << 16) | (g << 8) | b;
    }
}