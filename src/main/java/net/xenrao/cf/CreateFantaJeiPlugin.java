package net.xenrao.cf.compat.jei;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import net.xenrao.cf.CreateFantaMod;
import net.xenrao.cf.ModRecipes;
import net.xenrao.cf.ModRegistry;
import net.xenrao.cf.recipe.FilteringRecipe;
import net.xenrao.cf.recipe.GasConverterHeatingRecipe;

import java.util.List;

@JeiPlugin
public class CreateFantaJeiPlugin implements IModPlugin {

    private static final ResourceLocation PLUGIN_ID =
        new ResourceLocation(CreateFantaMod.MODID, "jei_plugin");

    @Override
    public ResourceLocation getPluginUid() {
        return PLUGIN_ID;
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        var gui = registration.getJeiHelpers().getGuiHelper();
        registration.addRecipeCategories(
            new HeatingRecipeCategory(gui),
            new FilterRecipeCategory(gui)
        );
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        var level = Minecraft.getInstance().level;
        if (level == null) return;

        // Heating
        List<GasConverterHeatingRecipe> heatingRecipes =
            level.getRecipeManager().getAllRecipesFor(ModRecipes.HEATING_TYPE.get());
        registration.addRecipes(HeatingRecipeCategory.TYPE, heatingRecipes);

        // Filtering
        List<FilteringRecipe> filteringRecipes =
            level.getRecipeManager().getAllRecipesFor(ModRecipes.FILTERING_TYPE.get());
        registration.addRecipes(FilterRecipeCategory.TYPE, filteringRecipes);
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(
            new ItemStack(ModRegistry.GAS_CONVERTER_BLOCK.get()),
            HeatingRecipeCategory.TYPE
        );
        registration.addRecipeCatalyst(
            new ItemStack(ModRegistry.FILTER_BLOCK.get()),
            FilterRecipeCategory.TYPE
        );
    }
}