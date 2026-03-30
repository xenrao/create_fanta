package net.xenrao.cf;

import net.createmod.ponder.api.registration.PonderSceneRegistrationHelper;
import net.createmod.ponder.api.registration.PonderTagRegistrationHelper;
import net.minecraft.resources.ResourceLocation;

public class CreateFantaPonders {

    public static void register(PonderSceneRegistrationHelper<ResourceLocation> helper) {
        helper.forComponents(ModRegistry.GAS_CONVERTER_BLOCK.getId())
            .addStoryBoard("gas_converter_heating", GasConverterScenes::heating);
        helper.forComponents(ModRegistry.FILTER_BLOCK.getId())
            .addStoryBoard("mechanical_filter_filtering", FilterScenes::filtering);
    }

    public static void register(PonderTagRegistrationHelper<ResourceLocation> helper) {
         //bruh
    }
}