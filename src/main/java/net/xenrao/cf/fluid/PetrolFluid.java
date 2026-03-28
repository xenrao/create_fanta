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

public abstract class PetrolFluid extends ForgeFlowingFluid {
	public static final ForgeFlowingFluid.Properties PROPERTIES = new ForgeFlowingFluid.Properties(() -> CreateFantaModFluidTypes.PETROL_TYPE.get(), () -> CreateFantaModFluids.PETROL.get(), () -> CreateFantaModFluids.FLOWING_PETROL.get())
			.explosionResistance(100f).tickRate(50).bucket(() -> CreateFantaModItems.PETROL_BUCKET.get()).block(() -> (LiquidBlock) CreateFantaModBlocks.PETROL.get());

	private PetrolFluid() {
		super(PROPERTIES);
	}

	public static class Source extends PetrolFluid {
		public int getAmount(FluidState state) {
			return 8;
		}

		public boolean isSource(FluidState state) {
			return true;
		}
	}

	public static class Flowing extends PetrolFluid {
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