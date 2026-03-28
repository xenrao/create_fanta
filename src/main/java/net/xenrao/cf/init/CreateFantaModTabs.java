/*
 *    MCreator note: This file will be REGENERATED on each build.
 */
package net.xenrao.cf.init;

import net.xenrao.cf.CreateFantaMod;

import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.network.chat.Component;
import net.minecraft.core.registries.Registries;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class CreateFantaModTabs {
	public static final DeferredRegister<CreativeModeTab> REGISTRY = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, CreateFantaMod.MODID);
	public static final RegistryObject<CreativeModeTab> CREATEFANTA = REGISTRY.register("createfanta",
			() -> CreativeModeTab.builder().title(Component.translatable("item_group.create_fanta.createfanta")).icon(() -> new ItemStack(CreateFantaModItems.FANTA.get())).displayItems((parameters, tabData) -> {
				tabData.accept(CreateFantaModItems.ORANGE.get());
				tabData.accept(CreateFantaModItems.UNFILTERED_ORANGE_JUICE_BUCKET.get());
				tabData.accept(CreateFantaModItems.HALF_ORANGE.get());
				tabData.accept(CreateFantaModItems.PULP_FILTER.get());
				tabData.accept(CreateFantaModItems.CREATIVE_PULP_FILTER.get());
				tabData.accept(CreateFantaModItems.ORANGE_JUICE_BUCKET.get());
				tabData.accept(CreateFantaModItems.PLASTIC_BOTTLE.get());
				tabData.accept(CreateFantaModItems.USED_PLASTIC_BOTTLE.get());
				tabData.accept(CreateFantaModItems.FANTA.get());
				tabData.accept(CreateFantaModItems.SWEETENED_ORANGE_JUICE_BUCKET.get());
				tabData.accept(CreateFantaModItems.SWEETENED_ORANGE_JUICE_BOTTLE.get());
				tabData.accept(CreateFantaModItems.CO_2_BUCKET.get());
				tabData.accept(CreateFantaModItems.PETROL_BUCKET.get());
				tabData.accept(CreateFantaModItems.RAW_PLASTIC_RESIN.get());
				tabData.accept(CreateFantaModBlocks.ORANGETREELEAVES.get().asItem());
			}).build());

	@SubscribeEvent
	public static void buildTabContentsVanilla(BuildCreativeModeTabContentsEvent tabData) {
		if (tabData.getTabKey() == CreativeModeTabs.NATURAL_BLOCKS) {
			tabData.accept(CreateFantaModBlocks.ORANGE_TREE_SAPLING.get().asItem());
		}
	}
}