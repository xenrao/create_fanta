/*
 *    MCreator note: This file will be REGENERATED on each build.
 */
package net.xenrao.cf.init;

import net.xenrao.cf.CreateFantaMod;

import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;

import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.network.chat.Component;
import net.minecraft.core.registries.Registries;

public class CreateFantaModTabs {
	public static final DeferredRegister<CreativeModeTab> REGISTRY = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, CreateFantaMod.MODID);
	public static final RegistryObject<CreativeModeTab> CREATEFANTA = REGISTRY.register("createfanta",
			() -> CreativeModeTab.builder().title(Component.translatable("item_group.create_fanta.createfanta")).icon(() -> new ItemStack(Blocks.BARRIER)).displayItems((parameters, tabData) -> {
				tabData.accept(CreateFantaModItems.ORANGE.get());
				tabData.accept(CreateFantaModItems.HALF_ORANGE.get());
				tabData.accept(CreateFantaModItems.PULP_FILTER.get());
			}).build());
}