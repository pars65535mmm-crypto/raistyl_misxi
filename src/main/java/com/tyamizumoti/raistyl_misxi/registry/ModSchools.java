package com.tyamizumoti.raistyl_misxi.registry;

// ⭕️ エラーの元だったSoundRegistryのインポートを削除し、バニラのSoundEventsを追加
import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.SchoolType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents; // バニラの効果音
import net.minecraft.tags.ItemTags;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ModSchools {
    public static final String MOD_ID = "raistyl_misxi";

    // SchoolRegistryの正しいキー
    public static final DeferredRegister<SchoolType> SCHOOLS = 
            DeferredRegister.create(SchoolRegistry.SCHOOL_REGISTRY_KEY, MOD_ID);

    // 1.20.1 (3.16.1) の仕様に完全に合致させた登録
    public static final RegistryObject<SchoolType> TIME = SCHOOLS.register("time", () -> 
            new SchoolType(
                    // 1. SchoolのID
                    new ResourceLocation(MOD_ID, "time"),
                    
                    // 2. 属性に関連するアイテムタグ（仮）
                    ItemTags.PIGLIN_LOVED,
                    
                    // 3. 翻訳キー（表示名）
                    Component.translatable("school." + MOD_ID + ".time"),
                    
                    // 4. 魔法パワー属性
                    () -> (Attribute) AttributeRegistry.SPELL_POWER.get(),
                    
                    // 5. 魔法耐性属性（両方ともSPELL_POWERで一旦適合させます）
                    () -> (Attribute) AttributeRegistry.SPELL_POWER.get(),
                    
                    // 6. 詠唱効果音（⭕️エラー回避のためバニラのエンドポータル音で代用）
                    () -> (SoundEvent) SoundEvents.END_PORTAL_FRAME_FILL,
                    
                    // 7. ダメージタイプのリソースキー
                    ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation("minecraft", "magic"))
            )
    );

    public static void register(IEventBus bus) {
        SCHOOLS.register(bus);
    }
}
