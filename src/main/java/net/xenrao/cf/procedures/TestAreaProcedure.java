package net.xenrao.cf.procedures;

import net.xenrao.cf.CreateFantaMod;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.event.level.SaplingGrowTreeEvent;

import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

@Mod.EventBusSubscriber
public class TestAreaProcedure {
	@SubscribeEvent
	public static void onSaplingGrow(SaplingGrowTreeEvent event) {
		execute(event, event.getLevel().getBlockState(event.getPos()));
	}

	public static void execute(BlockState blockstate) {
		execute(null, blockstate);
	}

	private static void execute(@Nullable Event event, BlockState blockstate) {
		CreateFantaMod.LOGGER.info(blockstate);
	}
}