package com.tyamizumoti.raistyl_misxi.block.entity;

import com.tyamizumoti.raistyl_misxi.CraftingPedestalBlockEntity;
import com.tyamizumoti.raistyl_misxi.MasterOutputBlockEntity;
import com.tyamizumoti.raistyl_misxi.ModBlocks;
import com.tyamizumoti.raistyl_misxi.RaistylMisxi;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.core.registries.Registries;
import com.tyamizumoti.raistyl_misxi.client.CraftingPedestalRenderer;     // ← 追加！
import com.tyamizumoti.raistyl_misxi.client.MasterOutputCoreRenderer;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, "raistyl_misxi");

    public static final RegistryObject<BlockEntityType<TimeCollectorBlockEntity>> TIME_COLLECTOR =
            BLOCK_ENTITIES.register("time_collector", () ->
                    BlockEntityType.Builder.of(TimeCollectorBlockEntity::new,
                            ModBlocks.TIME_COLLECTOR.get()).build(null));

    public static final RegistryObject<BlockEntityType<CraftingPedestalBlockEntity>> CRAFTING_PEDESTAL =
            BLOCK_ENTITIES.register("crafting_pedestal", () ->
                    BlockEntityType.Builder.of(CraftingPedestalBlockEntity::new,
                            RaistylMisxi.CRAFTING_PEDESTAL.get()).build(null));

    public static final RegistryObject<BlockEntityType<MasterOutputBlockEntity>> MASTER_OUTPUT_CORE =
            BLOCK_ENTITIES.register("master_output_core", () ->
                    BlockEntityType.Builder.of(MasterOutputBlockEntity::new,
                            RaistylMisxi.MASTER_OUTPUT_CORE.get()).build(null));

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }

    // ✅ このメソッドを追加！
    @OnlyIn(Dist.CLIENT)
    public static void registerRenderers() {
        BlockEntityRenderers.register(CRAFTING_PEDESTAL.get(), CraftingPedestalRenderer::new);
        BlockEntityRenderers.register(MASTER_OUTPUT_CORE.get(), MasterOutputCoreRenderer::new);
    }
}