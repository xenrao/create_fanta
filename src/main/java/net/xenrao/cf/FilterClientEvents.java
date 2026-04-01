package net.xenrao.cf.block;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.xenrao.cf.ModRegistry;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.xenrao.cf.CreateFantaPonderPlugin;
import net.createmod.ponder.foundation.PonderIndex;

import com.simibubi.create.foundation.item.KineticStats;
import com.simibubi.create.foundation.item.TooltipModifier;

@Mod.EventBusSubscriber(modid = "create_fanta", bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class FilterClientEvents {

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(ModRegistry.FILTER_BE.get(), FilterRenderer::new);
        event.registerBlockEntityRenderer(ModRegistry.GAS_CONVERTER_BE.get(), GasConverterReservoirRenderer::new);
    }
    @SubscribeEvent
    public static void clientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
        	FilterPartialModels.init();
            ItemBlockRenderTypes.setRenderLayer(ModRegistry.FILTER_BLOCK.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(ModRegistry.GAS_CONVERTER_BLOCK.get(), RenderType.cutoutMipped());
            PonderIndex.addPlugin(new CreateFantaPonderPlugin());
            TooltipModifier.REGISTRY.register(
                ModRegistry.FILTER_BLOCK_ITEM.get(),
                new KineticStats(ModRegistry.FILTER_BLOCK.get())
            );
        });
    }
}

