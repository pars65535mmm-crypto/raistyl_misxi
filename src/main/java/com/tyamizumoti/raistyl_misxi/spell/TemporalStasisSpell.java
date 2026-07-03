package com.tyamizumoti.raistyl_misxi.spell;

import com.tyamizumoti.raistyl_misxi.RaistylMisxi;
import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.spells.*;
import io.redspace.ironsspellbooks.api.util.Utils;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import java.util.List;

// ★ @AutoSpellConfig は削除（上司のコードにもない）
public class TemporalStasisSpell extends AbstractSpell {
    
    private final ResourceLocation spellId = ResourceLocation.fromNamespaceAndPath(RaistylMisxi.MODID, "temporal_stasis");

    public TemporalStasisSpell() {
        // ★【重要】JSON（データパック）で属性を追加する場合、
        // 親のコンストラクタには何も渡さない（引数なしのsuper()、または何も書かない）のが正解です。
        super(); 

        this.manaCostPerLevel = 5;
        this.baseSpellPower = 3;
        this.spellPowerPerLevel = 1;
        this.castTime = 0;
        this.baseManaCost = 20;
    }

    // ★ DefaultConfig は setSchoolResource() を使う（JSONのSchool IDを指定）
    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setSchoolResource(new ResourceLocation(RaistylMisxi.MODID, "time"))
            .setMinRarity(SpellRarity.RARE)
            .setMaxLevel(10)
            .setCooldownSeconds(30)
            .build();

    @Override
    public DefaultConfig getDefaultConfig() {
        return defaultConfig;
    }

    @Override
    public CastType getCastType() {
        return CastType.INSTANT;
    }

    @Override
    public ResourceLocation getSpellResource() {
        return spellId;
    }

    @Override
    public List<MutableComponent> getUniqueInfo(int spellLevel, LivingEntity caster) {
        return List.of(
                Component.translatable("ui.raistyl_misxi.radius", Utils.stringTruncation(getRadius(spellLevel), 1)),
                Component.translatable("ui.raistyl_misxi.duration", Utils.stringTruncation(getDuration(spellLevel) / 20.0, 1))
        );
    }

    private float getRadius(int spellLevel) {
        return 5.0f + spellLevel * 1.5f;
    }

    private int getDuration(int spellLevel) {
        return 40 + spellLevel * 20;
    }

    private int getMaxTargets(int spellLevel) {
        return 3 + spellLevel;
    }

    @Override
    public void onCast(Level level, int spellLevel, LivingEntity entity, CastSource castSource, MagicData playerMagicData) {
        if (level.isClientSide()) return;

        float radius = getRadius(spellLevel);
        int duration = getDuration(spellLevel);
        int maxTargets = getMaxTargets(spellLevel);

        List<LivingEntity> targets = level.getEntitiesOfClass(
                LivingEntity.class,
                new AABB(entity.getX() - radius, entity.getY() - radius, entity.getZ() - radius,
                        entity.getX() + radius, entity.getY() + radius, entity.getZ() + radius),
                e -> e != entity && !e.isDeadOrDying()
        );

        int count = 0;
        for (LivingEntity target : targets) {
            if (count >= maxTargets) break;
            if (target instanceof Player) continue;

            target.setNoGravity(true);
            target.setDeltaMovement(0, 0, 0);
            target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, duration, 255, false, false, true));
            target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, duration, 255, false, false, true));

            if (level instanceof ServerLevel serverLevel) {
                for (int i = 0; i < 20; i++) {
                    double px = target.getX() + (level.random.nextDouble() - 0.5) * 2;
                    double py = target.getY() + level.random.nextDouble() * 2;
                    double pz = target.getZ() + (level.random.nextDouble() - 0.5) * 2;
                    serverLevel.sendParticles(ParticleTypes.PORTAL, px, py, pz, 3, 0.1, 0.1, 0.1, 0.05);
                    serverLevel.sendParticles(ParticleTypes.END_ROD, px, py + 0.5, pz, 2, 0.2, 0.2, 0.2, 0.01);
                }
            }
            count++;
        }

        if (level instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.EXPLOSION, entity.getX(), entity.getY() + 1, entity.getZ(), 1, 0, 0, 0, 0);
            for (int i = 0; i < 50; i++) {
                double angle = level.random.nextDouble() * Math.PI * 2;
                double dist = level.random.nextDouble() * radius;
                double px = entity.getX() + Math.cos(angle) * dist;
                double pz = entity.getZ() + Math.sin(angle) * dist;
                serverLevel.sendParticles(ParticleTypes.DRAGON_BREATH, px, entity.getY() + 0.5, pz, 2, 0.1, 0.5, 0.1, 0.05);
            }
        }

        super.onCast(level, spellLevel, entity, castSource, playerMagicData);
    }
}