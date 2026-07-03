package com.tyamizumoti.raistyl_misxi.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Containers;
import net.minecraft.world.SimpleContainer;
// ⭕ これらが足りていないためエラーが起きています
import com.tyamizumoti.raistyl_misxi.recipe.AetherFluidWorkbenchRecipe;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import com.tyamizumoti.raistyl_misxi.screen.AetherFluidWorkbenchMenu;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.EnergyStorage;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.tyamizumoti.raistyl_misxi.RaistylMisxi; // メインクラスのBlockEntityType登録場所に合わせてな！

public class AetherFluidWorkbenchBlockEntity extends BlockEntity implements MenuProvider {
    
    // 📦 Item Handler: 0〜8がクラフト、9が出力スロット
    public final ItemStackHandler itemHandler = new ItemStackHandler(10) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }
    };

    // 🧪 Fluid Tank: 4000mB容量の時流流体タンク
    public final FluidTank fluidTank = new FluidTank(4000) {
        @Override
        protected void onContentsChanged() {
            setChanged();
        }
    };

    // ⚡ Energy Storage: 100,000 FE容量、毎tick最大1000FE搬入可能
     public final EnergyStorage energyStorage = new EnergyStorage(10000000, 1000000, 1000000) {
        @Override
        public int receiveEnergy(int maxReceive, boolean simulate) {
            int received = super.receiveEnergy(maxReceive, simulate);
            if (received > 0) setChanged();
            return received;
        }
    };

    // 外部（パイプなど）と接続するためのLazyOptional
    private LazyOptional<IItemHandler> lazyItemHandler = LazyOptional.empty();
    private LazyOptional<IFluidHandler> lazyFluidHandler = LazyOptional.empty();
    private LazyOptional<IEnergyStorage> lazyEnergyHandler = LazyOptional.empty();

    // GUI同期用のデータ（進行度、最大進行度、流体量、電気量などをMenuに送る）
    protected final ContainerData data;
    private int progress = 0;
    private int maxProgress = 20; // 1秒(20tick)で加工完了など

    public AetherFluidWorkbenchBlockEntity(BlockPos pPos, BlockState pBlockState) {
        // 👇 引数のBlockEntityTypeは、後でメインクラスで登録する変数名に合わせて書き換えてな！
        super(RaistylMisxi.AETHER_FLUID_WORKBENCH_BE.get(), pPos, pBlockState);
        this.data = new ContainerData() {
            @Override
            public int get(int pIndex) {
                return switch (pIndex) {
                    case 0 -> AetherFluidWorkbenchBlockEntity.this.progress;
                    case 1 -> AetherFluidWorkbenchBlockEntity.this.maxProgress;
                    case 2 -> AetherFluidWorkbenchBlockEntity.this.energyStorage.getEnergyStored();
                    case 3 -> AetherFluidWorkbenchBlockEntity.this.fluidTank.getFluidAmount();
                    default -> 0;
                };
            }

            @Override
            public void set(int pIndex, int pValue) {
                switch (pIndex) {
                    case 0 -> AetherFluidWorkbenchBlockEntity.this.progress = pValue;
                    case 1 -> AetherFluidWorkbenchBlockEntity.this.maxProgress = pValue;
                }
            }

            @Override
            public int getCount() {
                return 4;
            }
        };
    }

    @Override
    public Component getDisplayName() {
        return Component.literal("エーテル流体作業台");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int pContainerId, Inventory pPlayerInventory, Player pPlayer) {
        // ✨ コメントアウトを外して、ちゃんとMenuを生成して返すようにするぜ！
        return new AetherFluidWorkbenchMenu(pContainerId, pPlayerInventory, this, this.data);
    }

    // 外部からの能力（アイテム・流体・電力のパイプ）のアクセス許可
    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER) return lazyItemHandler.cast();
        if (cap == ForgeCapabilities.FLUID_HANDLER) return lazyFluidHandler.cast();
        if (cap == ForgeCapabilities.ENERGY) return lazyEnergyHandler.cast();
        return super.getCapability(cap, side);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        lazyItemHandler = LazyOptional.of(() -> itemHandler);
        lazyFluidHandler = LazyOptional.of(() -> fluidTank);
        lazyEnergyHandler = LazyOptional.of(() -> energyStorage);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        lazyItemHandler.invalidate();
        lazyFluidHandler.invalidate();
        lazyEnergyHandler.invalidate();
    }

    // --- 💾 データの保存と読み込み ---
    @Override
    protected void saveAdditional(CompoundTag pTag) {
        pTag.put("inventory", itemHandler.serializeNBT());
        pTag.putInt("progress", progress);
        pTag.put("energy", energyStorage.serializeNBT());
        fluidTank.writeToNBT(pTag);
        super.saveAdditional(pTag);
    }

    @Override
    public void load(CompoundTag pTag) {
        super.load(pTag);
        itemHandler.deserializeNBT(pTag.getCompound("inventory"));
        progress = pTag.getInt("progress");
        energyStorage.receiveEnergy(pTag.getInt("energy"), false); // 簡易復元
        fluidTank.readFromNBT(pTag);
    }

    // ブロックが壊れたときにアイテムをバラ撒く用
    public void drops() {
        SimpleContainer inventory = new SimpleContainer(itemHandler.getSlots());
        for (int i = 0; i < itemHandler.getSlots(); i++) {
            inventory.setItem(i, itemHandler.getStackInSlot(i));
        }
        Containers.dropContents(this.level, this.worldPosition, inventory);
    }

        // ⚡ 起動するだけで毎Tick消費する狂気の電力（40k FE/t）
    private static final int ENERGY_USAGE = 40000;

    public static void tick(Level pLevel, BlockPos pPos, BlockState pState, AetherFluidWorkbenchBlockEntity pBlockEntity) {
        if (pLevel.isClientSide()) return;

        // 🛑 常時発動型：内部に電力が40k FE以上あるかチェック
        if (pBlockEntity.energyStorage.getEnergyStored() >= ENERGY_USAGE) {
            // 💡 何をしていなくても、毎Tick問答無用で40k FEを消滅させる！
            pBlockEntity.energyStorage.extractEnergy(ENERGY_USAGE, false);
            
            // 電力が供給されている状態（安定状態）の時だけ、レシピのクラフトが進行する
            if (pBlockEntity.hasRecipe()) {
                pBlockEntity.progress++;
                if (pLevel.random.nextFloat() < 0.5f) {
                    double px = pPos.getX() + 0.5 + (pLevel.random.nextDouble() - 0.5) * 0.6;
                    double py = pPos.getY() + 0.8 + (pLevel.random.nextDouble() - 0.5) * 0.4;
                    double pz = pPos.getZ() + 0.5 + (pLevel.random.nextDouble() - 0.5) * 0.6;
                    
                    // サーバー側から全プレイヤーへ粒子をパケット送信する（電気のバチバチ感には「ELECTRIC_SPARK」が最適！）
                    ((net.minecraft.server.level.ServerLevel) pLevel).sendParticles(
                        net.minecraft.core.particles.ParticleTypes.ELECTRIC_SPARK, // 粒子タイプ
                        px, py, pz,   // 発生座標
                        2,            // 1回に出す個数
                        0.0, 0.0, 0.0,// 散らばる速度
                        0.02          // 粒子の移動スピード
                    );
                }
                setChanged(pLevel, pPos, pState);

                // 20Tick（1秒）経過でガシャコン！
                if (pBlockEntity.progress >= pBlockEntity.maxProgress) {
                    pBlockEntity.craftItem();
                    pBlockEntity.progress = 0;
                }
            } else {
                pBlockEntity.progress = 0; 
            }
        } else {
            // 🚫 電源喪失！時空維持システムが停止し、恐ろしいペナルティが発生する
            if (pBlockEntity.progress > 0) {
                // 1秒でリセットされるどころか、電力が切れた瞬間に一瞬で進行度が0に戻るアホ仕様
                pBlockEntity.progress = 0; 
                setChanged(pLevel, pPos, pState);
                
                // オプション：ここに雷を落としたり、爆発エフェクト（音だけ）を鳴らすとさらにアホっぽさが増します
            }
        }
    }


    // 🔬 現在スロットに置かれているアイテムと流体から、対応するレシピがあるか探す
    private boolean hasRecipe() {
        if (this.level == null) return false;

        // ForgeのItemStackHandler（0〜8スロット）をバニラのSimpleContainer（全9マス）に詰め替える
        SimpleContainer craftInventory = new SimpleContainer(9);
        for (int i = 0; i < 9; i++) {
            craftInventory.setItem(i, this.itemHandler.getStackInSlot(i));
        }

        // マイクラのレシピマネージャーから、この作業台用のレシピを探して一致するものを取得する
        var recipeOpt = this.level.getRecipeManager()
                .getRecipeFor(AetherFluidWorkbenchRecipe.Type.INSTANCE, craftInventory, this.level);

        if (recipeOpt.isPresent()) {
            AetherFluidWorkbenchRecipe recipe = recipeOpt.get();

            // 🧪 液体タンクのチェック：レシピ要求の液体と同じ種類で、かつ量が足りているか
            FluidStack requiredFluid = recipe.getInputFluid();
            if (this.fluidTank.getFluid().getFluid() != requiredFluid.getFluid() || 
                this.fluidTank.getFluidAmount() < requiredFluid.getAmount()) {
                return false;
            }

            // 📦 出力スロット（スロット番号9）のチェック：完成品を入れられる空きがあるか
            ItemStack resultItem = recipe.getResultItem(this.level.registryAccess());
            ItemStack outputSlot = this.itemHandler.getStackInSlot(9);

            // 出力スロットが空、または同じアイテムでスタック上限に余裕があればOK
            return outputSlot.isEmpty() || 
                   (outputSlot.is(resultItem.getItem()) && outputSlot.getCount() + resultItem.getCount() <= outputSlot.getMaxStackSize());
        }

        return false;
    }

    // 🔨 材料（ウラン1個と液体）を消費して、不完全なクロノインゴットを成果物スロットに生成する
    private void craftItem() {
        if (this.level == null) return;

        SimpleContainer craftInventory = new SimpleContainer(9);
        for (int i = 0; i < 9; i++) {
            craftInventory.setItem(i, this.itemHandler.getStackInSlot(i));
        }

        var recipeOpt = this.level.getRecipeManager()
                .getRecipeFor(AetherFluidWorkbenchRecipe.Type.INSTANCE, craftInventory, this.level);

        if (recipeOpt.isPresent()) {
            AetherFluidWorkbenchRecipe recipe = recipeOpt.get();
            ItemStack resultItem = recipe.getResultItem(this.level.registryAccess());

            // 1. クラフト格子（0〜8番スロット）の中を走査し、ウラン（レシピ一致アイテム）を1個だけ消費する
            for (int i = 0; i < 9; i++) {
                ItemStack stack = this.itemHandler.getStackInSlot(i);
                if (!stack.isEmpty() && recipe.getInputIngredient().test(stack)) {
                    this.itemHandler.extractItem(i, 1, false);
                    break; // 1個消したらループを抜ける
                }
            }

            // 2. 液体タンクからレシピ指定の量（1000mBなど）を消費する
            this.fluidTank.drain(recipe.getInputFluid().getAmount(), net.minecraftforge.fluids.capability.IFluidHandler.FluidAction.EXECUTE);

            // 3. 出力スロット（9番）に完成品を入れる
            ItemStack outputSlot = this.itemHandler.getStackInSlot(9);
            if (outputSlot.isEmpty()) {
                this.itemHandler.setStackInSlot(9, resultItem.copy());
            } else {
                outputSlot.grow(resultItem.getCount());
            }

            setChanged();
        }
    }
}