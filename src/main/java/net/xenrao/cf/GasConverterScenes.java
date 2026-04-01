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

import net.xenrao.cf.block.GasConverterReservoirBlockEntity;
import net.xenrao.cf.init.CreateFantaModFluids;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.IItemHandlerModifiable;

public class GasConverterScenes {

    public static void heating(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);

        scene.title("refinery_basin_processing", "Using the Gas Converter Reservoir");
        scene.configureBasePlate(0, 0, 5);
        scene.world().showSection(util.select().layer(0), Direction.UP);
        scene.idle(5);

        // Pozisyonlar
        BlockPos converter = util.grid().at(2, 1, 2);
        BlockPos lava      = util.grid().at(2, 1, 3);
        BlockPos fan       = util.grid().at(2, 1, 4);

        BlockPos pump      = util.grid().at(2, 2, 2);
  

        ItemStack limestone = new ItemStack(
            BuiltInRegistries.ITEM.get(new ResourceLocation("create", "limestone"))
        );

        // ===== 1) CONVERTER =====
        scene.world().showSection(util.select().position(converter), Direction.DOWN);
        scene.idle(15);

        scene.overlay().showText(50)
            .text("1")
            .pointAt(util.vector().topOf(converter))
            .placeNearTarget()
            .attachKeyFrame();
        scene.idle(80);

        // ===== 3) LIMESTONE INPUT =====
        
        scene.overlay().showControls(
            util.vector().topOf(converter),
            Pointing.DOWN,
            40
        ).rightClick().withItem(limestone);
        
        scene.overlay().showText(50)
            .text("2")
            .pointAt(util.vector().topOf(converter))
            .placeNearTarget()
            .attachKeyFrame();
            
		scene.world().modifyBlockEntityNBT(
		    util.select().position(converter),
		    GasConverterReservoirBlockEntity.class,
		    nbt -> {
		        // Limestone ekle
		        net.minecraft.nbt.CompoundTag inputTag = new net.minecraft.nbt.CompoundTag();
		        inputTag.putInt("Size", 1);
		        net.minecraft.nbt.ListTag items = new net.minecraft.nbt.ListTag();
		        net.minecraft.nbt.CompoundTag itemTag = new net.minecraft.nbt.CompoundTag();
		        itemTag.putByte("Slot", (byte) 0);
		        itemTag.putString("id", "create:limestone");
		        itemTag.putByte("Count", (byte) 64);
		        items.add(itemTag);
		        inputTag.put("Items", items);
		        nbt.put("Input", inputTag);
            }
        );
        
        scene.idle(50);
        
        // ===== 2) LAVA + FAN =====
        scene.world().showSection(
            util.select().position(lava)
                .add(util.select().position(fan)),
            Direction.DOWN
        );
        scene.effects().rotationDirectionIndicator(fan);
        scene.idle(15);


        scene.overlay().showText(150)
            .text("3")
            .pointAt(util.vector().topOf(lava))
            .placeNearTarget()
            .attachKeyFrame();
        scene.idle(150);



        scene.overlay().showText(80)
            .text("4")
            .pointAt(util.vector().centerOf(converter))
            .placeNearTarget()
            .attachKeyFrame();

		// Reservoir içine fluid doldur
        scene.world().modifyBlockEntity(converter,
            GasConverterReservoirBlockEntity.class, be -> {
                be.getCO2Tank().fill(
                    new FluidStack(CreateFantaModFluids.CO_2.get(), 1000),
                    FluidAction.EXECUTE
                );
            }
        );
        scene.idle(90);
        // ===== 4) PUMP + PIPE =====

        scene.world().showSection(
            util.select().position(pump),
            Direction.DOWN
        );
        scene.idle(15);

        scene.overlay().showText(80)
            .text("5")
            .pointAt(util.vector().topOf(pump))
            .placeNearTarget()
            .attachKeyFrame();
        scene.idle(30);

        // Pump'ı çalıştır
        scene.world().setKineticSpeed(
            util.select().position(pump),
            16
        );
        scene.idle(5);

        scene.markAsFinished();
    }
}