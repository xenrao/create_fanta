/*
 *    MCreator note: This file will be REGENERATED on each build.
 */
package net.xenrao.cf.init;

import net.xenrao.cf.item.*;
import net.xenrao.cf.CreateFantaMod;

import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.DeferredRegister;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.BlockItem;

public class CreateFantaModItems {
	public static final DeferredRegister<Item> REGISTRY = DeferredRegister.create(ForgeRegistries.ITEMS, CreateFantaMod.MODID);
	public static final RegistryObject<Item> ORANGE;
	public static final RegistryObject<Item> UNFILTERED_ORANGE_JUICE_BUCKET;
	public static final RegistryObject<Item> HALF_ORANGE;
	public static final RegistryObject<Item> PULP_FILTER;
	public static final RegistryObject<Item> CREATIVE_PULP_FILTER;
	public static final RegistryObject<Item> ORANGE_JUICE_BUCKET;
	public static final RegistryObject<Item> PLASTIC_BOTTLE;
	public static final RegistryObject<Item> USED_PLASTIC_BOTTLE;
	public static final RegistryObject<Item> FANTA;
	public static final RegistryObject<Item> SWEETENED_ORANGE_JUICE_BUCKET;
	public static final RegistryObject<Item> SWEETENED_ORANGE_JUICE_BOTTLE;
	public static final RegistryObject<Item> CO_2_BUCKET;
	public static final RegistryObject<Item> PETROL_BUCKET;
	public static final RegistryObject<Item> RAW_PLASTIC_RESIN;
	public static final RegistryObject<Item> ORANGE_TREE_SAPLING;
	public static final RegistryObject<Item> ORANGETREELEAVES;
	static {
		ORANGE = REGISTRY.register("orange", OrangeItem::new);
		UNFILTERED_ORANGE_JUICE_BUCKET = REGISTRY.register("unfiltered_orange_juice_bucket", UnfilteredOrangeJuiceItem::new);
		HALF_ORANGE = REGISTRY.register("half_orange", HalforangeItem::new);
		PULP_FILTER = REGISTRY.register("pulp_filter", PulpFilterItem::new);
		CREATIVE_PULP_FILTER = REGISTRY.register("creative_pulp_filter", CreativePulpFilterItem::new);
		ORANGE_JUICE_BUCKET = REGISTRY.register("orange_juice_bucket", OrangeJuiceItem::new);
		PLASTIC_BOTTLE = REGISTRY.register("plastic_bottle", PlasticBottleItem::new);
		USED_PLASTIC_BOTTLE = REGISTRY.register("used_plastic_bottle", UsedPlasticBottleItem::new);
		FANTA = REGISTRY.register("fanta", FantaItem::new);
		SWEETENED_ORANGE_JUICE_BUCKET = REGISTRY.register("sweetened_orange_juice_bucket", SweetenedOrangeJuiceItem::new);
		SWEETENED_ORANGE_JUICE_BOTTLE = REGISTRY.register("sweetened_orange_juice_bottle", SweetenedOrangeJuiceBottleItem::new);
		CO_2_BUCKET = REGISTRY.register("co_2_bucket", Co2Item::new);
		PETROL_BUCKET = REGISTRY.register("petrol_bucket", PetrolItem::new);
		RAW_PLASTIC_RESIN = REGISTRY.register("raw_plastic_resin", RawPlasticResinItem::new);
		ORANGE_TREE_SAPLING = block(CreateFantaModBlocks.ORANGE_TREE_SAPLING);
		ORANGETREELEAVES = block(CreateFantaModBlocks.ORANGETREELEAVES);
	}

	// Start of user code block custom items
	// End of user code block custom items
	private static RegistryObject<Item> block(RegistryObject<Block> block) {
		return block(block, new Item.Properties());
	}

	private static RegistryObject<Item> block(RegistryObject<Block> block, Item.Properties properties) {
		return REGISTRY.register(block.getId().getPath(), () -> new BlockItem(block.get(), properties));
	}
}