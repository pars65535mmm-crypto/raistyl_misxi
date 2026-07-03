package com.tyamizumoti.raistyl_misxi;

import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class AnnihilationPastDagger extends Item {
    public AnnihilationPastDagger(Properties properties) {
        super(properties);
    }

    /**
     * 👁️ 概念抹消・左クリック通常攻撃
     */
    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (!attacker.level().isClientSide() && attacker instanceof Player player) {
            this.executeAnnihilation(target, player);
        }
        return true;
    }

    /**
     * 🌌 【空間干渉】右クリック能力分岐
     * ・通常右クリック：視線の先（単体）の無敵をパージして消滅
     * ・Shift＋右クリック：周囲20ブロックの敵全員に「カスタム正の無限大ダメージ」
     */
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);
        
        if (!level.isClientSide()) {
            // 🔥 【Shift（スニーク）＋右クリックのとき：カスタム無限大ダメージ】
            if (player.isShiftKeyDown()) {
                player.sendSystemMessage(Component.literal("§c[過去の呪縛] §4§l特級カスタムダメージ・広域強制収束シーケンスを発動。"));

                // 1. 1.20.1のレジストリから密造した「annihilation_infinity」のキーを安全に取得
                net.minecraft.resources.ResourceLocation damageTypeLocation = net.minecraft.resources.ResourceLocation.tryBuild("raistyl_misxi", "annihilation_infinity");
                
                net.minecraft.core.Holder<net.minecraft.world.damagesource.DamageType> damageTypeHolder = 
                    level.registryAccess().registryOrThrow(net.minecraft.core.registries.Registries.DAMAGE_TYPE)
                    .getHolder(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.DAMAGE_TYPE, damageTypeLocation))
                    .orElseThrow();

                // 2. カスタムダメージソースを生成
                net.minecraft.world.damagesource.DamageSource customInfinityDamage = new net.minecraft.world.damagesource.DamageSource(damageTypeHolder, player);

                // 周囲20ブロックのMobをスキャン
                double radius = 20.0D;
                List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(radius));

                int hurtCount = 0;
                for (LivingEntity target : targets) {
                    if (target != player) {
                        // 👁️ 相手が「無敵タイマー（Invul）」を持ってたら事前に0にして被弾を強制確定させる
                        try {
                            net.minecraft.nbt.CompoundTag tag = new net.minecraft.nbt.CompoundTag();
                            target.saveWithoutId(tag);
                            if (tag.contains("Invul")) {
                                tag.putInt("Invul", 0);
                                target.load(tag);
                            }
                        } catch (Exception e) {}

                        // 💥 「痛みを……与えたいんだよ……！！」
                        // 防具・耐性・クリエ無敵を全無視する正の無限大ダメージをダイレクトに叩き込む！
                        target.hurt(customInfinityDamage, Float.POSITIVE_INFINITY);
                        hurtCount++;
                    }
                }
                
                player.sendSystemMessage(Component.literal("§c[過去の呪縛] §7周囲の " + hurtCount + " 体の肉体に無限の苦痛を刻みました。"));

                if (level instanceof net.minecraft.server.level.ServerLevel serverLevel) {
                    serverLevel.sendParticles(net.minecraft.core.particles.ParticleTypes.FLASH, player.getX(), player.getY() + 1.0, player.getZ(), 50, 3.0, 1.0, 3.0, 0.1);
                }

            } else {
                // 👁️ 【通常の右クリックのとき（単体空間スキャン消滅）】
                net.minecraft.world.phys.HitResult hitResult = net.minecraft.world.entity.projectile.ProjectileUtil.getHitResultOnViewVector(
                    player, entity -> !entity.isSpectator(), 20.0D
                );

                if (hitResult instanceof net.minecraft.world.phys.EntityHitResult entityHit && entityHit.getEntity() instanceof LivingEntity target) {
                    player.sendSystemMessage(Component.literal("§c[概念抹消] §4空間スキャンにより対象の不滅データを捕捉。直接パージします。"));
                    this.executeAnnihilation(target, player);
                }
            }
        }
        
        return InteractionResultHolder.sidedSuccess(itemstack, level.isClientSide());
    }

    /**
     * 💀 実際の抹消ロジック（通常攻撃＆単体スキャン用）
     */
    private void executeAnnihilation(LivingEntity target, Player player) {
        player.sendSystemMessage(Component.literal("§c[概念抹消] §7対象の履歴抹消シーケンスを開始: " + target.getName().getString()));

        target.setInvulnerable(false);

        try {
            net.minecraft.nbt.CompoundTag tag = new net.minecraft.nbt.CompoundTag();
            target.saveWithoutId(tag);
            if (tag.contains("Invul")) {
                tag.putInt("Invul", 0);
                target.load(tag);
            }
        } catch (Exception e) {}

        target.setHealth(0.0F);
        target.invulnerableTime = 0;
        target.die(player.damageSources().genericKill());
        target.discard();
        target.setRemoved(Entity.RemovalReason.KILLED);

        if (player.level() instanceof net.minecraft.server.level.ServerLevel serverLevel) {
            serverLevel.sendParticles(net.minecraft.core.particles.ParticleTypes.DRAGON_BREATH, target.getX(), target.getY() + 1.0, target.getZ(), 20, 0.5, 0.5, 0.5, 0.1);
            serverLevel.sendParticles(net.minecraft.core.particles.ParticleTypes.END_ROD, target.getX(), target.getY() + 1.0, target.getZ(), 20, 0.5, 0.5, 0.5, 0.05);
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("§7「その者が存在したという過去を、最初から白紙に戻す。」"));
        net.minecraft.network.chat.MutableComponent warningText = Component.literal("");
        warningText.append(Component.literal("概").withStyle(style -> style.withColor(0xFF0000).withObfuscated(true).withBold(true)));
        warningText.append(Component.literal("念").withStyle(style -> style.withColor(0xCC0000).withObfuscated(true).withBold(true)));
        warningText.append(Component.literal("抹").withStyle(style -> style.withColor(0x990000).withObfuscated(true).withBold(true)));
        warningText.append(Component.literal("消").withStyle(style -> style.withColor(0x660000).withObfuscated(true).withBold(true)));
        warningText.append(Component.literal("対策システム搭載（Shift+右クリックで広域無限痛覚）").withStyle(style -> style.withColor(0xAA0000).withBold(true)));
        tooltip.add(warningText);
        super.appendHoverText(stack, level, tooltip, flag);
    }
}