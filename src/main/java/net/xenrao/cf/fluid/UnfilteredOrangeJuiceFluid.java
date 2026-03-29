package net.xenrao.cf.fluid;

import net.xenrao.cf.init.CreateFantaModFluids;
import net.xenrao.cf.init.CreateFantaModFluidTypes;
import net.xenrao.cf.init.CreateFantaModBlocks;

import net.minecraftforge.fluids.ForgeFlowingFluid;

import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.LiquidBlock;

public abstract class UnfilteredOrangeJuiceFluid extends ForgeFlowingFluid {
	public static final ForgeFlowingFluid.Properties PROPERTIES = new ForgeFlowingFluid.Properties(() -> CreateFantaModFluidTypes.UNFILTERED_ORANGE_JUICE_TYPE.get(), () -> CreateFantaModFluids.UNFILTERED_ORANGE_JUICE.get(),
			() -> CreateFantaModFluids.FLOWING_UNFILTERED_ORANGE_JUICE.get()).explosionResistance(100f);

	private UnfilteredOrangeJuiceFluid() {
		super(PROPERTIES);
	}

	public static class Source extends UnfilteredOrangeJuiceFluid {
		public int getAmount(FluidState state) {
			return 8;
		}

		public boolean isSource(FluidState state) {
			return true;
		}
	}

	public static class Flowing extends UnfilteredOrangeJuiceFluid {
		protected void createFluidStateDefinition(StateDefinition.Builder<Fluid, FluidState> builder) {
			super.createFluidStateDefinition(builder);
			builder.add(LEVEL);
		}

		public int getAmount(FluidState state) {
			return state.getValue(LEVEL);
		}

		public boolean isSource(FluidState state) {
			return false;
		}
	}
}