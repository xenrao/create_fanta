/*
 * MCreator note: This file will be REGENERATED on each build.
 */
package net.xenrao.cf.init;

import net.xenrao.cf.fluid.types.UnfilteredOrangeJuiceFluidType;
import net.xenrao.cf.CreateFantaMod;

import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.fluids.FluidType;

public class CreateFantaModFluidTypes {
	public static final DeferredRegister<FluidType> REGISTRY = DeferredRegister.create(ForgeRegistries.Keys.FLUID_TYPES, CreateFantaMod.MODID);
	public static final RegistryObject<FluidType> UNFILTERED_ORANGE_JUICE_TYPE = REGISTRY.register("unfiltered_orange_juice", () -> new UnfilteredOrangeJuiceFluidType());
}