package com.tyamizumoti.raistyl_misxi.screen;

import com.tyamizumoti.raistyl_misxi.ModBlocks;
import com.tyamizumoti.raistyl_misxi.block.entity.TimeCollectorBlockEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import com.tyamizumoti.raistyl_misxi.RaistylMisxi;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.SlotItemHandler;

public class TimeCollectorMenu extends AbstractContainerMenu {
    public final TimeCollectorBlockEntity blockEntity;
    private final Level level;
    private final ContainerData data;

    // クライアント用コンストラクタ
    public TimeCollectorMenu(int windowId, Inventory inv, FriendlyByteBuf extraData) {
        this(windowId, inv, inv.player.level().getBlockEntity(extraData.readBlockPos()), new SimpleContainerData(1));
    }

    // サーバー用コンストラクタ
    public TimeCollectorMenu(int windowId, Inventory inv, BlockEntity entity, ContainerData data) {
        super(ModMenuTypes.TIME_COLLECTOR_MENU.get(), windowId);
        checkContainerDataCount(data, 1);
        this.blockEntity = (TimeCollectorBlockEntity) entity;
        this.level = inv.player.level();
        this.data = data;

        addPlayerInventory(inv);
        addPlayerHotbar(inv);

        int slotStartX = 104 + 1; 
        int slotStartY = 57 + 1;

        // タグの定義をJava側で読み込む呪文
        net.minecraft.tags.TagKey<net.minecraft.world.item.Item> UPGRADE_TAG = 
            net.minecraft.tags.ItemTags.create(new ResourceLocation("raistyl_misxi", "upgrades"));

    this.blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent(handler -> {
            for (int i = 0; i < 3; i++) {
                this.addSlot(new SlotItemHandler(handler, i, slotStartX + (i * 18), slotStartY) {
                    @Override
                    public boolean mayPlace(ItemStack stack) {
                        // 🔑 タグを一切使わず、登録されているアイテムオブジェクトを直接チェックするぜ！
                        // これなら読み込み順に関係なく、100%確実にスロットに入れることができます。
                        return stack.is(com.tyamizumoti.raistyl_misxi.RaistylMisxi.UPGRADE_BASE.get()) || 
                               stack.is(com.tyamizumoti.raistyl_misxi.RaistylMisxi.UPGRADE_SPEED.get()) || 
                               stack.is(com.tyamizumoti.raistyl_misxi.RaistylMisxi.UPGRALANK.get());
                    }
                });
            }
        });

        addDataSlots(data);
    }

    public int getFluidAmount() {
        return this.data.get(0);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        // 工業MODのシフトクリック処理（コンパイルを通すための最低限の記述。後で拡張可能）
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();
            if (index < 36) { // プレイヤーインベントリからマシンのスロットへ
                if (!this.moveItemStackTo(itemstack1, 36, 39, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(itemstack1, 0, 36, true)) { // マシンからインベントリへ
                return ItemStack.EMPTY;
            }
            if (itemstack1.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }
        return itemstack;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(ContainerLevelAccess.create(level, blockEntity.getBlockPos()), player, ModBlocks.TIME_COLLECTOR.get());
    }

    private void addPlayerInventory(Inventory playerInventory) {
        for (int i = 0; i < 3; ++i) {
            for (int l = 0; l < 9; ++l) {
                this.addSlot(new Slot(playerInventory, l + i * 9 + 9, 8 + l * 18, 84 + i * 18));
            }
        }
    }

    private void addPlayerHotbar(Inventory playerInventory) {
        for (int i = 0; i < 9; ++i) {
            this.addSlot(new Slot(playerInventory, i, 8 + i * 18, 142));
        }
    }
}