package com.tyamizumoti.raistyl_misxi.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import com.tyamizumoti.raistyl_misxi.screen.TimeCollectorMenu;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import com.tyamizumoti.raistyl_misxi.RaistylMisxi;
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
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TimeCollectorBlockEntity extends BlockEntity implements MenuProvider {
    // ⚙️ 3マスのアップグレードスロット
    private final ItemStackHandler itemHandler = new ItemStackHandler(3) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }
    };

    // ⏳ 1000mb最大容量の時流流体タンク（仮にバニラの水で代用、後で独自流体に変えられる）
    private final FluidTank fluidTank = new FluidTank(1000) {
        @Override
        protected void onContentsChanged() {
            setChanged();
        }
    };

    private LazyOptional<IItemHandler> lazyItemHandler = LazyOptional.empty();
    private LazyOptional<FluidTank> lazyFluidHandler = LazyOptional.empty();

    // クライアント同期用のデータ（流体量などをGUIに送る用）
    protected final ContainerData data;
    private int progressTicks = 0;

    public TimeCollectorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.TIME_COLLECTOR.get(), pos, state);
        this.data = new ContainerData() {
            @Override
            public int get(int index) {
                return switch (index) {
                    case 0 -> TimeCollectorBlockEntity.this.fluidTank.getFluidAmount();
                    default -> 0;
                };
            }
            @Override
            public void set(int index, int value) {
                // クライアント側での同期用
            }
            @Override
            public int getCount() {
                return 1;
            }
        };
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.raistyl_misxi.time_collector");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new TimeCollectorMenu(id, inventory, this, this.data);
    }

// クラス内の変数宣言の並びに追記
    private int timer = 0;

        public static void tick(Level level, BlockPos pos, BlockState state, TimeCollectorBlockEntity pEntity) {
        if (level.isClientSide()) return;

        int speedUpgradeCount = 0;
        int rankUpgradeCount = 0;

        // 🔍 3マス個のスロットを調べて、それぞれのアプグレ枚数を数える
        for (int i = 0; i < 3; i++) {
            ItemStack stackInSlot = pEntity.itemHandler.getStackInSlot(i);
            
            // ⚡ スピードアップグレード（既存）
            if (stackInSlot.is(RaistylMisxi.UPGRADE_SPEED.get())) {
                speedUpgradeCount += stackInSlot.getCount();
            }
            // 🌟 ランクアップグレード（新しく作った upgralank を指定！）
            else if (stackInSlot.is(com.tyamizumoti.raistyl_misxi.RaistylMisxi.UPGRALANK.get())) {
                rankUpgradeCount += stackInSlot.getCount();
            }
        }

        // 📈 ランクアプグレの合計枚数から「現在の流体ランク」を判定する
        int currentRank = 1; // 未装着はランク1
        if (rankUpgradeCount >= 192) {
            currentRank = 5; // MAX（3スタックフル）
        } else if (rankUpgradeCount >= 64) {
            currentRank = 4; // 1スタック以上
        } else if (rankUpgradeCount >= 8) {
            currentRank = 3; // 8個以上
        } else if (rankUpgradeCount >= 1) {
            currentRank = 2; // 1個以上
        }

        // ⏳ ランクに応じた「時間ペナルティ（必要カウント数）」の計算
        // 基本は 1200（1分）。ランクが上がるごとに必要時間が 2倍、4倍、8倍、16倍 に増えていく設定です。
        int baseRequiredTime = 1200;
        switch (currentRank) {
            case 2 -> baseRequiredTime = 1200 * 2;  // 2分
            case 3 -> baseRequiredTime = 1200 * 4;  // 4分
            case 4 -> baseRequiredTime = 1200 * 8;  // 8分
            case 5 -> baseRequiredTime = 1200 * 16; // 16分
        }

        // ⏱️ タイマーの進行
        // スピードアプグレ1枚につき、進む速度が+1ブースト。
        pEntity.timer += (1 + speedUpgradeCount);

        // ランクに応じた目標カウント（時間）に達したら流体を生成！
        if (pEntity.timer >= baseRequiredTime) {
            pEntity.timer = 0; // タイマーリセット

            // 🔮 将来の拡張ポイント
            // 今はすべて同じ「SOURCE_TIME_FLUID（時流流体）」を生成します。
            // 将来、流体が増えたら以下のように currentRank で分岐させて登録名（ModFluids.XXX）を変えるだけでOKです！
            var fluidToGenerate = com.tyamizumoti.raistyl_misxi.fluid.ModFluids.SOURCE_TIME_FLUID.get();
            
            /* ※ 将来の上位流体の切り替え用メモ
            switch (currentRank) {
                case 2 -> fluidToGenerate = ModFluids.SOURCE_SPACE_FLUID.get(); // 時空流体（仮）
                case 3 -> fluidToGenerate = ModFluids.SOURCE_RANK3_FLUID.get();
                // ...
            }
            */

            pEntity.fluidTank.fill(new net.minecraftforge.fluids.FluidStack(fluidToGenerate, 1), 
                    net.minecraftforge.fluids.capability.IFluidHandler.FluidAction.EXECUTE);
        }
    }


    // 💾 データのセーブ・ロード
    @Override
    protected void saveAdditional(CompoundTag nbt) {
        nbt.put("inventory", itemHandler.serializeNBT());
        nbt = fluidTank.writeToNBT(nbt);
        nbt.putInt("progress", progressTicks);
        super.saveAdditional(nbt);
    }

    @Override
    public void load(CompoundTag nbt) {
        super.load(nbt);
        itemHandler.deserializeNBT(nbt.getCompound("inventory"));
        fluidTank.readFromNBT(nbt);
        progressTicks = nbt.getInt("progress");
    }

    // 外部（配管とかホッパー）から触れるようにする設定
    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if(cap == ForgeCapabilities.ITEM_HANDLER) return lazyItemHandler.cast();
        if(cap == ForgeCapabilities.FLUID_HANDLER) return lazyFluidHandler.cast();
        return super.getCapability(cap, side);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        lazyItemHandler = LazyOptional.of(() -> itemHandler);
        lazyFluidHandler = LazyOptional.of(() -> fluidTank);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        lazyItemHandler.invalidate();
        lazyFluidHandler.invalidate();
    }
}