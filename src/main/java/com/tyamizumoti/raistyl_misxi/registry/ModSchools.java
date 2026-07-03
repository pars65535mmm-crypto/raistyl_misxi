// ModSchools.java の中身
package com.tyamizumoti.raistyl_misxi.registry;

// 主要なクラスをインポート（パッケージ名は適宜修正してください）
import io.redspace.ironsspellbooks.api.spells.SchoolType;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
public class ModSchools {
    // SchoolType用のレジストリを登録し、"time"属性を定義
    public static final DeferredRegister<SchoolType> SCHOOLS = ...;
    public static final RegistryObject<SchoolType> TIME = ...;

    public static void register(IEventBus bus) {
        SCHOOLS.register(bus);
    }
}
