package com.tyamizumoti.raistyl_misxi;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import com.tyamizumoti.raistyl_misxi.fluid.ModFluids;
import net.minecraft.world.level.block.LiquidBlock;
import java.util.function.Supplier;

public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, "raistyl_misxi");
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, "raistyl_misxi");

    // ⏳ 時間収集機ブロックの登録
    public static final RegistryObject<Block> TIME_COLLECTOR = registerBlock("time_collector",
        () -> new TimeCollectorBlock(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK)));
        
    // 🛠️ ブロックと同時に「手持ちアイテム版」も自動登録する便利メソッド
    private static <T extends Block> RegistryObject<T> registerBlock(String name, Supplier<T> block) {
        RegistryObject<T> toReturn = BLOCKS.register(name, block);
        registerBlockItem(name, toReturn);
        return toReturn;
    }

    private static <T extends Block> RegistryObject<Item> registerBlockItem(String name, RegistryObject<T> block) {
        return ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
    }

    // メインクラスのコンストラクタあたりからこれを1回呼ぶ必要がある
    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
        ITEMS.register(eventBus);
    }
    // 世界に流体を置いたときに実体化する液体ブロック
    public static final RegistryObject<LiquidBlock> TIME_FLUID_BLOCK = BLOCKS.register("time_fluid_block",
        () -> new LiquidBlock(ModFluids.SOURCE_TIME_FLUID, BlockBehaviour.Properties.copy(Blocks.WATER).noCollission()));
}