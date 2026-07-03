package com.tyamizumoti.raistyl_misxi.recipe;

import com.google.gson.JsonObject;
import com.tyamizumoti.raistyl_misxi.RaistylMisxi;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

public class AetherFluidWorkbenchRecipe implements Recipe<SimpleContainer> {
    private final ResourceLocation id;
    private final Ingredient inputIngredient;
    private final FluidStack inputFluid;
    private final ItemStack output;

    public AetherFluidWorkbenchRecipe(ResourceLocation id, Ingredient inputIngredient, FluidStack inputFluid, ItemStack output) {
        this.id = id;
        this.inputIngredient = inputIngredient;
        this.inputFluid = inputFluid;
        this.output = output;
    }

    @Override
public boolean matches(SimpleContainer pContainer, Level pLevel) {
    // ※注意：もし液体バーのチェックもBlockEntity側ではなく、
    // このRecipeクラス側で同時にやりたい場合はここに液体のチェックも追加できます。

    // 3×3の格子（全9スロット）のなかに、レシピのアイテム（ウラン）が含まれているか走査する
    for (int i = 0; i < pContainer.getContainerSize(); i++) {
        ItemStack stack = pContainer.getItem(i);
        if (!stack.isEmpty() && inputIngredient.test(stack)) {
            return true; // ウランが見つかったので一致！
        }
    }
    return false;
}

    public FluidStack getInputFluid() { return this.inputFluid; }
    public Ingredient getInputIngredient() { return this.inputIngredient; }

    @Override
    public ItemStack assemble(SimpleContainer pContainer, RegistryAccess pRegistryAccess) { return output.copy(); }
    @Override
    public boolean canCraftInDimensions(int pWidth, int pHeight) { return true; }
    @Override
    public ItemStack getResultItem(RegistryAccess pRegistryAccess) { return output; }
    @Override
    public ResourceLocation getId() { return id; }
    @Override
    public RecipeSerializer<?> getSerializer() { return Serializer.INSTANCE; }
    @Override
    public RecipeType<?> getType() { return Type.INSTANCE; }

    // ⚙️ JEIやシステムが認識するためのType
    public static class Type implements RecipeType<AetherFluidWorkbenchRecipe> {
        public static final Type INSTANCE = new Type();
        public static final String ID = "aether_fluid_workbench";
    }

    // 💾 JSON ➔ Java / ネットワーク通信用のシリアライザー
    public static class Serializer implements RecipeSerializer<AetherFluidWorkbenchRecipe> {
        public static final Serializer INSTANCE = new Serializer();
        public static final ResourceLocation ID = new ResourceLocation(RaistylMisxi.MODID, "aether_fluid_workbench");

        @Override
        public AetherFluidWorkbenchRecipe fromJson(ResourceLocation pRecipeId, JsonObject pSerializedRecipe) {
            // アイテム入力の読み込み
            Ingredient ingredient = Ingredient.fromJson(GsonHelper.getAsJsonObject(pSerializedRecipe, "ingredient"));
    
            // 流体入力の読み込み
            JsonObject fluidJson = GsonHelper.getAsJsonObject(pSerializedRecipe, "fluid");
            String fluidName = GsonHelper.getAsString(fluidJson, "fluid");
            int fluidAmount = GsonHelper.getAsInt(fluidJson, "amount");
            FluidStack fluidStack = new FluidStack(ForgeRegistries.FLUIDS.getValue(new ResourceLocation(fluidName)), fluidAmount);

            // 完成品の読み込み
            ItemStack result = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(pSerializedRecipe, "result"));

            return new AetherFluidWorkbenchRecipe(pRecipeId, ingredient, fluidStack, result);
        }

        @Override
        public @Nullable AetherFluidWorkbenchRecipe fromNetwork(ResourceLocation pRecipeId, FriendlyByteBuf pBuffer) {
            Ingredient ingredient = Ingredient.fromNetwork(pBuffer);
            FluidStack fluid = pBuffer.readFluidStack();
            ItemStack result = pBuffer.readItem();
            return new AetherFluidWorkbenchRecipe(pRecipeId, ingredient, fluid, result);
        }

        @Override
        public void toNetwork(FriendlyByteBuf pBuffer, AetherFluidWorkbenchRecipe pRecipe) {
            pRecipe.inputIngredient.toNetwork(pBuffer);
            pBuffer.writeFluidStack(pRecipe.inputFluid);
            pBuffer.writeItem(pRecipe.output);
        }
    }
}