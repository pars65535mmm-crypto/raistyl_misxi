package com.tyamizumoti.raistyl_misxi.effect;

import com.tyamizumoti.raistyl_misxi.RaistylMisxi;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModEffects {
    public static final DeferredRegister<MobEffect> MOB_EFFECTS = DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, RaistylMisxi.MODID);

    // 💤 ① 眠りエフェクト (drowsinesspotion.png を紐づける)
    public static final RegistryObject<MobEffect> SLEEP = MOB_EFFECTS.register("sleep",
    () -> new MobEffect(MobEffectCategory.HARMFUL, 0x224488) {
        @Override
        public void applyEffectTick(LivingEntity entity, int amplifier) {
            entity.setDeltaMovement(0, entity.getDeltaMovement().y, 0);
        }
        @Override
        public boolean isDurationEffectTick(int duration, int amplifier) { return true; }
    });

    // 🖤 ② 黒焔エフェクト (blocksoul_fire.png を紐づける)
    public static final RegistryObject<MobEffect> BLOCKSOUL_FIRE = MOB_EFFECTS.register("blocksoul_fire",
        () -> new CustomEffect(MobEffectCategory.HARMFUL, 0x111111) {
            @Override
            public void applyEffectTick(LivingEntity entity, int amplifier) {
                // 黒焔：じわじわと体力を割合、または魔法ダメージで焼き尽くす
                entity.hurt(entity.damageSources().magic(), 2.0F + amplifier);
            }
            @Override
            public boolean isDurationEffectTick(int duration, int amplifier) {
                return duration % 20 == 0; // 1秒ごとにダメージ
            }
        });

    // 🩸 ③ 出血エフェクト (動くとダメージ増加)
    public static final RegistryObject<MobEffect> BLEEDING = MOB_EFFECTS.register("bleeding",
        () -> new CustomEffect(MobEffectCategory.HARMFUL, 0x880000) {
            @Override
            public void applyEffectTick(LivingEntity entity, int amplifier) {
                Vec3 motion = entity.getDeltaMovement();
                // 速度の絶対値から動いているかを判定
                double speed = Math.sqrt(motion.x * motion.x + motion.z * motion.z);
                if (speed > 0.03) {
                    // 動けば動くだけ痛い（激しい出血ダメージ）
                    entity.hurt(entity.damageSources().magic(), (float)(3.0F + (speed * 10.0) * (amplifier + 1)));
                } else {
                    // 止まっていても微小な継続ダメージ
                    entity.hurt(entity.damageSources().magic(), 0.5F);
                }
            }
            @Override
            public boolean isDurationEffectTick(int duration, int amplifier) { return duration % 10 == 0; }
        });

    // 🌀 ④ 混乱エフェクト (向いてる方向がランダムになる)
    public static final RegistryObject<MobEffect> CONFUSION = MOB_EFFECTS.register("confusion",
        () -> new CustomEffect(MobEffectCategory.HARMFUL, 0xCC00FF) {
            @Override
            public void applyEffectTick(LivingEntity entity, int amplifier) {
                if (entity.level().random.nextFloat() < 0.2F) {
                    // 20%の確率で突然あらぬ方向を向かせる
                    float randomYaw = entity.level().random.nextFloat() * 360.0F;
                    entity.setYRot(randomYaw);
                    entity.setYHeadRot(randomYaw);
                }
            }
            @Override
            public boolean isDurationEffectTick(int duration, int amplifier) { return true; }
        });

    public static void register(IEventBus eventBus) {
        MOB_EFFECTS.register(eventBus);
    }

    // エフェクトの基本クラス
    public static class CustomEffect extends MobEffect {
    protected CustomEffect(MobEffectCategory category, int color) { super(category, color); }
}
}