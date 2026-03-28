package net.xenrao.cf.procedures;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.event.level.SaplingGrowTreeEvent;

import javax.annotation.Nullable;

@Mod.EventBusSubscriber
public class TestAreaProcedure {
	@SubscribeEvent
	public static void onSaplingGrow(SaplingGrowTreeEvent event) {
		execute(event);
	}

	public static void execute() {
		execute(null);
	}

	private static void execute(@Nullable Event event) {
	}
}