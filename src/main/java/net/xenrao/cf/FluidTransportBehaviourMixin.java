package net.xenrao.cf.mixin;

import com.simibubi.create.content.fluids.FluidTransportBehaviour;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.xenrao.cf.block.FilterBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FluidTransportBehaviour.class)
public class FluidTransportBehaviourMixin {
	@Inject(method = "getRenderedRimAttachment", at = @At("HEAD"), cancellable = true, remap = false)
	private void create_fanta$hideRimForFilter(BlockAndTintGetter world, BlockPos pos, BlockState state,
	                                            Direction direction,
	                                            CallbackInfoReturnable<FluidTransportBehaviour.AttachmentTypes> cir) {
	    BlockPos offsetPos = pos.relative(direction);
	    BlockState facingState = world.getBlockState(offsetPos);
	
	    if (facingState.getBlock() instanceof FilterBlock) {
	        Direction filterFacing = facingState.getValue(FilterBlock.FACING);
	        Direction pipeSide = direction;
	
	        // Pipe'ın baktığı yön, filter'ın input veya output yönüyse rim kaldır
	        if (pipeSide == filterFacing || pipeSide == filterFacing.getOpposite()) {
	            cir.setReturnValue(FluidTransportBehaviour.AttachmentTypes.NONE);
	        }
	    }
	}
}