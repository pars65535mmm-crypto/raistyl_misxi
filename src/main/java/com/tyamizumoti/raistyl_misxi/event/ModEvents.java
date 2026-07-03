package com.tyamizumoti.raistyl_misxi.event;

import com.tyamizumoti.raistyl_misxi.RaistylMisxi;
import com.tyamizumoti.raistyl_misxi.item.ChronoIngotItem;
import com.tyamizumoti.raistyl_misxi.item.IncompleteChronoIngotItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = RaistylMisxi.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ModEvents {

    @SubscribeEvent
    public static void onItemSmelted(PlayerEvent.ItemSmeltedEvent event) {
        ItemStack stack = event.getSmelting();
        
        // かまどから取り出したアイテムが、あの自作インゴットクラスだった場合
        if (stack.getItem() instanceof IncompleteChronoIngotItem) {
            // NBTタグを生成して "Heated = true" を無理やり書き込むぜ！
            CompoundTag tag = stack.getOrCreateTag();
            tag.putBoolean("Heated", true);
            stack.setTag(tag);
        }
    }


     @net.minecraftforge.eventbus.api.SubscribeEvent
    public static void onTooltipColor(net.minecraftforge.client.event.RenderTooltipEvent.Color event) {
        ItemStack stack = event.getItemStack();
        
        // ⏰【最上部に配置】ここでしっかりと time 変数を宣言して取得するぜ！
        long time = System.currentTimeMillis();
        
        // 🔮 パターン1：朽ちたクロノインゴット（紫〜ピンクに明滅）
        if (stack.getItem() instanceof IncompleteChronoIngotItem && stack.hasTag() && stack.getTag().getBoolean("Heated")) {
            float pulse = (float) (Math.sin(time / 200.0) * 0.35 + 0.65);
            int borderStart = (255 << 24) | ((int)(180 * pulse) << 16) | ((int)(50 * pulse) << 8) | (int)(220 * pulse);
            int borderEnd = (255 << 24) | ((int)(220 * pulse) << 16) | ((int)(50 * pulse) << 8) | (int)(180 * pulse);
            event.setBorderStart(borderStart);
            event.setBorderEnd(borderEnd);
        }
        
        // 🌌 パターン2：完全版クロノインゴット（白〜神秘的な水色に明滅！）
        else if (stack.getItem() instanceof ChronoIngotItem) {
            // これで time が見つからないエラーが完全に消滅します！
            float pulse = (float) (Math.sin(time / 300.0) * 0.3 + 0.7);
            
            int r = (int) (160 * pulse);
            int g = (int) (220 * pulse);
            int b = (int) (255 * pulse); // 水色ベース
            
            int borderStart = (255 << 24) | (r << 16) | (g << 8) | b;
            int borderEnd = (255 << 24) | (255 << 16) | (255 << 8) | 255; // 下側は純白！
            
            event.setBorderStart(borderStart);
            event.setBorderEnd(borderEnd);
        }
    }

}
