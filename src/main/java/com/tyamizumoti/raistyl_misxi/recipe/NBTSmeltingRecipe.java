package com.tyamizumoti.raistyl_misxi.recipe;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraftforge.registries.ForgeRegistries;

public class NBTSmeltingRecipe extends SmeltingRecipe {
    public NBTSmeltingRecipe(ResourceLocation id, String group, CookingBookCategory category, Ingredient ingredient, ItemStack result, float experience, int cookingTime) {
        super(id, group, category, ingredient, result, experience, cookingTime);
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.NBT_SMELTING_SERIALIZER.get();
    }

    public static class Serializer implements RecipeSerializer<NBTSmeltingRecipe> {
        @Override
        public NBTSmeltingRecipe fromJson(ResourceLocation recipeId, JsonObject json) {
            String group = GsonHelper.getAsString(json, "group", "");
            CookingBookCategory category = CookingBookCategory.CODEC.byName(GsonHelper.getAsString(json, "category", null), CookingBookCategory.MISC);
            
            JsonObject ingredientJson = GsonHelper.getAsJsonObject(json, "ingredient");
            Ingredient ingredient = Ingredient.fromJson(ingredientJson);
            
            JsonObject resultJson = GsonHelper.getAsJsonObject(json, "result");
            String itemId = GsonHelper.getAsString(resultJson, "item");
            var item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(itemId));
            if (item == null) throw new JsonSyntaxException("Unknown item: " + itemId);
            
            ItemStack result = new ItemStack(item);
            if (resultJson.has("nbt")) {
                try {
                    result.setTag(TagParser.parseTag(GsonHelper.getAsString(resultJson, "nbt")));
                } catch (CommandSyntaxException e) {
                    throw new JsonSyntaxException("Invalid NBT: " + e.getMessage());
                }
            }

            float experience = GsonHelper.getAsFloat(json, "experience", 0.0F);
            int cookingTime = GsonHelper.getAsInt(json, "cookingtime", 200);

            return new NBTSmeltingRecipe(recipeId, group, category, ingredient, result, experience, cookingTime);
        }

        @Override
        public NBTSmeltingRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
            String group = buffer.readUtf();
            CookingBookCategory category = buffer.readEnum(CookingBookCategory.class);
            Ingredient ingredient = Ingredient.fromNetwork(buffer);
            ItemStack result = buffer.readItem();
            float experience = buffer.readFloat();
            int cookingTime = buffer.readVarInt();
            return new NBTSmeltingRecipe(recipeId, group, category, ingredient, result, experience, cookingTime);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, NBTSmeltingRecipe recipe) {
            buffer.writeUtf(recipe.getGroup());
            buffer.writeEnum(recipe.category());
            recipe.getIngredients().get(0).toNetwork(buffer);
            buffer.writeItem(recipe.getResultItem(RegistryAccess.EMPTY));
            buffer.writeFloat(recipe.getExperience());
            buffer.writeVarInt(recipe.getCookingTime());
        }
    }
}
