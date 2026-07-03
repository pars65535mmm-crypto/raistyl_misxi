package com.tyamizumoti.raistyl_misxi.event;

import com.tyamizumoti.raistyl_misxi.RaistylMisxi;
import com.tyamizumoti.raistyl_misxi.item.IncompleteChronoIngotItem;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

@Mod.EventBusSubscriber(modid = RaistylMisxi.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class VoidCraftingEvent {

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        // サーバー側かつ、毎Tickの終わりのタイミングでのみ処理する
        if (event.side.isClient() || event.phase != TickEvent.Phase.END) return;

        Player player = event.player;
        
        // プレイヤーの周囲（半径128マス以内）に落ちているすべてのアイテムエンティティを探し出す
        AABB searchArea = player.getBoundingBox().inflate(128.0);
        List<ItemEntity> items = player.level().getEntitiesOfClass(ItemEntity.class, searchArea);

        for (ItemEntity itemEntity : items) {
            // 1. Y座標がマイナス64（奈落の底）より下になったかチェック
            if (itemEntity.getY() < -64) {
                ItemStack stack = itemEntity.getItem();

                // 2. NBT（Heated）が付いている自作インゴットかチェック
                if (stack.getItem() instanceof IncompleteChronoIngotItem && stack.hasTag() && stack.getTag().getBoolean("Heated")) {
                    
                    // 🎲 3. 1/255 の確率で手元に還元されるガチャ（★テスト用に一時的に数値を 2 や 1 にすると確実に成功します！）
                    if (player.level().getRandom().nextInt(255) == 0) {
                        
                        ItemStack finalIngot = new ItemStack(com.tyamizumoti.raistyl_misxi.RaistylMisxi.CHRONO_INGOT.get(), 1);
                        
                        if (!player.getInventory().add(finalIngot)) {
                            player.drop(finalIngot, false);
                        }
                        
                        player.sendSystemMessage(Component.literal("虚空の底から、完全なる輝きが還ってきた。").withStyle(ChatFormatting.GOLD));
                        player.level().playSound(null, player.getX(), player.getY(), player.getZ(), 
                            SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, 0.5F);
                    } else {
                    }
                    
                    // 奈落に落ちたエンティティ自体は、成否に関わらず消去して重さを防ぐ
                    itemEntity.discard();
                }
            }
        }
    }
}
