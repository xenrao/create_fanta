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
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.level.BlockGetter;
import net.xenrao.cf.ModRegistry;

public class FilterBlock extends KineticBlock implements IBE<FilterBlockEntity>, ICogWheel {

    public static final DirectionProperty FACING = BlockStateProperties.FACING;
    public static final BooleanProperty HAS_FILTER = BooleanProperty.create("has_filter");

    private static final VoxelShape SHAPE_Y = Block.box(2, 0, 2, 14, 16, 14);
    private static final VoxelShape SHAPE_Z = Block.box(2, 2, 0, 14, 14, 16);
    private static final VoxelShape SHAPE_X = Block.box(0, 2, 2, 16, 14, 14);

    public FilterBlock(Properties p) {
        super(p);
        registerDefaultState(defaultBlockState()
            .setValue(FACING, Direction.UP)
            .setValue(HAS_FILTER, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, HAS_FILTER);
        super.createBlockStateDefinition(builder);
    }

    // ── Create tarzı yerleştirme ─────────────────────────────
    //   Normal:   Baktığın yöne doğru yerleşir (bakış yönü)
    //   Shift:    Tam tersi yöne yerleşir (bakışın tersine)
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction looking = context.getNearestLookingDirection();

        Direction facing;
        if (context.getPlayer() != null && context.getPlayer().isShiftKeyDown()) {
            // Shift basılı → bakışın tersi (mevcut davranış)
            facing = looking.getOpposite();
        } else {
            // Normal → bakış yönüyle aynı
            facing = looking;
        }

        return defaultBlockState().setValue(FACING, facing);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level,
                                BlockPos pos, CollisionContext context) {
        return switch (state.getValue(FACING).getAxis()) {
            case Y -> SHAPE_Y;
            case Z -> SHAPE_Z;
            case X -> SHAPE_X;
        };
    }

    @Override
    public Direction.Axis getRotationAxis(BlockState state) {
        return state.getValue(FACING).getAxis();
    }

    @Override
    public boolean hasShaftTowards(LevelReader world, BlockPos pos,
                                    BlockState state, Direction face) {
        return false;
    }

    @Override public boolean isSmallCog() { return true; }
    @Override public boolean isLargeCog() { return false; }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos,
                                  Player player, InteractionHand hand,
                                  BlockHitResult hit) {
        if (level.isClientSide) return InteractionResult.SUCCESS;
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof FilterBlockEntity filter)
            return filter.handleInteraction(player, hand);
        return InteractionResult.PASS;
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState state) { return true; }

    @Override
    public int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof FilterBlockEntity f) return f.getComparatorOutput();
        return 0;
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos,
                          BlockState newState, boolean moving) {
        if (!state.is(newState.getBlock())) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof FilterBlockEntity f) f.dropFilter();
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