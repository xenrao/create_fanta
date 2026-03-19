/*
 * MCreator note: This file will be REGENERATED on each build.
 */
package net.xenrao.cf.init;

import net.xenrao.cf.fluid.UnfilteredOrangeJuiceFluid;
import net.xenrao.cf.CreateFantaMod;

import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.api.distmarker.Dist;

import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ItemBlockRenderTypes;

public class CreateFantaModFluids {
	public static final DeferredRegister<Fluid> REGISTRY = DeferredRegister.create(ForgeRegistries.FLUIDS, CreateFantaMod.MODID);
	public static final RegistryObject<FlowingFluid> UNFILTERED_ORANGE_JUICE = REGISTRY.register("unfiltered_orange_juice", () -> new UnfilteredOrangeJuiceFluid.Source());
	public static final RegistryObject<FlowingFluid> FLOWING_UNFILTERED_ORANGE_JUICE = REGISTRY.register("flowing_unfiltered_orange_juice", () -> new UnfilteredOrangeJuiceFluid.Flowing());

	@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
	public static class FluidsClientSideHandler {
		@SubscribeEvent
		public static void clientSetup(FMLClientSetupEvent event) {
			ItemBlockRenderTypes.setRenderLayer(UNFILTERED_ORANGE_JUICE.get(), RenderType.translucent());
			ItemBlockRenderTypes.setRenderLayer(FLOWING_UNFILTERED_ORANGE_JUICE.get(), RenderType.translucent());
		}
	}
}