package net.xenrao.cf.block;

import com.simibubi.create.content.kinetics.base.KineticBlock;
import com.simibubi.create.content.kinetics.simpleRelays.ICogWheel;
import com.simibubi.create.foundation.block.IBE;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.xenrao.cf.ModRegistry;
import net.minecraft.world.level.BlockAndTintGetter;
import com.simibubi.create.content.fluids.FluidTransportBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;

public class FilterBlock extends KineticBlock implements IBE<FilterBlockEntity>, ICogWheel {

    public static final DirectionProperty FACING = BlockStateProperties.FACING;

    public FilterBlock(Properties p) {
        super(p);
        registerDefaultState(defaultBlockState().setValue(FACING, Direction.UP));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
        super.createBlockStateDefinition(builder);
    }
    
    // Oyuncu nereye bakıyorsa oraya yönlendir
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING,
            context.getNearestLookingDirection().getOpposite());
    }

    // Facing eksenine göre rotation axis
    @Override
    public Direction.Axis getRotationAxis(BlockState state) {
        return state.getValue(FACING).getAxis();
    }

    // Shaft yok, cogwheel kullanıyoruz
    @Override
    public boolean hasShaftTowards(LevelReader world, BlockPos pos,
                                    BlockState state, Direction face) {
        return false;
    }

    // Küçük cogwheel gibi davransın
    @Override
    public boolean isSmallCog() {
        return true;
    }

    @Override
    public boolean isLargeCog() {
        return false;
    }

    // Sağ tık → filtre koy/çıkar
    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos,
                                  Player player, InteractionHand hand,
                                  BlockHitResult hit) {
        if (level.isClientSide)
            return InteractionResult.SUCCESS;

        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof FilterBlockEntity filter) {
            return filter.handleInteraction(player, hand);
        }
        return InteractionResult.PASS;
    }

    // Comparator sinyali
    @Override
    public boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof FilterBlockEntity filter) {
            return filter.getComparatorOutput();
        }
        return 0;
    }

    // Blok kırılınca filtreyi düşür
    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos,
                          BlockState newState, boolean moving) {
        if (!state.is(newState.getBlock())) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof FilterBlockEntity filter) {
                filter.dropFilter();
            }
        }
        super.onRemove(state, level, pos, newState, moving);
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