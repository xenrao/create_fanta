package net.xenrao.cf.item;

import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.Item;

public class CreativePulpFilterItem extends Item {
	public CreativePulpFilterItem() {
		super(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));
	}
}