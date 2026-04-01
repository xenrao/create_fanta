package net.xenrao.cf.compat.jei;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.simibubi.create.compat.jei.category.animations.AnimatedKinetics;

import mezz.jei.api.forge.ForgeTypes;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

import net.xenrao.cf.CreateFantaMod;
import net.xenrao.cf.ModRegistry;
import net.xenrao.cf.block.FilterBlock;
import net.xenrao.cf.block.FilterPartialModels;
import net.xenrao.cf.recipe.FilteringRecipe;

public class FilterRecipeCategory implements IRecipeCategory<FilteringRecipe> {

    public static final ResourceLocation UID = new ResourceLocation(CreateFantaMod.MODID, "filtering");
    public static final RecipeType<FilteringRecipe> TYPE = new RecipeType<>(UID, FilteringRecipe.class);

    private final IDrawable background;
    private final IDrawable icon;

    private static final ResourceLocation CREATE_WIDGETS =
        new ResourceLocation("create", "textures/gui/jei/widgets.png");

    int slot1x = 30;
    int slot2x = 133;
    int sloty  = 15;

    public FilterRecipeCategory(IGuiHelper helper) {
        this.background = helper.createBlankDrawable(180, 45);
        this.icon = helper.createDrawableIngredient(VanillaTypes.ITEM_STACK,
            new ItemStack(ModRegistry.FILTER_BLOCK.get()));
    }

    @Override public RecipeType<FilteringRecipe> getRecipeType() { return TYPE; }
    @Override public Component getTitle() { return Component.translatable("create_fanta.jei.filtering"); }
    @Override public IDrawable getBackground() { return background; }
    @Override public IDrawable getIcon() { return icon; }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, FilteringRecipe recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, slot1x, sloty)
            .addIngredient(ForgeTypes.FLUID_STACK, recipe.getIngredientFluid())
            .setFluidRenderer(recipe.getIngredientFluid().getAmount(), false, 16, 16);

        builder.addSlot(RecipeIngredientRole.OUTPUT, slot2x, sloty)
            .addIngredient(ForgeTypes.FLUID_STACK, recipe.getResultFluid())
            .setFluidRenderer(recipe.getResultFluid().getAmount(), false, 16, 16);
    }

    @Override
    public void draw(FilteringRecipe recipe, IRecipeSlotsView slots,
                     GuiGraphics gfx, double mouseX, double mouseY) {

                     	        // Widgets
        gfx.blit(CREATE_WIDGETS, 54, sloty + 4, 19, 0, 72, 9, 256, 256);
        gfx.blit(CREATE_WIDGETS, slot1x - 1, sloty - 1, 0, 0, 18, 18, 256, 256);
        gfx.blit(CREATE_WIDGETS, slot2x - 1, sloty - 1, 0, 0, 18, 18, 256, 256);
        
        PoseStack ms = gfx.pose();
		
        ms.pushPose();
        ms.translate(86, 30, 200);

        float pitch = -15f;
        float yaw   = -30f;
        ms.mulPose(Axis.XP.rotationDegrees(pitch));
        ms.mulPose(Axis.YP.rotationDegrees(yaw));

        final int SCALE = 24;

        BlockState state = ModRegistry.FILTER_BLOCK.get().defaultBlockState()
            .setValue(FilterBlock.FACING, Direction.EAST)
            .setValue(FilterBlock.HAS_FILTER, true);

        AnimatedKinetics.defaultBlockElement(state)
            .scale(SCALE)
            .render(gfx);

        float angle = AnimatedKinetics.getCurrentAngle() * 16f;

        AnimatedKinetics.defaultBlockElement(FilterPartialModels.FILTER_COG)
            .rotateBlock(0, angle, -90)
            .scale(SCALE)
            .render(gfx);

        ms.popPose();



    }
}