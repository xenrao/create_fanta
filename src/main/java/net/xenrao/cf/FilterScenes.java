package net.xenrao.cf;

import com.simibubi.create.content.fluids.tank.FluidTankBlockEntity;
import com.simibubi.create.foundation.ponder.CreateSceneBuilder;

import net.createmod.catnip.math.Pointing;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;

import net.xenrao.cf.block.FilterBlock;
import net.xenrao.cf.block.FilterBlockEntity;
import net.xenrao.cf.init.CreateFantaModFluids;

public class FilterScenes {

    public static void filtering(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);

        scene.title("mechanical_filter_filtering", "Using the Mechanical Filter");
        scene.configureBasePlate(0, 0, 6);
        scene.world().showSection(util.select().layer(0), Direction.UP);
        scene.idle(5);

        BlockPos filter     = util.grid().at(2, 1, 2);
        BlockPos pump       = util.grid().at(2, 1, 3);
        BlockPos glasspipe1 = util.grid().at(2, 1, 4);
        BlockPos tank1      = util.grid().at(2, 1, 5);

        BlockPos glasspipe2 = util.grid().at(2, 1, 1);
        BlockPos tank2      = util.grid().at(2, 1, 0);

        ItemStack pulpFilter = new ItemStack(
            BuiltInRegistries.ITEM.get(new ResourceLocation("create_fanta", "pulp_filter"))
        );

        // source tankı doldur
        scene.world().modifyBlockEntity(tank1, FluidTankBlockEntity.class,
            be -> be.getTankInventory().fill(
                new FluidStack(CreateFantaModFluids.UNFILTERED_ORANGE_JUICE.get(), 8000),
                FluidAction.EXECUTE
            )
        );

        // ===== 1) FILTER TEK BAŞINA =====
        scene.world().showSection(util.select().position(filter), Direction.DOWN);
        scene.idle(15);

        // geçici 90 derece saat yönü
        scene.world().modifyBlock(filter,
            state -> state.setValue(FilterBlock.FACING, state.getValue(FilterBlock.FACING).getClockWise()),
            false
        );
        scene.world().modifyBlock(filter,
            state -> state.setValue(FilterBlock.HAS_FILTER, false),
            false
        );
        scene.idle(5);

        scene.overlay().showText(70)
            .text("The Mechanical Filter processes fluids passing through it")
            .pointAt(util.vector().topOf(filter))
            .placeNearTarget()
            .attachKeyFrame();
        scene.idle(80);

        scene.overlay().showText(60)
            .text("It cannot operate without a Pulp Filter installed")
            .pointAt(util.vector().topOf(filter))
            .placeNearTarget()
            .attachKeyFrame();
        scene.idle(70);

        // ===== 2) PULP FILTER TAK =====
        scene.overlay().showControls(
            util.vector().topOf(filter),
            Pointing.DOWN,
            40
        ).rightClick().withItem(pulpFilter);
        scene.idle(15);

        scene.world().modifyBlock(filter,
            state -> state.setValue(FilterBlock.HAS_FILTER, true),
            false
        );

        scene.world().modifyBlockEntityNBT(
            util.select().position(filter),
            FilterBlockEntity.class,
            nbt -> {
                CompoundTag filterTag = new CompoundTag();
                filterTag.putInt("Size", 1);

                ListTag items = new ListTag();
                CompoundTag itemTag = new CompoundTag();
                itemTag.putByte("Slot", (byte) 0);
                itemTag.putString("id", "create_fanta:pulp_filter");
                itemTag.putByte("Count", (byte) 1);
                items.add(itemTag);

                filterTag.put("Items", items);
                nbt.put("Filter", filterTag);
            }
        );
        scene.idle(10);

        scene.effects().indicateSuccess(filter);
        scene.idle(10);

        // filter yönünü eski haline getir
        scene.world().modifyBlock(filter,
            state -> state.setValue(FilterBlock.FACING, state.getValue(FilterBlock.FACING).getCounterClockWise()),
            false
        );
        scene.idle(5);

        // ===== 3) SADECE INPUT TARAFINI GÖSTER =====
        scene.world().showSection(
            util.select().position(pump)
                .add(util.select().position(glasspipe1))
                .add(util.select().position(tank1)),
            Direction.DOWN
        );
        scene.idle(20);

        scene.overlay().showText(70)
            .text("A Mechanical Pump is required to move fluid through the filter")
            .pointAt(util.vector().topOf(pump))
            .placeNearTarget()
            .attachKeyFrame();
        scene.idle(80);

        // ===== 4) INPUT AKIŞI =====
        scene.world().setKineticSpeed(util.select().position(pump), 16);
        scene.world().setKineticSpeed(util.select().position(filter), 16);
        scene.world().propagatePipeChange(pump);
        scene.idle(20);

        scene.overlay().showText(70)
            .text("Unfiltered Orange Juice enters from the input side")
            .pointAt(util.vector().topOf(tank1))
            .placeNearTarget()
            .attachKeyFrame();
        scene.idle(80);

        // ===== 5) GECİKMELİ ÇIKIŞ METNİ =====
        scene.overlay().showText(70)
            .text("After a short delay, the filtered fluid exits from the other side")
            .pointAt(util.vector().topOf(filter))
            .placeNearTarget()
            .attachKeyFrame();
        scene.idle(40);




        // şimdi output tarafını göster
        scene.world().showSection(
            util.select().position(glasspipe2)
                .add(util.select().position(tank2)),
            Direction.DOWN
        );
        scene.idle(15);

        scene.markAsFinished();
    }
}