package com.tyamizumoti.raistyl_misxi;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Rpg936Rmv1 extends Item {
    private static final ScheduledExecutorService SCHEDULER = Executors.newScheduledThreadPool(4);

    public Rpg936Rmv1(Properties properties) {
        super(properties);
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BOW;
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 72000;
    }

    // 🏋️‍♂️ 13Kgの重量ペナルティ ＆ 白乃崩壊時の専用オーラエフェクト
    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean isSelected) {
        if (entity instanceof Player player && (player.getMainHandItem() == stack || player.getOffhandItem() == stack)) {
            if (!level.isClientSide()) {
                player.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                    net.minecraft.world.effect.MobEffects.MOVEMENT_SLOWDOWN, 5, 1, false, false, true
                ));
            }
            
            if (isWhiteCollapse(stack) && level.isClientSide()) {
                double age = player.tickCount * 0.2;
                double px = player.getX() + Math.cos(age) * 0.8;
                double py = player.getY() + 1.2 + Math.sin(age * 0.5) * 0.2;
                double pz = player.getZ() + Math.sin(age) * 0.8;
                level.addParticle(net.minecraft.core.particles.ParticleTypes.END_ROD, px, py, pz, 0, 0, 0);
            }
        }
    }

    // 📝 ツールチップ（説明欄）の動的変化
    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        if (isWhiteCollapse(stack)) {
            tooltip.add(Component.translatable("tooltip.raistyl_misxi.white_collapse"));
            tooltip.add(Component.translatable("tooltip.raistyl_misxi.white_collapse_desc"));
        }
        super.appendHoverText(stack, level, tooltip, flag);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);
        player.startUsingItem(hand);
        return InteractionResultHolder.consume(itemstack);
    }

    // 🔥 リアルタイムチャージ（白乃崩壊時は時間が5倍かかる ＝ 100ticks必要）
    @Override
    public void onUseTick(Level level, LivingEntity entity, ItemStack stack, int count) {
        if (!level.isClientSide() && entity instanceof Player player) {
            int chargeTime = this.getUseDuration(stack) - count;
            
            boolean isWhite = isWhiteCollapse(stack);
            boolean isDualWield = player.getMainHandItem().getItem() instanceof Rpg936Rmv1 
                               && player.getOffhandItem().getItem() instanceof Rpg936Rmv1;
            
            int baseRequired = isWhite ? 100 : 20;
            int maxCharge = isDualWield ? baseRequired * 2 : baseRequired;
            int percent = Math.min(isDualWield ? 200 : 100, (chargeTime * 100) / baseRequired);

            // 【白乃崩壊限定】周辺Mob吸い込みロジック
            if (isWhite && chargeTime % 5 == 0) {
                AABB pullArea = player.getBoundingBox().inflate(15.0D);
                List<Entity> targets = level.getEntities(player, pullArea, e -> e instanceof LivingEntity);
                for (Entity target : targets) {
                    Vec3 pullDir = player.position().subtract(target.position()).normalize().scale(0.3D);
                    target.setDeltaMovement(target.getDeltaMovement().add(pullDir));
                    target.hurtMarked = true;
                }
            }

            int barProgress = Math.min(20, (chargeTime * 20) / maxCharge);
            StringBuilder bar = new StringBuilder();
            for (int i = 0; i < 20; i++) {
                bar.append(i < barProgress ? "█" : "░");
            }

            String color = "§b";
            if (percent >= 200) {
                color = isWhite ? "§5§l[白乃崩壊・両眼解放] §d§l" : "§d§l[量子限界突破] §5§l";
            } else if (percent >= 100) {
                color = isWhite ? "§f§l[白乃光輪・100%出力] §e§l" : "§a§l[通常充填完了] §2§l";
            }

            String name = isWhite ? "RPG936-白乃崩壊" : "RPG936";
            player.displayClientMessage(Component.literal("§b[" + name + "] " + color + bar.toString() + " " + percent + "%"), true);
        }
    }

    // 💥 発射シーケンス（全4形態完全分岐）
    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeLeft) {
        if (entity instanceof Player player) {
            int chargeTime = this.getUseDuration(stack) - timeLeft;
            boolean isWhite = isWhiteCollapse(stack);
            boolean isDualWield = player.getMainHandItem().getItem() instanceof Rpg936Rmv1 
                               && player.getOffhandItem().getItem() instanceof Rpg936Rmv1;

            // 🌌 形態①：【白乃崩壊・二挺持ち200%】ブラックホール・岩盤消滅
            if (isWhite && isDualWield && chargeTime >= 200) {
                if (!level.isClientSide()) {
                    player.displayClientMessage(Component.literal("§f§l[白乃崩壊] §c§l全ては漆黒へ。事象の地平線を開放します。"), true);

                    HitResult hitResult = player.pick(16000.0D, 0.0F, false);
                    final double tx = hitResult.getLocation().x;
                    final double ty = hitResult.getLocation().y;
                    final double tz = hitResult.getLocation().z;

                    if (level instanceof ServerLevel serverLevel) {
                        renderHyperLaser(player, tx, ty, tz, serverLevel, true);

                        for (int i = 0; i < 15; i++) {
                            final int index = i;
                            SCHEDULER.schedule(() -> {
                                serverLevel.getServer().execute(() -> {
                                    double radius = index * 3.0;
                                    double angle = index * 0.5;
                                    double offsetX = Math.cos(angle) * radius;
                                    double offsetZ = Math.sin(angle) * radius;
                                    
                                    BlockPos centerPos = new BlockPos((int)(tx + offsetX), (int)ty, (int)(tz + offsetZ));
                                    
                                    // ブラックホールによる岩盤消滅
                                    for (BlockPos p : BlockPos.betweenClosed(centerPos.offset(-4, -4, -4), centerPos.offset(4, 4, 4))) {
                                        if (serverLevel.getBlockState(p).getBlock() == Blocks.BEDROCK || serverLevel.getBlockState(p).getDestroySpeed(serverLevel, p) >= 0) {
                                            serverLevel.setBlock(p, Blocks.AIR.defaultBlockState(), 3);
                                        }
                                    }
                                    
                                    serverLevel.sendParticles(net.minecraft.core.particles.ParticleTypes.SONIC_BOOM, tx + offsetX, ty, tz + offsetZ, 10, 1.0, 1.0, 1.0, 0.0);
                                    serverLevel.explode(player, tx + offsetX, ty, tz + offsetZ, 50.0F, Level.ExplosionInteraction.BLOCK);
                                });
                            }, i * 150, TimeUnit.MILLISECONDS);
                        }
                    }
                }
            }
            // ⚪ 形態②：【白乃崩壊・片手100%】威力25倍レーザー爆発
            else if (isWhite && chargeTime >= 100) {
                if (!level.isClientSide()) {
                    player.displayClientMessage(Component.literal("§f§l[白乃崩壊] §7光輪限界駆動。"), true);

                    HitResult hitResult = player.pick(6400.0D, 0.0F, false);
                    double tx = hitResult.getLocation().x;
                    double ty = hitResult.getLocation().y;
                    double tz = hitResult.getLocation().z;

                    if (level instanceof ServerLevel serverLevel) {
                        renderHyperLaser(player, tx, ty, tz, serverLevel, false);
                        level.explode(player, tx, ty, tz, 250.0F, Level.ExplosionInteraction.TNT);
                    }
                }
            }
            // 💥 形態③：【通常形態・二挺持ち200%】ボコ箱箱ボコ連続時差爆発
            else if (!isWhite && isDualWield && chargeTime >= 40) {
                if (!level.isClientSide()) {
                    player.displayClientMessage(Component.literal("§d§l[RPG936] §5§l両眼解放：時空歪曲・量子崩壊シーケンスを執行。"), true);

                    HitResult hitResult = player.pick(128.0D, 0.0F, false);
                    final double tx = hitResult.getLocation().x;
                    final double ty = hitResult.getLocation().y;
                    final double tz = hitResult.getLocation().z;

                    if (level instanceof ServerLevel serverLevel) {
                        renderLaser(player, tx, ty, tz, serverLevel, true);

                        for (int i = 0; i < 5; i++) {
                            final int index = i;
                            SCHEDULER.schedule(() -> {
                                serverLevel.getServer().execute(() -> {
                                    if (serverLevel.isLoaded(new net.minecraft.core.BlockPos((int)tx, (int)ty, (int)tz))) {
                                        double offsetX = (Math.random() - 0.5) * 4.0 * index;
                                        double offsetY = (Math.random() - 0.5) * 2.0;
                                        double offsetZ = (Math.random() - 0.5) * 4.0 * index;
                                        
                                        serverLevel.sendParticles(net.minecraft.core.particles.ParticleTypes.DRAGON_BREATH, tx + offsetX, ty + offsetY, tz + offsetZ, 30, 1.0, 1.0, 1.0, 0.2);
                                        serverLevel.explode(player, tx + offsetX, ty + offsetY, tz + offsetZ, 8.0F, Level.ExplosionInteraction.TNT);
                                    }
                                });
                            }, i * 250, TimeUnit.MILLISECONDS);
                        }
                    }
                }
            } 
            // 💥 形態④：【通常形態・片手100%】単発大爆発
            else if (!isWhite && chargeTime >= 20) {
                if (!level.isClientSide()) {
                    player.displayClientMessage(Component.literal("§b[RPG936] §c§lシングルバレル射撃。"), true);

                    HitResult hitResult = player.pick(128.0D, 0.0F, false);
                    double tx = hitResult.getLocation().x;
                    double ty = hitResult.getLocation().y;
                    double tz = hitResult.getLocation().z;

                    if (level instanceof ServerLevel serverLevel) {
                        renderLaser(player, tx, ty, tz, serverLevel, false);
                        level.explode(player, tx, ty, tz, 10.0F, Level.ExplosionInteraction.TNT);
                    }
                }
            } else {
                if (!level.isClientSide()) {
                    player.displayClientMessage(Component.literal("§b[RPG936] §cチャージ不足。出力エラー。"), true);
                }
            }
        }
    }

    // NBTデータから白乃光輪判定
    public static boolean isWhiteCollapse(ItemStack stack) {
        return stack.hasTag() && stack.getTag().getBoolean("IsWhiteCollapse");
    }

    // 通常時のレーザー演出
    private void renderLaser(Player player, double tx, double ty, double tz, ServerLevel serverLevel, boolean isQuantum) {
        double startX = player.getX();
        double startY = player.getEyeY() - 0.2;
        double startZ = player.getZ();
        double dx = tx - startX;
        double dy = ty - startY;
        double dz = tz - startZ;
        double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);

        for (double d = 0; d < distance; d += 0.5) {
            double px = startX + (dx / distance) * d;
            double py = startY + (dy / distance) * d;
            double pz = startZ + (dz / distance) * d;
            if (isQuantum) {
                serverLevel.sendParticles(net.minecraft.core.particles.ParticleTypes.PORTAL, px, py, pz, 2, 0.1, 0.1, 0.1, 0.0);
                serverLevel.sendParticles(net.minecraft.core.particles.ParticleTypes.WITCH, px, py, pz, 1, 0, 0, 0, 0);
            } else {
                serverLevel.sendParticles(net.minecraft.core.particles.ParticleTypes.END_ROD, px, py, pz, 1, 0, 0, 0, 0);
                serverLevel.sendParticles(net.minecraft.core.particles.ParticleTypes.SONIC_BOOM, px, py, pz, 1, 0, 0, 0, 0);
            }
        }
    }

    // 白乃崩壊時の超巨大レーザー演出
    private void renderHyperLaser(Player player, double tx, double ty, double tz, ServerLevel serverLevel, boolean isBlackHole) {
        double startX = player.getX();
        double startY = player.getEyeY() - 0.2;
        double startZ = player.getZ();
        double dx = tx - startX;
        double dy = ty - startY;
        double dz = tz - startZ;
        double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);

        for (double d = 0; d < distance; d += 1.0) {
            double px = startX + (dx / distance) * d;
            double py = startY + (dy / distance) * d;
            double pz = startZ + (dz / distance) * d;
            if (isBlackHole) {
                serverLevel.sendParticles(net.minecraft.core.particles.ParticleTypes.SQUID_INK, px, py, pz, 15, 0.5, 0.5, 0.5, 0.0);
                serverLevel.sendParticles(net.minecraft.core.particles.ParticleTypes.DRAGON_BREATH, px, py, pz, 5, 0.2, 0.2, 0.2, 0.0);
            } else {
                serverLevel.sendParticles(net.minecraft.core.particles.ParticleTypes.END_ROD, px, py, pz, 10, 0.3, 0.3, 0.3, 0.01);
                serverLevel.sendParticles(net.minecraft.core.particles.ParticleTypes.FLASH, px, py, pz, 2, 0.1, 0.1, 0.1, 0.0);
            }
        }
    }
}