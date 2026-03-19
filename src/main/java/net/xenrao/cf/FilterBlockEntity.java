package net.xenrao.cf.block;

import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.content.fluids.FluidTransportBehaviour;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.fluid.SmartFluidTank;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import net.xenrao.cf.ModRegistry;

import java.util.List;

public class FilterBlockEntity extends KineticBlockEntity implements IHaveGoggleInformation {

    private static final int BUFFER = 1000;
    private static final int BATCH = 50;
    private static final int BASE_TIME = 200;

    private int timer = BASE_TIME;
    private final SmartFluidTank inTank;
    private final SmartFluidTank outTank;

    private LazyOptional<IFluidHandler> capUp = LazyOptional.empty();
    private LazyOptional<IFluidHandler> capDown = LazyOptional.empty();
    private LazyOptional<IFluidHandler> capAll = LazyOptional.empty();

    public FilterBlockEntity(BlockPos pos, BlockState state) {
        super(ModRegistry.FILTER_BE.get(), pos, state);
        inTank = new SmartFluidTank(BUFFER, f -> setChanged());
        outTank = new SmartFluidTank(BUFFER, f -> setChanged());
    }

    // =========================================
    //  BEHAVIOUR - PASİF BORU BAĞLANTISI
    //  Pressure YOK. Pull/Push YOK.
    //  Sadece pipe'ların bağlanmasını sağlar.
    //  Harici pump akışı yönetir.
    // =========================================
    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        super.addBehaviours(behaviours);
        behaviours.add(new FilterTransport(this));
    }

    // =========================================
    //  TICK - SADECE DÖNÜŞÜM, PULL/PUSH YOK
    // =========================================
    @Override
    public void tick() {
        super.tick();

        if (level == null || level.isClientSide)
            return;

        float spd = Math.abs(getSpeed());
        if (spd <= 0)
            return;

        // Input tankta su varsa dönüştür
        if (!inTank.isEmpty()
            && inTank.getFluid().getFluid().isSame(Fluids.WATER)) {

            int rate = Mth.clamp((int) (spd / 16f), 1, 512);
            timer -= rate;

            if (timer <= 0) {
                convert();
                timer = BASE_TIME;
            }
        } else {
            timer = BASE_TIME;
        }
    }

    private void convert() {
        if (inTank.getFluidAmount() < BATCH)
            return;
        if (!inTank.getFluid().getFluid().isSame(Fluids.WATER))
            return;

        int outSpace = outTank.getCapacity() - outTank.getFluidAmount();
        if (outSpace < BATCH)
            return;
        if (!outTank.isEmpty()
            && !outTank.getFluid().getFluid().isSame(Fluids.LAVA))
            return;

        inTank.drain(BATCH, FluidAction.EXECUTE);
        outTank.fill(new FluidStack(Fluids.LAVA, BATCH), FluidAction.EXECUTE);
        setChanged();
    }

    @Override
    public float calculateStressApplied() {
        return 8f;
    }

    // =========================================
    //  SAVE / LOAD
    // =========================================
    @Override
    protected void write(CompoundTag tag, boolean client) {
        super.write(tag, client);
        tag.put("In", inTank.writeToNBT(new CompoundTag()));
        tag.put("Out", outTank.writeToNBT(new CompoundTag()));
        tag.putInt("T", timer);
    }

    @Override
    protected void read(CompoundTag tag, boolean client) {
        super.read(tag, client);
        inTank.readFromNBT(tag.getCompound("In"));
        outTank.readFromNBT(tag.getCompound("Out"));
        timer = tag.getInt("T");
    }

    // =========================================
    //  GOGGLES - LOOP YOK
    // =========================================
	private static final String INDENT = "    ";
	private static final String HALF = "  ";
	
	@Override
	public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
	    boolean added = super.addToGoggleTooltip(tooltip, isPlayerSneaking);
	
	    tooltip.add(Component.empty());
	    tooltip.add(Component.literal(INDENT + "\u00a76\u00a7lFilter Info"));
	    tooltip.add(Component.literal(INDENT + "\u00a77Water: \u00a7f"
	        + inTank.getFluidAmount() + "/" + inTank.getCapacity() + " mB"));
	    tooltip.add(Component.literal(INDENT + "\u00a77Lava:  \u00a7f"
	        + outTank.getFluidAmount() + "/" + outTank.getCapacity() + " mB"));
	
	    return true;
	}

    // =========================================
    //  CAPS
    // =========================================
    @Override
    public void onLoad() {
        super.onLoad();
        capUp = LazyOptional.of(InHandler::new);
        capDown = LazyOptional.of(OutHandler::new);
        capAll = LazyOptional.of(BothHandler::new);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        capUp.invalidate();
        capDown.invalidate();
        capAll.invalidate();
    }

    @Override
    public void reviveCaps() {
        super.reviveCaps();
        capUp = LazyOptional.of(InHandler::new);
        capDown = LazyOptional.of(OutHandler::new);
        capAll = LazyOptional.of(BothHandler::new);
    }

    @Override
    public <T> @NotNull LazyOptional<T> getCapability(
        @NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.FLUID_HANDLER) {
            if (side == Direction.UP)    return capUp.cast();
            if (side == Direction.DOWN)   return capDown.cast();
            if (side == null)             return capAll.cast();
            return LazyOptional.empty();
        }
        return super.getCapability(cap, side);
    }

    // =========================================
    //  PASİF PIPE BAĞLANTISI
    //  - Pressure YAPMIYOR
    //  - Pull/Push YAPMIYOR
    //  - Sadece pipe'ların buraya bağlanmasını
    //    sağlıyor
    //  - Harici pump akışı yönetiyor
    // =========================================
    private class FilterTransport extends FluidTransportBehaviour {

        public FilterTransport(SmartBlockEntity be) {
            super(be);
        }

        @Override
        public boolean canHaveFlowToward(BlockState state, Direction dir) {
            // Üstten ve alttan pipe bağlanabilir
            return dir == Direction.UP || dir == Direction.DOWN;
        }

        // tick() override YOK
        // pressure set YOK
        // parent tick halleder, biz karışmıyoruz
    }

    // =========================================
    //  FLUID HANDLERS
    // =========================================

    // Üstten: pump su iter → biz kabul ederiz
    private class InHandler implements IFluidHandler {
        public int getTanks() { return 1; }

        public @NotNull FluidStack getFluidInTank(int t) {
            return inTank.getFluid();
        }

        public int getTankCapacity(int t) {
            return inTank.getCapacity();
        }

        public boolean isFluidValid(int t, @NotNull FluidStack s) {
            return s.getFluid().isSame(Fluids.WATER);
        }

        public int fill(FluidStack r, FluidAction a) {
            if (r.getFluid().isSame(Fluids.WATER))
                return inTank.fill(r, a);
            return 0;
        }

        public @NotNull FluidStack drain(FluidStack s, FluidAction a) {
            return FluidStack.EMPTY;
        }

        public @NotNull FluidStack drain(int m, FluidAction a) {
            return FluidStack.EMPTY;
        }
    }

    // Alttan: pump lav çeker → biz veririz
    private class OutHandler implements IFluidHandler {
        public int getTanks() { return 1; }

        public @NotNull FluidStack getFluidInTank(int t) {
            return outTank.getFluid();
        }

        public int getTankCapacity(int t) {
            return outTank.getCapacity();
        }

        public boolean isFluidValid(int t, @NotNull FluidStack s) {
            return false;
        }

        public int fill(FluidStack r, FluidAction a) {
            return 0;
        }

        public @NotNull FluidStack drain(FluidStack r, FluidAction a) {
            if (r.getFluid().isSame(Fluids.LAVA))
                return outTank.drain(r.getAmount(), a);
            return FluidStack.EMPTY;
        }

        public @NotNull FluidStack drain(int m, FluidAction a) {
            return outTank.drain(m, a);
        }
    }

    // Goggles okuma için
    private class BothHandler implements IFluidHandler {
        public int getTanks() { return 2; }

        public @NotNull FluidStack getFluidInTank(int t) {
            return t == 0 ? inTank.getFluid() : outTank.getFluid();
        }

        public int getTankCapacity(int t) {
            return t == 0 ? inTank.getCapacity() : outTank.getCapacity();
        }

        public boolean isFluidValid(int t, @NotNull FluidStack s) {
            return t == 0 ? s.getFluid().isSame(Fluids.WATER)
                          : s.getFluid().isSame(Fluids.LAVA);
        }

        public int fill(FluidStack r, FluidAction a) {
            if (r.getFluid().isSame(Fluids.WATER))
                return inTank.fill(r, a);
            return 0;
        }

        public @NotNull FluidStack drain(FluidStack r, FluidAction a) {
            if (r.getFluid().isSame(Fluids.LAVA))
                return outTank.drain(r.getAmount(), a);
            return FluidStack.EMPTY;
        }

        public @NotNull FluidStack drain(int m, FluidAction a) {
            return outTank.drain(m, a);
        }
    }
}