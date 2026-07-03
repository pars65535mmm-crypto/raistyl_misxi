package com.tyamizumoti.raistyl_misxi;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.item.ItemStack;
import com.tyamizumoti.raistyl_misxi.block.entity.ModBlockEntities;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class CraftingPedestalBlockEntity extends BlockEntity {
    private ItemStack storedItem = ItemStack.EMPTY;

    // --- アニメーションデータ（クライアント同期用） ---
    private float floatHeight = 0.0f;      // 現在の浮遊高さ（0〜1）
    private float rotation = 0.0f;         // 現在の回転角度（度）
    private float targetFloatHeight = 0.0f;
    private float targetRotation = 0.0f;

    // --- クラフト進行中フラグ（trueなら浮遊＆回転アニメーション） ---
    private boolean isAnimating = false;
    private float gatherProgress = 0.0f;   // 0.0〜1.0（中央へ集まる進行度）

    public CraftingPedestalBlockEntity(BlockPos pos, BlockState state) {
    super(ModBlockEntities.CRAFTING_PEDESTAL.get(), pos, state);
}


    public ItemStack getItem() { return this.storedItem; }

    public void setItem(ItemStack stack) {
        this.storedItem = stack;
        this.setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
        // アイテムが置かれたら一旦アニメーションリセット
        this.isAnimating = false;
        this.floatHeight = 0.0f;
        this.gatherProgress = 0.0f;
    }

    // --- アニメーションデータの更新（サーバーからクライアントへ同期） ---
    public void setAnimationData(boolean animating, float progress, float height, float rot) {
        this.isAnimating = animating;
        this.gatherProgress = progress;
        this.floatHeight = height;
        this.rotation = rot;
    }

    // --- ゲッター（レンダラー用） ---
    public float getFloatHeight() { return floatHeight; }
    public float getRotation() { return rotation; }
    public float getGatherProgress() { return gatherProgress; }
    public boolean isAnimating() { return isAnimating; }

    // --- 毎tick呼ばれる（サーバー側での状態更新） ---
    public static void tick(Level level, BlockPos pos, BlockState state, CraftingPedestalBlockEntity entity) {
        if (level.isClientSide()) return;

        // ここでは特に何もしない（MasterOutputBlockEntityが制御する）
        // ただし、アニメーションデータはMasterOutputBlockEntityからsetAnimationDataで更新される
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        this.storedItem = ItemStack.of(tag.getCompound("StoredItem"));
        this.floatHeight = tag.getFloat("FloatHeight");
        this.rotation = tag.getFloat("Rotation");
        this.isAnimating = tag.getBoolean("IsAnimating");
        this.gatherProgress = tag.getFloat("GatherProgress");
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("StoredItem", this.storedItem.save(new CompoundTag()));
        tag.putFloat("FloatHeight", this.floatHeight);
        tag.putFloat("Rotation", this.rotation);
        tag.putBoolean("IsAnimating", this.isAnimating);
        tag.putFloat("GatherProgress", this.gatherProgress);
    }

    @Override
    public CompoundTag getUpdateTag() { 
        CompoundTag tag = super.getUpdateTag();
        tag.put("StoredItem", this.storedItem.save(new CompoundTag()));
        tag.putFloat("FloatHeight", this.floatHeight);
        tag.putFloat("Rotation", this.rotation);
        tag.putBoolean("IsAnimating", this.isAnimating);
        tag.putFloat("GatherProgress", this.gatherProgress);
        return tag;
    }
}