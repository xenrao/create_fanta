/*
 *    MCreator note: This file will be REGENERATED on each build.
 */
package net.xenrao.cf.init;

import net.xenrao.cf.block.*;
import net.xenrao.cf.CreateFantaMod;

import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.api.distmarker.Dist;

import net.minecraft.world.level.block.Block;

public class CreateFantaModBlocks {
	public static final DeferredRegister<Block> REGISTRY = DeferredRegister.create(ForgeRegistries.BLOCKS, CreateFantaMod.MODID);
	public static final RegistryObject<Block> UNFILTERED_ORANGE_JUICE;
	public static final RegistryObject<Block> ORANGE_JUICE;
	public static final RegistryObject<Block> SWEETENED_ORANGE_JUICE;
	public static final RegistryObject<Block> CO_2;
	public static final RegistryObject<Block> PETROL;
	public static final RegistryObject<Block> ORANGE_TREE_SAPLING;
	public static final RegistryObject<Block> ORANGETREELEAVES;
	static {
		UNFILTERED_ORANGE_JUICE = REGISTRY.register("unfiltered_orange_juice", UnfilteredOrangeJuiceBlock::new);
		ORANGE_JUICE = REGISTRY.register("orange_juice", OrangeJuiceBlock::new);
		SWEETENED_ORANGE_JUICE = REGISTRY.register("sweetened_orange_juice", SweetenedOrangeJuiceBlock::new);
		CO_2 = REGISTRY.register("co_2", Co2Block::new);
		PETROL = REGISTRY.register("petrol", PetrolBlock::new);
		ORANGE_TREE_SAPLING = REGISTRY.register("orange_tree_sapling", OrangeTreeSaplingBlock::new);
		ORANGETREELEAVES = REGISTRY.register("orangetreeleaves", OrangetreeleavesBlock::new);
	}

	// Start of user code block custom blocks
	// End of user code block custom blocks
	@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
	public static class BlocksClientSideHandler {
		@SubscribeEvent
		public static void blockColorLoad(RegisterColorHandlersEvent.Block event) {
			OrangetreeleavesBlock.blockColorLoad(event);
		}

		@SubscribeEvent
		public static void itemColorLoad(RegisterColorHandlersEvent.Item event) {
			OrangetreeleavesBlock.itemColorLoad(event);
		}
	}
}