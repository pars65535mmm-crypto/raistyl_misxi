package com.tyamizumoti.raistyl_misxi.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.tyamizumoti.raistyl_misxi.RaistylMisxi;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class TimeCollectorScreen extends AbstractContainerScreen<TimeCollectorMenu> {
    private static final ResourceLocation TEXTURE = 
        new ResourceLocation(RaistylMisxi.MODID, "textures/gui/time_collector_gui.png");

    public TimeCollectorScreen(TimeCollectorMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 176;
        this.imageHeight = 176;
    }

    @Override
    protected void init() {
        super.init();
        this.titleLabelX = (this.imageWidth - this.font.width(this.title)) / 2; // タイトルを中央寄せ
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, TEXTURE);
        
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        // 🖼️ メインGUIの背景描画（176x176の部分を切り出し）
        guiGraphics.blit(TEXTURE, x, y, 0, 0, imageWidth, imageHeight);

        // 🧪 液体バーの描画処理（1の指定座標：左75、上12から、18x46のサイズ）
        int fluidAmount = this.menu.getFluidAmount();
        int maxFluid = 46; 
        int progress = (int) (fluidAmount * (maxFluid / 1000.0)); // 1000mB満タン計算

        if (progress > 0) {
    guiGraphics.fill(x + 76, y + 59 - progress, x + 94, y + 59, 0xBF9900FF);
    }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, delta);
        renderTooltip(guiGraphics, mouseX, mouseY);
        
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;
        
        // 🎯 マウス判定も 1px 右・下にずらした位置に完全同期！
        if (mouseX >= x + 76 && mouseX <= x + 94 && mouseY >= y + 13 && mouseY <= y + 59) {
            guiGraphics.renderTooltip(this.font, Component.literal(this.menu.getFluidAmount() + " / 1000 mB"), mouseX, mouseY);
        }
    }
}