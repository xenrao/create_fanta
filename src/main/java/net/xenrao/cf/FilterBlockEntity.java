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
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import net.xenrao.cf.ModRegistry;
import net.xenrao.cf.item.PulpFilterItem;

import java.util.List;
import net.minecraft.world.level.BlockAndTintGetter;

public class FilterBlockEntity extends KineticBlockEntity implements IHaveGoggleInformation {

    private static final int BUFFER = 10;
    private static final int BATCH = 5;
    private static final int LOSS = 2;
    private static final int BASE_TIME = 200;

 private int timer = BASE_TIME;
    private final SmartFluidTank inTank;
    private final SmartFluidTank outTank;

    // ===== FILTER SLOT =====
    private final ItemStackHandler filterSlot = new ItemStackHandler(1) {
        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return stack.getItem() instanceof PulpFilterItem;
        }

        @Override
        public int getSlotLimit(int slot) {
            return 1;
        }

        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            if (level != null && !level.isClientSide)
                level.updateNeighbourForOutputSignal(worldPosition, getBlockState().getBlock());
        }
    };

    private LazyOptional<IItemHandler> itemCap = LazyOptional.empty();
    private LazyOptional<IFluidHandler> inputFluidCap = LazyOptional.empty();
    private LazyOptional<IFluidHandler> outputFluidCap = LazyOptional.empty();
    private LazyOptional<IFluidHandler> allFluidCap = LazyOptional.empty();

    public FilterBlockEntity(BlockPos pos, BlockState state) {
        super(ModRegistry.FILTER_BE.get(), pos, state);
        inTank = new SmartFluidTank(BUFFER, f -> setChanged());
        outTank = new SmartFluidTank(BUFFER, f -> setChanged());
    }

    // ===== YÖN =====
    public Direction getFilterFacing() {
        if (getBlockState().getBlock() instanceof FilterBlock)
            return getBlockState().getValue(FilterBlock.FACING);
        return Direction.UP;
    }

    public Direction getInputSide() {
        return getFilterFacing().getOpposite();
    }

    public Direction getOutputSide() {
        return getFilterFacing();
    }

    // ===== BEHAVIOURS =====
    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        super.addBehaviours(behaviours);
        behaviours.add(new FilterTransport(this));
    }

    // ===== CAPS =====
    @Override
    public void onLoad() {
        super.onLoad();
        inputFluidCap = LazyOptional.of(InHandler::new);
        outputFluidCap = LazyOptional.of(OutHandler::new);
        allFluidCap = LazyOptional.of(BothHandler::new);
        itemCap = LazyOptional.of(() -> filterSlot);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        inputFluidCap.invalidate();
        outputFluidCap.invalidate();
        allFluidCap.invalidate();
        itemCap.invalidate();
    }

    @Override
    public void reviveCaps() {
        super.reviveCaps();
        inputFluidCap = LazyOptional.of(InHandler::new);
        outputFluidCap = LazyOptional.of(OutHandler::new);
        allFluidCap = LazyOptional.of(BothHandler::new);
        itemCap = LazyOptional.of(() -> filterSlot);
    }

    // ===== SAĞ TIK =====
    public InteractionResult handleInteraction(Player player, InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);
        ItemStack current = filterSlot.getStackInSlot(0);

        if (!current.isEmpty()) {
            if (!player.getInventory().add(current.copy())) {
                Containers.dropItemStack(level,
                    worldPosition.getX() + 0.5,
                    worldPosition.getY() + 1.0,
                    worldPosition.getZ() + 0.5,
                    current.copy());
            }
            filterSlot.setStackInSlot(0, ItemStack.EMPTY);
            notifyUpdate();
            return InteractionResult.SUCCESS;
        }

        if (!held.isEmpty() && held.getItem() instanceof PulpFilterItem) {
            ItemStack toInsert = held.copy();
            toInsert.setCount(1);
            filterSlot.setStackInSlot(0, toInsert);
            held.shrink(1);
            notifyUpdate();
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }

    // ===== COMPARATOR =====
    public int getComparatorOutput() {
        ItemStack filter = filterSlot.getStackInSlot(0);
        if (filter.isEmpty())
            return 0;

        int maxDamage = filter.getMaxDamage();
        if (maxDamage <= 0)
            return 15;

        int remaining = maxDamage - filter.getDamageValue();
        if (remaining <= 0)
            return 0;

        return Mth.clamp(
            (int) Math.ceil((double) remaining / maxDamage * 15.0),
            1, 15);
    }

    // ===== FİLTRE =====
    public boolean hasFilter() {
        ItemStack filter = filterSlot.getStackInSlot(0);
        if (filter.isEmpty())
            return false;
        if (filter.getMaxDamage() > 0 && filter.getDamageValue() >= filter.getMaxDamage())
            return false;
        return true;
    }

    public void dropFilter() {
        ItemStack filter = filterSlot.getStackInSlot(0);
        if (!filter.isEmpty() && level != null) {
            Containers.dropItemStack(level,
                worldPosition.getX() + 0.5,
                worldPosition.getY() + 0.5,
                worldPosition.getZ() + 0.5,
                filter.copy());
            filterSlot.setStackInSlot(0, ItemStack.EMPTY);
        }
    }

    // ===== TICK =====
    @Override
    public void tick() {
        super.tick();

        if (level == null || level.isClientSide)
            return;

        float spd = Math.abs(getSpeed());
        if (spd <= 0)
            return;

        if (!hasFilter())
            return;

        int rate = Mth.clamp((int) (spd / 16f), 1, 512);

        if (!inTank.isEmpty()
            && inTank.getFluid().getFluid().isSame(Fluids.WATER)) {
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
        outTank.fill(new FluidStack(Fluids.LAVA, BATCH - LOSS), FluidAction.EXECUTE);
        damageFilter();
        setChanged();
    }

    private void damageFilter() {
        ItemStack filter = filterSlot.getStackInSlot(0);
        if (filter.isEmpty())
            return;
        if (filter.getMaxDamage() <= 0)
            return;

        filter.setDamageValue(filter.getDamageValue() + 1);
        level.updateNeighbourForOutputSignal(worldPosition, getBlockState().getBlock());
        notifyUpdate();
    }

    @Override
    public float calculateStressApplied() {
        return 8f;
    }

    // ===== SAVE / LOAD =====
    @Override
    protected void write(CompoundTag tag, boolean client) {
        super.write(tag, client);
        tag.put("In", inTank.writeToNBT(new CompoundTag()));
        tag.put("Out", outTank.writeToNBT(new CompoundTag()));
        tag.putInt("T", timer);
        tag.put("Filter", filterSlot.serializeNBT());
    }

    @Override
    protected void read(CompoundTag tag, boolean client) {
        super.read(tag, client);
        inTank.readFromNBT(tag.getCompound("In"));
        outTank.readFromNBT(tag.getCompound("Out"));
        timer = tag.getInt("T");
        filterSlot.deserializeNBT(tag.getCompound("Filter"));
    }

    // ===== GOGGLES =====
	@Override
	public boolean addToGoggleTooltip(List<Component> tooltip, boolean sneak) {
	    boolean added = super.addToGoggleTooltip(tooltip, sneak);
	
	    tooltip.add(Component.empty());
		tooltip.add(Component.literal("    Filter Stats:"));
		
	    ItemStack filter = filterSlot.getStackInSlot(0);
	    if (filter.isEmpty()) {
	        tooltip.add(Component.literal("    \u00a7c\u00a7lNo Filter!"));
	        return true;
	    }
	
	    int maxDmg = filter.getMaxDamage();
	    int remaining = maxDmg - filter.getDamageValue();
	
	    if (remaining <= 0) {
	        tooltip.add(Component.literal("    \u00a7c\u00a7lFilter Broke!"));
	        return true;
	    }
	
	    int percent = (remaining * 100) / maxDmg;
	    int bars = (remaining * 50) / maxDmg;
	
	    StringBuilder bar = new StringBuilder();
	    bar.append("\u00a7a");
	    for (int i = 0; i < bars; i++) {
	        bar.append("|");
	    }
	    bar.append("\u00a78");
	    for (int i = bars; i < 50; i++) {
	        bar.append("|");
	    }
	    
	    tooltip.add(Component.literal("    " + bar + " \u00a7f" + percent + "%"));
	    return true;
	}

    // ===== CAPABILITY =====
    @Override
    public <T> @NotNull LazyOptional<T> getCapability(
        @NotNull Capability<T> cap, @Nullable Direction side) {

        if (cap == ForgeCapabilities.FLUID_HANDLER) {
            if (side == getInputSide())   return inputFluidCap.cast();
            if (side == getOutputSide())  return outputFluidCap.cast();
            if (side == null)             return allFluidCap.cast();
            return LazyOptional.empty();
        }

        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            return itemCap.cast();
        }

        return super.getCapability(cap, side);
    }

    // ===== PIPE BAĞLANTISI =====
    private class FilterTransport extends FluidTransportBehaviour {
        public FilterTransport(SmartBlockEntity be) {
            super(be);
        }
        
        @Override
        public boolean canHaveFlowToward(BlockState state, Direction dir) {
            return dir == getInputSide() || dir == getOutputSide();
        }

        @Override
		public AttachmentTypes getRenderedRimAttachment(BlockAndTintGetter world, BlockPos pos, BlockState state,
														Direction direction) {
															return AttachmentTypes.NONE;
														}
    }

    // ===== FLUID HANDLERS =====
    private class InHandler implements IFluidHandler {
        public int getTanks() { return 1; }
        public @NotNull FluidStack getFluidInTank(int t) { return inTank.getFluid(); }
        public int getTankCapacity(int t) { return inTank.getCapacity(); }
        public boolean isFluidValid(int t, @NotNull FluidStack s) {
            return s.getFluid().isSame(Fluids.WATER); }
        public int fill(FluidStack r, FluidAction a) {
            return r.getFluid().isSame(Fluids.WATER) ? inTank.fill(r, a) : 0; }
        public @NotNull FluidStack drain(FluidStack s, FluidAction a) {
            return FluidStack.EMPTY; }
        public @NotNull FluidStack drain(int m, FluidAction a) {
            return FluidStack.EMPTY; }
    }

    private class OutHandler implements IFluidHandler {
        public int getTanks() { return 1; }
        public @NotNull FluidStack getFluidInTank(int t) { return outTank.getFluid(); }
        public int getTankCapacity(int t) { return outTank.getCapacity(); }
        public boolean isFluidValid(int t, @NotNull FluidStack s) { return false; }
        public int fill(FluidStack r, FluidAction a) { return 0; }
        public @NotNull FluidStack drain(FluidStack r, FluidAction a) {
            return r.getFluid().isSame(Fluids.LAVA)
                ? outTank.drain(r.getAmount(), a) : FluidStack.EMPTY; }
        public @NotNull FluidStack drain(int m, FluidAction a) {
            return outTank.drain(m, a); }
    }

    private class BothHandler implements IFluidHandler {
        public int getTanks() { return 2; }
        public @NotNull FluidStack getFluidInTank(int t) {
            return t == 0 ? inTank.getFluid() : outTank.getFluid(); }
        public int getTankCapacity(int t) {
            return t == 0 ? inTank.getCapacity() : outTank.getCapacity(); }
        public boolean isFluidValid(int t, @NotNull FluidStack s) {
            return t == 0 ? s.getFluid().isSame(Fluids.WATER)
                          : s.getFluid().isSame(Fluids.LAVA); }
        public int fill(FluidStack r, FluidAction a) {
            return r.getFluid().isSame(Fluids.WATER) ? inTank.fill(r, a) : 0; }
        public @NotNull FluidStack drain(FluidStack r, FluidAction a) {
            return r.getFluid().isSame(Fluids.LAVA)
                ? outTank.drain(r.getAmount(), a) : FluidStack.EMPTY; }
        public @NotNull FluidStack drain(int m, FluidAction a) {
            return outTank.drain(m, a); }
    }
}