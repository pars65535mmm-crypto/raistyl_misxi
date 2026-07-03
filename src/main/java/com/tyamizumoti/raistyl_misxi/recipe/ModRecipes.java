package com.tyamizumoti.raistyl_misxi.recipe;

import com.tyamizumoti.raistyl_misxi.RaistylMisxi;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModRecipes {
    public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS =
            DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, RaistylMisxi.MODID);

    // ✅ RecipeType 用の DeferredRegister を新設！
    public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES =
            DeferredRegister.create(ForgeRegistries.RECIPE_TYPES, RaistylMisxi.MODID);

    // --- 既存のレシピ ---
    public static final RegistryObject<RecipeSerializer<AetherFluidWorkbenchRecipe>> AETHER_FLUID_WORKBENCH_SERIALIZER =
            SERIALIZERS.register("aether_fluid_workbench", () -> AetherFluidWorkbenchRecipe.Serializer.INSTANCE);

    public static final RegistryObject<RecipeSerializer<NBTSmeltingRecipe>> NBT_SMELTING_SERIALIZER =
            SERIALIZERS.register("nbt_smelting", NBTSmeltingRecipe.Serializer::new);

    // --- マルチブロックレシピ ---
    public static final RegistryObject<RecipeSerializer<MultiblockRecipe>> MULTIBLOCK_SERIALIZER =
            SERIALIZERS.register("multiblock_crafting", MultiblockRecipe.Serializer::new);

    // ✅ RecipeType は RegistryObject で持つ！
    public static final RegistryObject<RecipeType<MultiblockRecipe>> MULTIBLOCK_TYPE =
            RECIPE_TYPES.register("multiblock_crafting",
                    () -> new RecipeType<MultiblockRecipe>() {
                        @Override
                        public String toString() {
                            return "multiblock_crafting";
                        }
                    });

    public static void register(IEventBus eventBus) {
        RECIPE_TYPES.register(eventBus);
        SERIALIZERS.register(eventBus);
          // ← これも忘れずに！
    }
}