package net.xenrao.cf.recipe;

import com.google.gson.JsonObject;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

import net.xenrao.cf.ModRecipes;

public class GasConverterHeatingRecipe implements Recipe<Container> {

    private final ResourceLocation id;
    private final Ingredient ingredient;
    private final int ingredientCount;
    private final FluidStack result;
    private final int processingTime;

    public GasConverterHeatingRecipe(ResourceLocation id, Ingredient ingredient, int ingredientCount,
                                    FluidStack result, int processingTime) {
        this.id = id;
        this.ingredient = ingredient;
        this.ingredientCount = ingredientCount;
        this.result = result;
        this.processingTime = processingTime;
    }

    public Ingredient getIngredient() { return ingredient; }
    public int getIngredientCount() { return ingredientCount; }
    public FluidStack getResultFluid() { return result.copy(); }
    public int getProcessingTime() { return processingTime; }

    @Override public boolean matches(Container c, net.minecraft.world.level.Level level) { return false; }

    @Override
    public ItemStack assemble(Container c, RegistryAccess registryAccess) {
        return ItemStack.EMPTY;
    }

    @Override public boolean canCraftInDimensions(int w, int h) { return false; }

    @Override
    public ItemStack getResultItem(RegistryAccess registryAccess) {
        return ItemStack.EMPTY;
    }

    @Override public ResourceLocation getId() { return id; }
    @Override public RecipeSerializer<?> getSerializer() { return ModRecipes.HEATING_SERIALIZER.get(); }
    @Override
	public RecipeType<?> getType() {
	    return ModRecipes.HEATING_TYPE.get();
	}

    public static class Serializer implements RecipeSerializer<GasConverterHeatingRecipe> {

        @Override
        public GasConverterHeatingRecipe fromJson(ResourceLocation id, JsonObject json) {
            Ingredient ing = Ingredient.fromJson(GsonHelper.getAsJsonObject(json, "ingredient"));
            int count = GsonHelper.getAsInt(json, "ingredient_count", 1);

            JsonObject res = GsonHelper.getAsJsonObject(json, "result");
            ResourceLocation fluidId = new ResourceLocation(GsonHelper.getAsString(res, "fluid"));
            int amount = GsonHelper.getAsInt(res, "amount");
            int time = GsonHelper.getAsInt(json, "processing_time", 100);

            var fluid = ForgeRegistries.FLUIDS.getValue(fluidId);
            if (fluid == null)
                throw new IllegalStateException("Unknown fluid: " + fluidId);

            return new GasConverterHeatingRecipe(id, ing, count, new FluidStack(fluid, amount), time);
        }

        @Override
        public @Nullable GasConverterHeatingRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buf) {
            Ingredient ing = Ingredient.fromNetwork(buf);
            int count = buf.readVarInt();
            FluidStack result = buf.readFluidStack();
            int time = buf.readVarInt();
            return new GasConverterHeatingRecipe(id, ing, count, result, time);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buf, GasConverterHeatingRecipe recipe) {
            recipe.ingredient.toNetwork(buf);
            buf.writeVarInt(recipe.ingredientCount);
            buf.writeFluidStack(recipe.result);
            buf.writeVarInt(recipe.processingTime);
        }
    }
}