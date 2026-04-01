package net.xenrao.cf;

import com.simibubi.create.api.stress.BlockStressValues;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class CommonSetup {

    @SubscribeEvent
    public static void init(FMLCommonSetupEvent event) {
        BlockStressValues.IMPACTS.registerProvider(block -> {
            if (block == ModRegistry.FILTER_BLOCK.get()) {
                return () -> 8.0;
            }
            return null;
        });
    }
}