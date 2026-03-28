package net.xenrao.cf.block;

import com.simibubi.create.foundation.block.IBE;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.xenrao.cf.ModRegistry;
import org.jetbrains.annotations.Nullable;
import net.minecraft.world.level.block.RenderShape;

public class GasConverterReservoirBlock extends Block implements IBE<GasConverterReservoirBlockEntity> {

    public static final BooleanProperty ACTIVE = BooleanProperty.create("active");

    // ===== SHAPE =====
    // Taban: 14x14, 2px yükseklik, ortalanmış
    // Duvarlar: 16x16, 2px kalınlık, 2px'den 16px'e kadar
    private static final VoxelShape SHAPE;

	static {
	    VoxelShape base = Block.box(2, 0, 2, 14, 2, 14);
	    VoxelShape rest = Block.box(0, 2, 0, 16, 16, 16);
	    SHAPE = Shapes.or(base, rest);
	}

    public GasConverterReservoirBlock(Properties props) {
        super(props);
        registerDefaultState(defaultBlockState()
            .setValue(ACTIVE, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(ACTIVE);
    }

    // Rotasyon yok - her zaman aynı şekilde yerleşir

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level,
                                BlockPos pos, CollisionContext ctx) {
        return SHAPE;
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos,
                                  Player player, InteractionHand hand,
                                  BlockHitResult hit) {
        if (level.isClientSide) return InteractionResult.SUCCESS;
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof GasConverterReservoirBlockEntity reservoir)
            return reservoir.handleInteraction(player, hand);
        return InteractionResult.PASS;
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos,
                          BlockState newState, boolean moving) {
        if (!state.is(newState.getBlock())) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof GasConverterReservoirBlockEntity reservoir)
                reservoir.dropContents();
        }
        super.onRemove(state, level, pos, newState, moving);
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState state) { return true; }

    @Override
    public int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof GasConverterReservoirBlockEntity r)
            return r.getComparatorOutput();
        return 0;
    }

    // ===== EntityBlock =====
    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new GasConverterReservoirBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide) return null;
        return type == ModRegistry.GAS_CONVERTER_BE.get()
            ? (lvl, pos, st, be) -> ((GasConverterReservoirBlockEntity) be).serverTick()
            : null;
    }

    // ===== IBE =====
    @Override
    public Class<GasConverterReservoirBlockEntity> getBlockEntityClass() {
        return GasConverterReservoirBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends GasConverterReservoirBlockEntity> getBlockEntityType() {
        return ModRegistry.GAS_CONVERTER_BE.get();
    }
}