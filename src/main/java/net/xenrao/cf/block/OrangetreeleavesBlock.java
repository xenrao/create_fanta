package net.xenrao.cf.block;

import net.xenrao.cf.init.CreateFantaModBlocks;

import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.api.distmarker.Dist;

import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.GrassColor;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.core.BlockPos;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.xenrao.cf.init.CreateFantaModItems;
import net.minecraft.world.level.FoliageColor;

public class OrangetreeleavesBlock extends LeavesBlock {
	public static final BooleanProperty FRUIT = BooleanProperty.create("fruit");

	public OrangetreeleavesBlock() {
		super(BlockBehaviour.Properties.of().sound(SoundType.AZALEA_LEAVES).strength(0.2f).noOcclusion().pushReaction(PushReaction.DESTROY).isRedstoneConductor((bs, br, bp) -> false).ignitedByLava().isSuffocating((bs, br, bp) -> false)
				.isViewBlocking((bs, br, bp) -> false));
		this.registerDefaultState(this.stateDefinition.any().setValue(FRUIT, false));
	}

	@Override
	public int getLightBlock(BlockState state, BlockGetter worldIn, BlockPos pos) {
		return 1;
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder);
		builder.add(FRUIT);
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		return super.getStateForPlacement(context).setValue(FRUIT, false);
	}

	@OnlyIn(Dist.CLIENT)
	public static void blockColorLoad(RegisterColorHandlersEvent.Block event) {
	    event.getBlockColors().register((bs, world, pos, index) -> {
	        // Sadece tintindex=0 olan katmanı renklendir (yapraklar)
	        // tintindex olmayan katman (portakallar) buraya hiç gelmez
	        if (index == 0) {
	            return world != null && pos != null 
	                ? BiomeColors.getAverageFoliageColor(world, pos) 
	                : FoliageColor.getDefaultColor();
	        }
	        return 0xFFFFFF; // Renk değiştirme
	    }, CreateFantaModBlocks.ORANGETREELEAVES.get());
	}

	@OnlyIn(Dist.CLIENT)
	public static void itemColorLoad(RegisterColorHandlersEvent.Item event) {
		event.getItemColors().register((stack, index) -> {
			return FoliageColor.getDefaultColor();
		}, CreateFantaModBlocks.ORANGETREELEAVES.get());
	}

	 @Override
	public InteractionResult use(BlockState state, Level level, BlockPos pos,
	                              Player player, InteractionHand hand, BlockHitResult hit) {
	    super.use(state, level, pos, player, hand, hit);
	
	    if (hand != InteractionHand.MAIN_HAND)
	        return InteractionResult.PASS;
	
	    if (state.getValue(FRUIT)) {
	        // === Meyve varsa: envantere ekle ===
	        level.setBlock(pos, state.setValue(FRUIT, false), 3);
	
	        if (!level.isClientSide()) {
	            ItemStack orange = new ItemStack(CreateFantaModItems.ORANGE.get());
	            // Envantere eklemeyi dene, doluysa yere at
	            if (!player.getInventory().add(orange)) {
	                player.drop(orange, false);
	            }
	        }
	        return InteractionResult.sidedSuccess(level.isClientSide());
	
	    } else {
	        // === Meyve yoksa: portakal tutuyorsa yerleştir ===
	        ItemStack heldItem = player.getItemInHand(hand);
	
	        if (heldItem.getItem() == CreateFantaModItems.ORANGE.get()) {
	            level.setBlock(pos, state.setValue(FRUIT, true), 3);
	
	            if (!player.isCreative()) {
	                heldItem.shrink(1);
	            }
	            return InteractionResult.sidedSuccess(level.isClientSide());
	        }
	    }
	
	    return InteractionResult.PASS;
	}
}