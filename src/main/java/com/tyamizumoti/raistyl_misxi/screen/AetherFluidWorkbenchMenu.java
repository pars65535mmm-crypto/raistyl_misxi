package com.tyamizumoti.raistyl_misxi.screen;

import com.tyamizumoti.raistyl_misxi.block.entity.AetherFluidWorkbenchBlockEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.SlotItemHandler;

public class AetherFluidWorkbenchMenu extends AbstractContainerMenu {
    public final AetherFluidWorkbenchBlockEntity blockEntity;
    private final Level level;
    private final ContainerData data;

    public AetherFluidWorkbenchMenu(int pContainerId, Inventory inv, FriendlyByteBuf extraData) {
        this(pContainerId, inv, inv.player.level().getBlockEntity(extraData.readBlockPos()), new SimpleContainerData(4));
    }

    public AetherFluidWorkbenchMenu(int pContainerId, Inventory inv, BlockEntity entity, ContainerData data) {
        super(ModMenuTypes.AETHER_FLUID_WORKBENCH_MENU.get(), pContainerId);
        checkContainerDataCount(data, 4);
        this.blockEntity = (AetherFluidWorkbenchBlockEntity) entity;
        this.level = inv.player.level();
        this.data = data;

        addDataSlots(data);

        this.blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent(handler -> {
            int startX = 30; 
            int startY = 17;
            for (int row = 0; row < 3; row++) {
                for (int col = 0; col < 3; col++) {
                    this.addSlot(new SlotItemHandler(handler, col + row * 3, startX + col * 18, startY + row * 18));
                }
            }
            this.addSlot(new SlotItemHandler(handler, 9, 124, 35) {
                @Override
                public boolean mayPlace(ItemStack stack) {
                    return false;
                }
            });
        });

        layoutPlayerInventorySlots(inv, 8, 84);
    }

    public int getProgress() { return this.data.get(0); }
    public int getMaxProgress() { return this.data.get(1); }
    public int getEnergy() { return this.data.get(2); }
    public int getFluidAmount() { return this.data.get(3); }

    public int getScaledProgress() {
        int progress = this.getProgress();
        int maxProgress = this.getMaxProgress();
        return maxProgress != 0 && progress != 0 ? progress * 24 / maxProgress : 0;
    }

    @Override
    public boolean stillValid(Player pPlayer) {
        return stillValid(ContainerLevelAccess.create(level, blockEntity.getBlockPos()), pPlayer, blockEntity.getBlockState().getBlock());
    }

    @Override
    public ItemStack quickMoveStack(Player playerIn, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();
            if (index < 10) {
                if (!this.moveItemStackTo(itemstack1, 10, 46, true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                if (!this.moveItemStackTo(itemstack1, 0, 9, false)) {
                    return ItemStack.EMPTY;
                }
            }

            if (itemstack1.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }
        return itemstack;
    }

    private void layoutPlayerInventorySlots(Inventory playerInventory, int startX, int startY) {
        for (int moveY = 0; moveY < 3; moveY++) {
            for (int moveX = 0; moveX < 9; moveX++) {
                this.addSlot(new Slot(playerInventory, moveX + moveY * 9 + 9, startX + moveX * 18, startY + moveY * 18));
            }
        }
        for (int moveX = 0; moveX < 9; moveX++) {
            this.addSlot(new Slot(playerInventory, moveX, startX + moveX * 18, startY + 58));
        }
    }
}