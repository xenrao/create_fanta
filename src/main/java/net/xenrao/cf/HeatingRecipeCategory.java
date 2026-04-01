package net.xenrao.cf.compat.jei;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.compat.jei.category.animations.AnimatedKinetics;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.forge.ForgeTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;

import net.createmod.catnip.gui.element.GuiGameElement;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluids;

import net.xenrao.cf.CreateFantaMod;
import net.xenrao.cf.ModRegistry;
import net.xenrao.cf.recipe.GasConverterHeatingRecipe;

import java.util.Arrays;
import java.util.List;

public class HeatingRecipeCategory implements IRecipeCategory<GasConverterHeatingRecipe> {

    public static final ResourceLocation UID = new ResourceLocation(CreateFantaMod.MODID, "heating");
    public static final RecipeType<GasConverterHeatingRecipe> TYPE =
        new RecipeType<>(UID, GasConverterHeatingRecipe.class);

    private final IDrawable background;
    private final IDrawable icon;

    private static final ResourceLocation CREATE_WIDGETS =
        new ResourceLocation("create", "textures/gui/jei/widgets.png");

    int slot1x = 30;
    int slot2x = 134;
    int sloty = 55;

    public HeatingRecipeCategory(IGuiHelper helper) {
        this.background = helper.createBlankDrawable(180, 90);
        this.icon = helper.createDrawableIngredient(VanillaTypes.ITEM_STACK,
            new ItemStack(ModRegistry.GAS_CONVERTER_BLOCK.get()));
    }

    @Override
    public RecipeType<GasConverterHeatingRecipe> getRecipeType() {
        return TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("create_fanta.jei.heating");
    }

    @Override
    public IDrawable getBackground() {
        return background;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, GasConverterHeatingRecipe recipe, IFocusGroup focuses) {

        // Ingredient'taki tüm geçerli item'ları al, count'u ayarla
        List<ItemStack> inputs = Arrays.stream(recipe.getIngredient().getItems())
            .map(stack -> {
                ItemStack copy = stack.copy();
                copy.setCount(recipe.getIngredientCount());
                return copy;
            })
            .toList();

        builder.addSlot(RecipeIngredientRole.INPUT, slot1x, sloty)
            .addIngredients(VanillaTypes.ITEM_STACK, inputs);

        builder.addSlot(RecipeIngredientRole.OUTPUT, slot2x, sloty)
            .addIngredient(ForgeTypes.FLUID_STACK, recipe.getResultFluid())
            .setFluidRenderer(recipe.getResultFluid().getAmount(), false, 16, 16);
    }

    @Override
    public void draw(GasConverterHeatingRecipe recipe, IRecipeSlotsView slots,
                     GuiGraphics gfx, double mouseX, double mouseY) {

        PoseStack ms = gfx.pose();
        ms.pushPose();

        ms.translate(55, 36, 200);

        float pitch = -15f;
        float yaw = 30f;
        ms.mulPose(com.mojang.math.Axis.XP.rotationDegrees(pitch));
        ms.mulPose(com.mojang.math.Axis.YP.rotationDegrees(yaw));

        final int SCALE = 24;
        float angle = AnimatedKinetics.getCurrentAngle();

        AnimatedKinetics
            .defaultBlockElement(AllPartialModels.ENCASED_FAN_INNER)
            .rotateBlock(180, 0, angle * 16)
            .scale(SCALE)
            .render(gfx);

        AnimatedKinetics
            .defaultBlockElement(AllBlocks.ENCASED_FAN.getDefaultState())
            .rotateBlock(0, 180, 0)
            .atLocal(0, 0, 0)
            .scale(SCALE)
            .render(gfx);

        GuiGameElement.of(Fluids.LAVA)
            .scale(SCALE)
            .atLocal(0, 0, 1.5)
            .lighting(AnimatedKinetics.DEFAULT_LIGHTING)
            .render(gfx);

        AnimatedKinetics
            .defaultBlockElement(ModRegistry.GAS_CONVERTER_BLOCK.get().defaultBlockState())
            .atLocal(0, 0, 3)
            .scale(SCALE)
            .render(gfx);

        ms.popPose();

        // İşlem süresi göster
        int time = recipe.getProcessingTime();
        String timeText = (time / 20) + "s";
        gfx.drawString(net.minecraft.client.Minecraft.getInstance().font,
            timeText, 87, sloty + 14, 0x888888, false);

        gfx.blit(CREATE_WIDGETS, 54, sloty + 4, 19, 0, 72, 9, 256, 256);
        gfx.blit(CREATE_WIDGETS, slot1x - 1, sloty - 1, 0, 0, 18, 18, 256, 256);
        gfx.blit(CREATE_WIDGETS, slot2x - 1, sloty - 1, 0, 0, 18, 18, 256, 256);
    }
}