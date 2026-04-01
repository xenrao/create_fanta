package net.xenrao.cf.recipe;

import com.google.gson.JsonObject;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

import net.xenrao.cf.ModRecipes;

public class FilteringRecipe implements Recipe<Container> {

    private final ResourceLocation id;
    private final FluidStack ingredientFluid;
    private final FluidStack result;
    private final int processingTime;

    public FilteringRecipe(ResourceLocation id, FluidStack ingredientFluid,
                           FluidStack result, int processingTime) {
        this.id = id;
        this.ingredientFluid = ingredientFluid;
        this.result = result;
        this.processingTime = processingTime;
    }

    public FluidStack getIngredientFluid() { return ingredientFluid.copy(); }
    public int getIngredientAmount()       { return ingredientFluid.getAmount(); }
    public FluidStack getResultFluid()     { return result.copy(); }
    public int getProcessingTime()         { return processingTime; }

    /**
     * Verilen tank sıvısının bu tarifle eşleşip eşleşmediğini kontrol eder.
     */
    public boolean matches(FluidStack input) {
        if (input.isEmpty()) return false;
        return input.getFluid().isSame(ingredientFluid.getFluid())
            && input.getAmount() >= ingredientFluid.getAmount();
    }

    @Override public boolean matches(Container c, Level level) { return false; }
    @Override public ItemStack assemble(Container c, RegistryAccess ra) { return ItemStack.EMPTY; }
    @Override public boolean canCraftInDimensions(int w, int h) { return false; }
    @Override public ItemStack getResultItem(RegistryAccess ra) { return ItemStack.EMPTY; }
    @Override public ResourceLocation getId() { return id; }
    @Override public RecipeSerializer<?> getSerializer() { return ModRecipes.FILTERING_SERIALIZER.get(); }
    @Override public RecipeType<?> getType() { return ModRecipes.FILTERING_TYPE.get(); }

    // ===== SERIALIZER =====
    public static class Serializer implements RecipeSerializer<FilteringRecipe> {

        private FluidStack readFluidFromJson(JsonObject obj) {
            ResourceLocation fluidId = new ResourceLocation(GsonHelper.getAsString(obj, "fluid"));
            int amount = GsonHelper.getAsInt(obj, "amount");
            var fluid = ForgeRegistries.FLUIDS.getValue(fluidId);
            if (fluid == null)
                throw new IllegalStateException("Unknown fluid: " + fluidId);
            return new FluidStack(fluid, amount);
        }

        @Override
        public FilteringRecipe fromJson(ResourceLocation id, JsonObject json) {
            FluidStack ingredient = readFluidFromJson(GsonHelper.getAsJsonObject(json, "ingredient"));
            FluidStack result = readFluidFromJson(GsonHelper.getAsJsonObject(json, "result"));
            int time = GsonHelper.getAsInt(json, "processing_time", 100);
            return new FilteringRecipe(id, ingredient, result, time);
        }

        @Override
        public @Nullable FilteringRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buf) {
            FluidStack ingredient = buf.readFluidStack();
            FluidStack result = buf.readFluidStack();
            int time = buf.readVarInt();
            return new FilteringRecipe(id, ingredient, result, time);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buf, FilteringRecipe recipe) {
            buf.writeFluidStack(recipe.ingredientFluid);
            buf.writeFluidStack(recipe.result);
            buf.writeVarInt(recipe.processingTime);
        }
    }
}