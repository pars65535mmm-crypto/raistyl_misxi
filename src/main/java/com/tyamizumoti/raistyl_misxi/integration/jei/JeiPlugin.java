package com.tyamizumoti.raistyl_misxi.integration.jei;

import com.tyamizumoti.raistyl_misxi.RaistylMisxi;
import com.tyamizumoti.raistyl_misxi.recipe.AetherFluidWorkbenchRecipe;
import mezz.jei.api.IModPlugin;
// ❌【削除】名前がダブってエラーの原因になっていた「import mezz.jei.api.JeiPlugin;」を完全に消去しました！
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.forge.ForgeTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraftforge.fluids.FluidStack;

import java.util.List;

// ⭕ アノテーションをフルパス（mezz.jei.api...）で書くことで、インポートなしで安全にJEIにプラグインを認識させます！
@mezz.jei.api.JeiPlugin 
public class JeiPlugin implements IModPlugin {
    
    public static final RecipeType<AetherFluidWorkbenchRecipe> WORKBENCH_TYPE = 
            RecipeType.create(RaistylMisxi.MODID, "aether_fluid_workbench", AetherFluidWorkbenchRecipe.class);

    @Override
    public ResourceLocation getPluginUid() {
        return new ResourceLocation(RaistylMisxi.MODID, "jei_plugin");
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(new WorkbenchRecipeCategory(registration.getJeiHelpers().getGuiHelper()));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        RecipeManager rm = Minecraft.getInstance().level.getRecipeManager();
        List<AetherFluidWorkbenchRecipe> recipes = rm.getAllRecipesFor(AetherFluidWorkbenchRecipe.Type.INSTANCE);
        registration.addRecipes(WORKBENCH_TYPE, recipes);
    }

    private static class WorkbenchRecipeCategory implements IRecipeCategory<AetherFluidWorkbenchRecipe> {
        private final IDrawable background;
        private final IDrawable icon;
        private final Component localizedName;

        public WorkbenchRecipeCategory(IGuiHelper helper) {
            ResourceLocation texture = new ResourceLocation(RaistylMisxi.MODID, "textures/gui/aether_fluid_workbench_gui.png");
            this.background = helper.createDrawable(texture, 5, 15, 140, 60);
            this.icon = helper.createBlankDrawable(16, 16);
            this.localizedName = Component.literal("エーテル流体作業台");
        }

        @Override
        public RecipeType<AetherFluidWorkbenchRecipe> getRecipeType() { return WORKBENCH_TYPE; }
        @Override
        public Component getTitle() { return localizedName; }
        @Override
        public IDrawable getBackground() { return background; }
        @Override
        public IDrawable getIcon() { return icon; }

        @Override
        public void setRecipe(IRecipeLayoutBuilder builder, AetherFluidWorkbenchRecipe recipe, IFocusGroup focuses) {
            FluidStack fluidStack = recipe.getInputFluid();
            builder.addSlot(RecipeIngredientRole.INPUT, 2, 2)
                    .addIngredient(ForgeTypes.FLUID_STACK, fluidStack)
                    .setFluidRenderer(fluidStack.getAmount(), false, 16, 52);

            builder.addSlot(RecipeIngredientRole.INPUT, 51, 20)
                    .addIngredients(recipe.getInputIngredient());

            ItemStack result = recipe.getResultItem(Minecraft.getInstance().level.registryAccess());
            builder.addSlot(RecipeIngredientRole.OUTPUT, 116, 20)
                    .addItemStack(result);
        }
    }
}
