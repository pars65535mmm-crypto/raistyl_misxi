package com.tyamizumoti.raistyl_misxi;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import com.tyamizumoti.raistyl_misxi.ModBlocks;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ModCreativeModeTabs {
    // クリエイティブタブ用のレジストリを作成
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = 
        DeferredRegister.create(Registries.CREATIVE_MODE_TAB, RaistylMisxi.MODID);

    // 🔥 専用のクリエイティブタブ「RaistylMisxi Tab」を登録！
    public static final RegistryObject<CreativeModeTab> RAISTYL_MISXI_TAB = CREATIVE_MODE_TABS.register("raistyl_misxi_tab",
        () -> CreativeModeTab.builder()
            // タブのアイコン（今回はエンジェルヘイローを看板にするで！）
            .icon(() -> new ItemStack(RaistylMisxi.ANGEL_HALO.get()))
            // タブのタイトル（インベントリを開いた時に上に表示される名前）
            .title(Component.literal("Raistyl Misxi"))
            // タブの中に並べるアイテムを登録する
            .displayItems((parameters, output) -> {
                output.accept(RaistylMisxi.ANGEL_HALO.get());
                output.accept(RaistylMisxi.RPG936_RMV1.get());
                output.accept(RaistylMisxi.THE_WHITE_HALO.get());
                output.accept(RaistylMisxi.ANGEL_HALO.get());
                output.accept(RaistylMisxi.DISASTROVA_SWORD.get());
                output.accept(RaistylMisxi.TIME_FLUID_BUCKET.get());
                output.accept(RaistylMisxi.UPGRADE_BASE.get());
                output.accept(RaistylMisxi.UPGRADE_BASE.get());
                output.accept(RaistylMisxi.UPGRALANK.get());
                output.accept(RaistylMisxi.UPGRADE_SPEED.get());
                output.accept(RaistylMisxi.AETHER_FLUID_WORKBENCH_ITEM.get());
                output.accept(RaistylMisxi.INCOMPLETE_CHRONO_INGOT.get());

                output.accept(ModBlocks.TIME_COLLECTOR.get());

                output.accept(RaistylMisxi.CRAFTING_PEDESTAL_ITEM.get());
                output.accept(RaistylMisxi.MASTER_OUTPUT_CORE_ITEM.get());
                output.accept(RaistylMisxi.POWER_INPUT_PORT_ITEM.get());
            })
            .build());

    // メインクラスから呼び出すための初期化メソッド
    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
    }
}