package com.tyamizumoti.raistyl_misxi;

import com.tyamizumoti.raistyl_misxi.recipe.MultiblockRecipe;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import com.tyamizumoti.raistyl_misxi.recipe.ModRecipes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import com.tyamizumoti.raistyl_misxi.block.entity.ModBlockEntities;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.EnergyStorage;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class MasterOutputBlockEntity extends BlockEntity {
    // --- 内部エネルギーストレージ（最大1,000,000 FE） ---
    private final EnergyStorage energyStorage = new EnergyStorage(1000000, 10000, 0);
    private final LazyOptional<IEnergyStorage> energyOptional = LazyOptional.of(() -> energyStorage);

    // --- 内部インベントリ（完成品を一時保存） ---
    public final ItemStackHandler inventory = new ItemStackHandler(1);
    private final LazyOptional<IItemHandler> itemOptional = LazyOptional.of(() -> inventory);

    // --- クラフト進行用 ---
    private MultiblockRecipe currentRecipe = null;
    private int craftProgress = 0;
    private int craftTime = 0;

    // --- アニメーション状態（クライアント同期用） ---
    private boolean isCrafting = false;
    private float craftAnimProgress = 0.0f; // 0.0〜1.0（中央に集まる進行度）
    private int animTicks = 0;


public MasterOutputBlockEntity(BlockPos pos, BlockState state) {
    super(ModBlockEntities.MASTER_OUTPUT_CORE.get(), pos, state);
}

    // --- 毎tick呼ばれるメイン処理 ---
    public static void tick(Level level, BlockPos pos, BlockState state, MasterOutputBlockEntity entity) {
        if (level.isClientSide()) return;

        // 1. 現在のペデスタル状態をスキャン
        Map<MultiblockRecipe.Position, ItemStack> currentItems = entity.scanPedestals(level, pos);

        // 2. レシピが未確定 or 変わった場合 → レシピを検索
        if (entity.currentRecipe == null || !entity.currentRecipe.matches(currentItems)) {
            Optional<MultiblockRecipe> recipe = entity.findMatchingRecipe(currentItems);
            if (recipe.isPresent()) {
                entity.currentRecipe = recipe.get();
                entity.craftProgress = 0;
                entity.craftTime = entity.currentRecipe.getCraftTime();
                entity.isCrafting = false;
                entity.craftAnimProgress = 0.0f;
                entity.animTicks = 0;
                // ペデスタルにアニメーション開始を通知（浮遊開始）
                entity.setPedestalAnimation(true, 0.0f);
                entity.setChanged();
            } else {
                entity.currentRecipe = null;
                entity.craftProgress = 0;
                entity.isCrafting = false;
                entity.craftAnimProgress = 0.0f;
                entity.animTicks = 0;
                // ペデスタルのアニメーションを停止
                entity.setPedestalAnimation(false, 0.0f);
                return;
            }
        }

        // 3. レシピが一致したら、エネルギーを消費してクラフト進行
        if (entity.currentRecipe != null) {
            int requiredEnergy = entity.currentRecipe.getEnergyCost();

            if (entity.energyStorage.getEnergyStored() >= requiredEnergy) {
                entity.energyStorage.extractEnergy(requiredEnergy, false);
                entity.craftProgress++;
                entity.animTicks++;
                entity.setChanged();

                // --- アニメーション進行（0.0〜1.0） ---
                float progress = (float) entity.craftProgress / (float) entity.craftTime;
                entity.craftAnimProgress = Math.min(1.0f, progress);
                
                // ペデスタルのアイテム浮遊高さを更新（クラフト進行に合わせて徐々に上がる）
                float height = entity.craftAnimProgress * 0.8f; // 最大0.8ブロック浮く
                float rotation = entity.animTicks * 0.5f; // 毎tick回転
                entity.setPedestalAnimation(true, height, rotation);

                // --- クラフト完了！ ---
                if (entity.craftProgress >= entity.craftTime) {
                    entity.completeCraft(level, pos);
                    entity.currentRecipe = null;
                    entity.craftProgress = 0;
                    entity.isCrafting = false;
                    entity.craftAnimProgress = 0.0f;
                    entity.animTicks = 0;
                    entity.setPedestalAnimation(false, 0.0f);
                }
            } else {
                // エネルギー不足で進行停止（アニメーションも止まる）
                entity.setPedestalAnimation(false, 0.0f);
            }
        }
    }

    // --- ペデスタル全基にアニメーションデータを送信 ---
    private void setPedestalAnimation(boolean animating, float height, float rotation) {
        if (level == null) return;
        BlockPos pedestalOrigin = worldPosition.south();
        for (int x = 0; x < 5; x++) {
            for (int y = 0; y < 5; y++) {
                BlockPos checkPos = pedestalOrigin.offset(x - 2, y - 2, 0);
                BlockEntity be = level.getBlockEntity(checkPos);
                if (be instanceof CraftingPedestalBlockEntity pedestal) {
                    pedestal.setAnimationData(animating, 0.0f, height, rotation);
                }
            }
        }
    }

    private void setPedestalAnimation(boolean animating, float height) {
        setPedestalAnimation(animating, height, 0.0f);
    }

    // --- ペデスタルをスキャン ---
    private Map<MultiblockRecipe.Position, ItemStack> scanPedestals(Level level, BlockPos corePos) {
        Map<MultiblockRecipe.Position, ItemStack> map = new HashMap<>();
        BlockPos pedestalOrigin = corePos.south();
        for (int x = 0; x < 5; x++) {
            for (int y = 0; y < 5; y++) {
                BlockPos checkPos = pedestalOrigin.offset(x - 2, y - 2, 0);
                BlockEntity be = level.getBlockEntity(checkPos);
                ItemStack stack = ItemStack.EMPTY;
                if (be instanceof CraftingPedestalBlockEntity pedestal) {
                    stack = pedestal.getItem();
                }
                map.put(new MultiblockRecipe.Position(x, y), stack);
            }
        }
        return map;
    }

    // --- レシピ検索 ---
    private Optional<MultiblockRecipe> findMatchingRecipe(Map<MultiblockRecipe.Position, ItemStack> currentItems) {
        if (level == null) return Optional.empty();
        var recipes = level.getRecipeManager().getAllRecipesFor(ModRecipes.MULTIBLOCK_TYPE.get());        for (MultiblockRecipe recipe : recipes) {
            if (recipe.matches(currentItems)) {
                return Optional.of(recipe);
            }
        }
        return Optional.empty();
    }

    // --- クラフト完了 ---
    private void completeCraft(Level level, BlockPos corePos) {
        if (currentRecipe == null) return;

        // 1. ペデスタルのアイテムを全消去
        BlockPos pedestalOrigin = corePos.south();
        for (int x = 0; x < 5; x++) {
            for (int y = 0; y < 5; y++) {
                BlockPos checkPos = pedestalOrigin.offset(x - 2, y - 2, 0);
                BlockEntity be = level.getBlockEntity(checkPos);
                if (be instanceof CraftingPedestalBlockEntity pedestal) {
                    pedestal.setItem(ItemStack.EMPTY);
                }
            }
        }

        // 2. 完成品を内部インベントリにセット
        ItemStack result = currentRecipe.getResult();
        inventory.setStackInSlot(0, result);

        // 3. 演出（パーティクル＆エフェクト）
        if (level instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(
                net.minecraft.core.particles.ParticleTypes.FLASH,
                corePos.getX(), corePos.getY() + 1, corePos.getZ(),
                30, 1.0, 1.0, 1.0, 0.1
            );
            serverLevel.sendParticles(
                net.minecraft.core.particles.ParticleTypes.END_ROD,
                corePos.getX(), corePos.getY() + 0.5, corePos.getZ(),
                50, 0.5, 0.5, 0.5, 0.05
            );
            // 大きな音（任意）
            // serverLevel.playSound(null, corePos, SoundEvents.BEACON_ACTIVATE, SoundSource.BLOCKS, 1.0f, 1.0f);
        }
    }

    // --- エネルギーCapability ---
    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ENERGY) return energyOptional.cast();
        if (cap == ForgeCapabilities.ITEM_HANDLER) return itemOptional.cast();
        return super.getCapability(cap, side);
    }

    // --- NBT保存 ---
    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        energyStorage.deserializeNBT(tag.getCompound("Energy"));
        inventory.deserializeNBT(tag.getCompound("Inventory"));
        craftProgress = tag.getInt("Progress");
        isCrafting = tag.getBoolean("IsCrafting");
        craftAnimProgress = tag.getFloat("AnimProgress");
        animTicks = tag.getInt("AnimTicks");
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("Energy", energyStorage.serializeNBT());
        tag.put("Inventory", inventory.serializeNBT());
        tag.putInt("Progress", craftProgress);
        tag.putBoolean("IsCrafting", isCrafting);
        tag.putFloat("AnimProgress", craftAnimProgress);
        tag.putInt("AnimTicks", animTicks);
    }
}