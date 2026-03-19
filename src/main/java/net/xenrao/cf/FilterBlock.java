package net.xenrao.cf.block;

import com.simibubi.create.content.kinetics.base.KineticBlock;
import com.simibubi.create.foundation.block.IBE;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.xenrao.cf.ModRegistry;
import com.simibubi.create.content.kinetics.simpleRelays.ICogWheel;

public class FilterBlock extends KineticBlock implements IBE<FilterBlockEntity>,ICogWheel {

    public FilterBlock(Properties p) {
        super(p);
    }

    @Override
    public Direction.Axis getRotationAxis(BlockState state) {
        return Direction.Axis.Y;
    }


    @Override
    public Class<FilterBlockEntity> getBlockEntityClass() {
        return FilterBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends FilterBlockEntity> getBlockEntityType() {
        return ModRegistry.FILTER_BE.get();
    }
}