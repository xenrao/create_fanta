package net.xenrao.cf.block;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.xenrao.cf.ModRegistry;

@Mod.EventBusSubscriber(modid = "create_fanta", bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class FilterClientEvents {

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(ModRegistry.FILTER_BE.get(), FilterRenderer::new);
    }
}