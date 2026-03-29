package net.xenrao.cf.item;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;
import net.minecraft.world.food.FoodProperties;

public class HalforangeItem extends Item {
	public HalforangeItem() {
		super(new Item.Properties().food((new FoodProperties.Builder()).nutrition(3).saturationMod(0.3f).build()));
	}

	@Override
	public int getUseDuration(ItemStack itemstack) {
		return 15;
	}
}