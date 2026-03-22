/*
 *    MCreator note: This file will be REGENERATED on each build.
 */
package net.xenrao.cf.init;

import net.xenrao.cf.item.TestfItem;
import net.xenrao.cf.item.PulpFilterItem;
import net.xenrao.cf.item.OrangeItem;
import net.xenrao.cf.item.HalforangeItem;
import net.xenrao.cf.item.CreativePulpFilterItem;
import net.xenrao.cf.CreateFantaMod;

import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.DeferredRegister;

import net.minecraft.world.item.Item;

public class CreateFantaModItems {
	public static final DeferredRegister<Item> REGISTRY = DeferredRegister.create(ForgeRegistries.ITEMS, CreateFantaMod.MODID);
	public static final RegistryObject<Item> ORANGE;
	public static final RegistryObject<Item> HALF_ORANGE;
	public static final RegistryObject<Item> PULP_FILTER;
	public static final RegistryObject<Item> CREATIVE_PULP_FILTER;
	public static final RegistryObject<Item> TESTF_BUCKET;
	static {
		ORANGE = REGISTRY.register("orange", OrangeItem::new);
		HALF_ORANGE = REGISTRY.register("half_orange", HalforangeItem::new);
		PULP_FILTER = REGISTRY.register("pulp_filter", PulpFilterItem::new);
		CREATIVE_PULP_FILTER = REGISTRY.register("creative_pulp_filter", CreativePulpFilterItem::new);
		TESTF_BUCKET = REGISTRY.register("testf_bucket", TestfItem::new);
	}
	// Start of user code block custom items
	// End of user code block custom items
}