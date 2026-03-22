package net.xenrao.cf.fluid;

import net.xenrao.cf.init.CreateFantaModItems;
import net.xenrao.cf.init.CreateFantaModFluids;
import net.xenrao.cf.init.CreateFantaModFluidTypes;
import net.xenrao.cf.init.CreateFantaModBlocks;

import net.minecraftforge.fluids.ForgeFlowingFluid;

import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.LiquidBlock;

public abstract class TestfFluid extends ForgeFlowingFluid {
	public static final ForgeFlowingFluid.Properties PROPERTIES = new ForgeFlowingFluid.Properties(() -> CreateFantaModFluidTypes.TESTF_TYPE.get(), () -> CreateFantaModFluids.TESTF.get(), () -> CreateFantaModFluids.FLOWING_TESTF.get())
			.explosionResistance(100f).bucket(() -> CreateFantaModItems.TESTF_BUCKET.get()).block(() -> (LiquidBlock) CreateFantaModBlocks.TESTF.get());

	private TestfFluid() {
		super(PROPERTIES);
	}

	public static class Source extends TestfFluid {
		public int getAmount(FluidState state) {
			return 8;
		}

		public boolean isSource(FluidState state) {
			return true;
		}
	}

	public static class Flowing extends TestfFluid {
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