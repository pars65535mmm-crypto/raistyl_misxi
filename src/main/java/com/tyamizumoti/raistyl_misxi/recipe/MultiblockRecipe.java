package com.tyamizumoti.raistyl_misxi.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.HashMap;
import java.util.Map;

public class MultiblockRecipe implements Recipe<SimpleContainer> {
    private final ResourceLocation id;
    private final Map<Position, ItemStack> pedestalItems;
    private final int energyCost;
    private final int craftTime;
    private final ItemStack result;

    public static class Position {
        public final int x, y;
        public Position(int x, int y) {
            this.x = x;
            this.y = y;
        }
        @Override
        public boolean equals(Object o) {
            if (!(o instanceof Position p)) return false;
            return x == p.x && y == p.y;
        }
        @Override
        public int hashCode() {
            return 31 * x + y;
        }
    }

    public MultiblockRecipe(ResourceLocation id, Map<Position, ItemStack> pedestalItems, int energyCost, int craftTime, ItemStack result) {
        this.id = id;
        this.pedestalItems = pedestalItems;
        this.energyCost = energyCost;
        this.craftTime = craftTime;
        this.result = result;
    }

    public boolean matches(Map<Position, ItemStack> currentPedestals) {
        if (currentPedestals.size() != pedestalItems.size()) return false;
        for (Map.Entry<Position, ItemStack> entry : pedestalItems.entrySet()) {
            ItemStack current = currentPedestals.get(entry.getKey());
            if (current == null || !ItemStack.isSameItemSameTags(current, entry.getValue())) {
                return false;
            }
        }
        return true;
    }

    public int getEnergyCost() { return energyCost; }
    public int getCraftTime() { return craftTime; }
    public ItemStack getResult() { return result.copy(); }

    // === Recipe インターフェースの実装（必須） ===

    @Override
    public boolean matches(SimpleContainer container, Level level) {
        // マルチブロックレシピはペデスタル用なので、ここでは使用しない
        return false;
    }

    @Override
    public ItemStack assemble(SimpleContainer container, RegistryAccess access) {
        return result.copy();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getResultItem(RegistryAccess access) {
        return result;
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.MULTIBLOCK_SERIALIZER.get();  // ← これを追加！
    }

    @Override
    public RecipeType<?> getType() {
        return ModRecipes.MULTIBLOCK_TYPE.get();  // ← .get() を正しく付ける！
    }

    // === シリアライザー（JSON ↔ Java の変換） ===

    public static class Serializer implements RecipeSerializer<MultiblockRecipe> {
        @Override
        public MultiblockRecipe fromJson(ResourceLocation id, JsonObject json) {
            JsonArray items = GsonHelper.getAsJsonArray(json, "pedestals");
            Map<Position, ItemStack> pedestalItems = new HashMap<>();
            for (var elem : items) {
                JsonObject obj = elem.getAsJsonObject();
                int x = GsonHelper.getAsInt(obj, "x");
                int y = GsonHelper.getAsInt(obj, "y");
                String itemId = GsonHelper.getAsString(obj, "item");
                ItemStack stack = new ItemStack(ForgeRegistries.ITEMS.getValue(new ResourceLocation(itemId)));
                pedestalItems.put(new Position(x, y), stack);
            }
            int energyCost = GsonHelper.getAsInt(json, "energy_cost");
            int craftTime = GsonHelper.getAsInt(json, "craft_time");
            JsonObject resultObj = GsonHelper.getAsJsonObject(json, "result");
            String resultId = GsonHelper.getAsString(resultObj, "item");
            int count = GsonHelper.getAsInt(resultObj, "count", 1);
            ItemStack result = new ItemStack(ForgeRegistries.ITEMS.getValue(new ResourceLocation(resultId)), count);
            return new MultiblockRecipe(id, pedestalItems, energyCost, craftTime, result);
        }

        @Override
        public MultiblockRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buffer) {
            Map<Position, ItemStack> pedestalItems = new HashMap<>();
            int size = buffer.readInt();
            for (int i = 0; i < size; i++) {
                int x = buffer.readInt();
                int y = buffer.readInt();
                ItemStack stack = buffer.readItem();
                pedestalItems.put(new Position(x, y), stack);
            }
            int energyCost = buffer.readInt();
            int craftTime = buffer.readInt();
            ItemStack result = buffer.readItem();
            return new MultiblockRecipe(id, pedestalItems, energyCost, craftTime, result);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, MultiblockRecipe recipe) {
            buffer.writeInt(recipe.pedestalItems.size());
            for (var entry : recipe.pedestalItems.entrySet()) {
                buffer.writeInt(entry.getKey().x);
                buffer.writeInt(entry.getKey().y);
                buffer.writeItem(entry.getValue());
            }
            buffer.writeInt(recipe.energyCost);
            buffer.writeInt(recipe.craftTime);
            buffer.writeItem(recipe.result);
        }
    }

    // レシピタイプの識別子（内部用）
    /*
    public static class Type implements RecipeType<MultiblockRecipe> {
        public static final Type INSTANCE = new Type();
        public static final String ID = "multiblock_crafting";
    }*/
}