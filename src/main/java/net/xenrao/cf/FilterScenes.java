package net.xenrao.cf;

import com.simibubi.create.content.fluids.pump.PumpBlock;
import com.simibubi.create.foundation.ponder.CreateSceneBuilder;

import net.createmod.catnip.math.Pointing;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;

import net.xenrao.cf.block.FilterBlockEntity;
import net.xenrao.cf.init.CreateFantaModFluids;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.IItemHandlerModifiable;

public class FilterScenes {

    public static void filtering(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);

        scene.title("mechanical_filter_filtering", "Using the Mechanical filter");
        scene.configureBasePlate(0, 0, 6);
        scene.world().showSection(util.select().layer(0), Direction.UP);
        scene.idle(5);

        // Pozisyonlar
        BlockPos filter = util.grid().at(2, 1, 2);
        BlockPos pump      = util.grid().at(2, 1, 3);
        BlockPos glasspipe1 = util.grid().at(2, 1, 4);
        BlockPos tank1 = util.grid().at(2, 1, 5);

		BlockPos glasspipe2 = util.grid().at(2, 1, 1);
		BlockPos tank2 = util.grid().at(2, 1, 0);
        
  

        ItemStack limestone = new ItemStack(
            BuiltInRegistries.ITEM.get(new ResourceLocation("create", "limestone"))
        );

        // ===== 1) CONVERTER =====
        scene.world().showSection(util.select().position(filter), Direction.DOWN);
        scene.idle(15);

        scene.overlay().showText(50)
            .text("Place a Gas Converter Reservoir")
            .pointAt(util.vector().topOf(filter))
            .placeNearTarget()
            .attachKeyFrame();
        scene.idle(80);

        
        // ===== 2) LAVA + FAN =====
        scene.world().showSection(
            util.select().position(pump)
                .add(util.select().position(glasspipe1))
                .add(util.select().position(glasspipe2))
                .add(util.select().position(tank1))
                .add(util.select().position(tank2)),
            Direction.DOWN
        );
  
		scene.world().setKineticSpeed(
		    util.select().position(pump),
		    256
		);
		
		scene.world().modifyBlockEntityNBT(
		    util.select().position(filter),
		    FilterBlockEntity.class,
		    nbt -> {
		        nbt.putFloat("Speed", 256f);
		    }
		);
		scene.idle(80);

        scene.markAsFinished();
    }
}