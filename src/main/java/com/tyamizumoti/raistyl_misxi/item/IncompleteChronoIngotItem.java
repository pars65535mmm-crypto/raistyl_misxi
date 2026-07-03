package com.tyamizumoti.raistyl_misxi.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import java.util.function.Consumer;

public class IncompleteChronoIngotItem extends Item {
    public IncompleteChronoIngotItem(Properties pProperties) {
        super(pProperties);
    }

    // 🌟 NBT（Heated）が付いているときだけ、エンチャントの輝きを付ける
    @Override
    public boolean isFoil(ItemStack pStack) {
        return pStack.hasTag() && pStack.getTag().getBoolean("Heated");
    }

    // 📛 NBTが付いているときだけ、名前を「時空グラデーションする朽ちたクロノインゴット」に変身させる
    @Override
    public Component getName(ItemStack pStack) {
        if (pStack.hasTag() && pStack.getTag().getBoolean("Heated")) {
            String text = "朽ちたクロノインゴット";
            MutableComponent component = Component.literal("");
            long time = System.currentTimeMillis();

            for (int i = 0; i < text.length(); i++) {
                double phase = (time / 300.0) - (i * 0.4); 
                int r = (int) (Math.sin(phase) * 50 + 180);
                int g = (int) (Math.sin(phase + 2) * 40 + 60);
                int b = (int) (Math.sin(phase + 4) * 50 + 200);
                int rgb = (Math.max(0, Math.min(255, r)) << 16) | (Math.max(0, Math.min(255, g)) << 8) | Math.max(0, Math.min(255, b));

                component.append(Component.literal(String.valueOf(text.charAt(i)))
                    .withStyle(Style.EMPTY.withColor(TextColor.fromRgb(rgb)).withBold(true)));
            }
            return component;
        }
        return super.getName(pStack);
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            @Override
            public net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return com.tyamizumoti.raistyl_misxi.client.renderer.ChronoItemRenderer.getInstance();
            }
        });
    }
}
