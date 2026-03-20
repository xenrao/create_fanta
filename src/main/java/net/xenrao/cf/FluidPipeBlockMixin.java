package net.xenrao.cf.mixin;

import com.simibubi.create.content.fluids.pipes.FluidPipeBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.xenrao.cf.block.FilterBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FluidPipeBlock.class)
public class FluidPipeBlockMixin {
	
    @Inject(method = "canConnectTo", at = @At("HEAD"), cancellable = true, remap = false)
    private static void create_fanta$hideRimForFilter(BlockAndTintGetter world, BlockPos pos, BlockState state,
                                                      Direction direction, CallbackInfoReturnable<Boolean> cir) {


            cir.setReturnValue(true);
        
    }
    
}