package net.xenrao.cf;

import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.xenrao.cf.recipe.FilteringRecipe;
import net.xenrao.cf.recipe.GasConverterHeatingRecipe;

public class ModRecipes {

    public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS =
        DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, CreateFantaMod.MODID);

    public static final DeferredRegister<RecipeType<?>> TYPES =
        DeferredRegister.create(ForgeRegistries.RECIPE_TYPES, CreateFantaMod.MODID);

    // ===== HEATING (item → fluid) =====
    public static final RegistryObject<RecipeType<GasConverterHeatingRecipe>> HEATING_TYPE =
        TYPES.register("heating", () -> new RecipeType<>() {});

    public static final RegistryObject<RecipeSerializer<GasConverterHeatingRecipe>> HEATING_SERIALIZER =
        SERIALIZERS.register("heating", GasConverterHeatingRecipe.Serializer::new);

    // ===== FILTERING (fluid → fluid) =====
    public static final RegistryObject<RecipeType<FilteringRecipe>> FILTERING_TYPE =
        TYPES.register("filtering", () -> new RecipeType<>() {});

    public static final RegistryObject<RecipeSerializer<FilteringRecipe>> FILTERING_SERIALIZER =
        SERIALIZERS.register("filtering", FilteringRecipe.Serializer::new);

    public static void register(IEventBus bus) {
        SERIALIZERS.register(bus);
        TYPES.register(bus);
    }
}