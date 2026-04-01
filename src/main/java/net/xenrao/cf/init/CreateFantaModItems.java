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
	public static final RegistryObject<Item> HALF_ORANGE;
	public static final RegistryObject<Item> PULP_FILTER;
	public static final RegistryObject<Item> CREATIVE_PULP_FILTER;
	public static final RegistryObject<Item> PLASTIC_BOTTLE;
	public static final RegistryObject<Item> USED_PLASTIC_BOTTLE;
	public static final RegistryObject<Item> FANTA;
	public static final RegistryObject<Item> SWEETENED_ORANGE_JUICE_BOTTLE;
	public static final RegistryObject<Item> RAW_PLASTIC_RESIN;
	public static final RegistryObject<Item> ORANGE_TREE_SAPLING;
	public static final RegistryObject<Item> ORANGETREELEAVES;
	public static final RegistryObject<Item> ORANGE_PEEL;
	static {
		ORANGE = REGISTRY.register("orange", OrangeItem::new);
		HALF_ORANGE = REGISTRY.register("half_orange", HalforangeItem::new);
		PULP_FILTER = REGISTRY.register("pulp_filter", PulpFilterItem::new);
		CREATIVE_PULP_FILTER = REGISTRY.register("creative_pulp_filter", CreativePulpFilterItem::new);
		PLASTIC_BOTTLE = REGISTRY.register("plastic_bottle", PlasticBottleItem::new);
		USED_PLASTIC_BOTTLE = REGISTRY.register("used_plastic_bottle", UsedPlasticBottleItem::new);
		FANTA = REGISTRY.register("fanta", FantaItem::new);
		SWEETENED_ORANGE_JUICE_BOTTLE = REGISTRY.register("sweetened_orange_juice_bottle", SweetenedOrangeJuiceBottleItem::new);
		RAW_PLASTIC_RESIN = REGISTRY.register("raw_plastic_resin", RawPlasticResinItem::new);
		ORANGE_TREE_SAPLING = block(CreateFantaModBlocks.ORANGE_TREE_SAPLING);
		ORANGETREELEAVES = block(CreateFantaModBlocks.ORANGETREELEAVES);
		ORANGE_PEEL = REGISTRY.register("orange_peel", OrangePeelItem::new);
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