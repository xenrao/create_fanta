/*
 *    MCreator note: This file will be REGENERATED on each build.
 */
package net.xenrao.cf.init;

import net.xenrao.cf.block.UnfilteredOrangeJuiceBlock;
import net.xenrao.cf.block.TestfBlock;
import net.xenrao.cf.CreateFantaMod;

import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.DeferredRegister;

import net.minecraft.world.level.block.Block;

public class CreateFantaModBlocks {
	public static final DeferredRegister<Block> REGISTRY = DeferredRegister.create(ForgeRegistries.BLOCKS, CreateFantaMod.MODID);
	public static final RegistryObject<Block> UNFILTERED_ORANGE_JUICE;
	public static final RegistryObject<Block> TESTF;
	static {
		UNFILTERED_ORANGE_JUICE = REGISTRY.register("unfiltered_orange_juice", UnfilteredOrangeJuiceBlock::new);
		TESTF = REGISTRY.register("testf", TestfBlock::new);
	}
	// Start of user code block custom blocks
	// End of user code block custom blocks
}