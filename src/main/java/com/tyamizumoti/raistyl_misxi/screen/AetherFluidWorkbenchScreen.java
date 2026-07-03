package com.tyamizumoti.raistyl_misxi.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.tyamizumoti.raistyl_misxi.RaistylMisxi;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class AetherFluidWorkbenchScreen extends AbstractContainerScreen<AetherFluidWorkbenchMenu> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(RaistylMisxi.MODID, "textures/gui/aether_fluid_workbench_gui.png");

    public AetherFluidWorkbenchScreen(AetherFluidWorkbenchMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, pTitle);
        this.imageWidth = 176;
        this.imageHeight = 166;
    }

    @Override
    protected void init() {
        super.init();
        this.titleLabelX = 30;
        this.inventoryLabelX = 8;
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float pPartialTick, int pMouseX, int pMouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, TEXTURE);
        
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;
        guiGraphics.blit(TEXTURE, x, y, 0, 0, imageWidth, imageHeight);

        // ⏱️ クラフト進行度の矢印（溜まっているときだけ描画）
        int progressWidth = menu.getScaledProgress();
        if (progressWidth > 0) {
            guiGraphics.blit(TEXTURE, x + 90, y + 35, 176, 14, progressWidth, 17);
        }

        // 🧪 【修正】流体ゲージの描画
        int gaugeX = x + 5;
        int gaugeY = y + 17;
        int gaugeWidth = 18;
        int gaugeHeight = 50;

        int maxFluid = 4000; // 👈 1のBlockEntityの最大流体容量（mB）に合わせて調整してな！
        int currentFluid = menu.getFluidAmount(); // Menuからエネルギーじゃなく流体量（FluidAmount）を取得！
        int fluidBarHeight = maxFluid != 0 ? (int) ((long) currentFluid * gaugeHeight / maxFluid) : 0;

        if (fluidBarHeight > 0) {
            // 流体の中身のテクスチャ切り出し。GUI画像(176x166)の右側外（X=176, Y=31）などに
            // 1が縦50px分の流体の「満タン画像」を隠しパーツとして描いてくれている想定だぜ！
            int textureX = 176;
            int textureY = 31;
            
            guiGraphics.blit(TEXTURE, 
                gaugeX, 
                gaugeY + (gaugeHeight - fluidBarHeight), 
                textureX, 
                textureY + (gaugeHeight - fluidBarHeight), 
                gaugeWidth, 
                fluidBarHeight
            );
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, delta);
        renderTooltip(guiGraphics, mouseX, mouseY);

        // 🔍 ツールチップも流体表示（mB）に修正！
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        if (mouseX >= x + 5 && mouseX <= x + 23 && mouseY >= y + 17 && mouseY <= y + 67) {
            Component tooltipText = Component.literal("流体量: " + menu.getFluidAmount() + " / 4,000 mB");
            guiGraphics.renderTooltip(this.font, tooltipText, mouseX, mouseY);
        }
    }
}