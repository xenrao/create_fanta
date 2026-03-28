/*
 * MCreator note: This file will be REGENERATED on each build.
 */
package net.xenrao.cf.init;

import net.xenrao.cf.fluid.types.UnfilteredOrangeJuiceFluidType;
import net.xenrao.cf.fluid.types.SweetenedOrangeJuiceFluidType;
import net.xenrao.cf.fluid.types.PetrolFluidType;
import net.xenrao.cf.fluid.types.OrangeJuiceFluidType;
import net.xenrao.cf.fluid.types.Co2FluidType;
import net.xenrao.cf.CreateFantaMod;

import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.fluids.FluidType;

public class CreateFantaModFluidTypes {
	public static final DeferredRegister<FluidType> REGISTRY = DeferredRegister.create(ForgeRegistries.Keys.FLUID_TYPES, CreateFantaMod.MODID);
	public static final RegistryObject<FluidType> UNFILTERED_ORANGE_JUICE_TYPE = REGISTRY.register("unfiltered_orange_juice", () -> new UnfilteredOrangeJuiceFluidType());
	public static final RegistryObject<FluidType> ORANGE_JUICE_TYPE = REGISTRY.register("orange_juice", () -> new OrangeJuiceFluidType());
	public static final RegistryObject<FluidType> SWEETENED_ORANGE_JUICE_TYPE = REGISTRY.register("sweetened_orange_juice", () -> new SweetenedOrangeJuiceFluidType());
	public static final RegistryObject<FluidType> CO_2_TYPE = REGISTRY.register("co_2", () -> new Co2FluidType());
	public static final RegistryObject<FluidType> PETROL_TYPE = REGISTRY.register("petrol", () -> new PetrolFluidType());
}