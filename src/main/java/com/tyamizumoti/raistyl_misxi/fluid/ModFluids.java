package com.tyamizumoti.raistyl_misxi.fluid;

import com.tyamizumoti.raistyl_misxi.ModBlocks;
import com.tyamizumoti.raistyl_misxi.RaistylMisxi;
import com.tyamizumoti.raistyl_misxi.RaistylMisxi;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.common.SoundActions;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.ForgeFlowingFluid;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import java.util.function.Consumer;

public class ModFluids {
    public static final DeferredRegister<Fluid> FLUIDS =
            DeferredRegister.create(Registries.FLUID, "raistyl_misxi");

    public static final DeferredRegister<FluidType> FLUID_TYPES =
            DeferredRegister.create(ForgeRegistries.Keys.FLUID_TYPES, "raistyl_misxi");

    // 🌊 1. 流体の「性質（タイプ）」を定義（音やネバネバ度など）
    public static final RegistryObject<FluidType> TIME_FLUID_TYPE = FLUID_TYPES.register("time_fluid",
            () -> new FluidType(FluidType.Properties.create()
                    .lightLevel(5) // ほんのり発光
                    .density(1500) // 水よりちょっと重め
                    .viscosity(1500) // ちょっとネバネバ
                    .sound(SoundActions.BUCKET_FILL, SoundEvents.BUCKET_FILL)
                    .sound(SoundActions.BUCKET_EMPTY, SoundEvents.BUCKET_EMPTY)) {
                
                // 🎨 クライアント側での見た目（テクスチャ）の設定
                @Override
                public void initializeClient(Consumer<IClientFluidTypeExtensions> consumer) {
                    consumer.accept(new IClientFluidTypeExtensions() {
                        // とりあえずバニラの「流れる水」のテクスチャを借りて、後で色を塗る作戦
                        private static final ResourceLocation WATER_STILL = new ResourceLocation("block/water_still");
                        private static final ResourceLocation WATER_FLOW = new ResourceLocation("block/water_flow");

                        @Override
                        public ResourceLocation getStillTexture() { return WATER_STILL; }
                        @Override
                        public ResourceLocation getFlowingTexture() { return WATER_FLOW; }

                        @Override
                        public int getTintColor() {
                            // 🔮 時流流体の色（ARGBを16進数で指定。ここでは怪しい紫：0xFF9900FF）
                            return 0xFF9900FF;
                        }
                    });
                }
            });

    // 🌊 2. 「水源（静止状態）」の流体ブロック
    public static final RegistryObject<FlowingFluid> SOURCE_TIME_FLUID = FLUIDS.register("time_fluid",
            () -> new ForgeFlowingFluid.Source(ModFluids.TIME_FLUID_PROPERTIES));

    // 🌊 3. 「流水（流れている状態）」の流体ブロック
    public static final RegistryObject<FlowingFluid> FLOWING_TIME_FLUID = FLUIDS.register("flowing_time_fluid",
            () -> new ForgeFlowingFluid.Flowing(ModFluids.TIME_FLUID_PROPERTIES));


    // ⚙️ 流体の各種リンク設定（水源、流水、バケツ、設置ブロックを全部紐付ける）
    public static final ForgeFlowingFluid.Properties TIME_FLUID_PROPERTIES = new ForgeFlowingFluid.Properties(
            TIME_FLUID_TYPE, SOURCE_TIME_FLUID, FLOWING_TIME_FLUID)
            .slopeFindDistance(4) // どこまで流れるか（水は7、溶岩は通常4）
            .levelDecreasePerBlock(1) // 流れるごとにどれくらい水位が下がるか
            .block(() -> ModBlocks.TIME_FLUID_BLOCK.get()) // 設置されるブロック
            .bucket(() -> RaistylMisxi.TIME_FLUID_BUCKET.get()); // すくうバケツアイテム


    public static void register(IEventBus eventBus) {
        FLUIDS.register(eventBus);
        FLUID_TYPES.register(eventBus);
    }
}