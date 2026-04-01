package net.xenrao.cf.block;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.content.fluids.FluidPropagator;
import com.simibubi.create.content.fluids.FluidTransportBehaviour;
import com.simibubi.create.content.fluids.pipes.EncasedPipeBlock;
import com.simibubi.create.content.fluids.pump.PumpBlock;
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
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

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

import net.xenrao.cf.ModRecipes;
import net.xenrao.cf.ModRegistry;
import net.xenrao.cf.item.CreativePulpFilterItem;
import net.xenrao.cf.item.PulpFilterItem;
import net.xenrao.cf.recipe.FilteringRecipe;
import com.simibubi.create.content.kinetics.base.IRotate;

import java.util.List;

public class FilterBlockEntity extends KineticBlockEntity implements IHaveGoggleInformation  {

    private static final int BUFFER = 100;
    
    private int timer = 0;
    private int currentRecipeTime = 0;

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

    // ===== TARİF ARAMA =====
    @Nullable
    private FilteringRecipe findRecipe(FluidStack input) {
        if (level == null || input.isEmpty()) return null;
        for (FilteringRecipe r : level.getRecipeManager().getAllRecipesFor(ModRecipes.FILTERING_TYPE.get())) {
            if (r.matches(input)) return r;
        }
        return null;
    }

    /**
     * Herhangi bir filtering tarifinde input olarak kullanılabilen sıvı mı?
     */
    private boolean isValidInput(FluidStack stack) {
        if (level == null || stack.isEmpty()) return false;
        for (FilteringRecipe r : level.getRecipeManager().getAllRecipesFor(ModRecipes.FILTERING_TYPE.get())) {
            if (r.getIngredientFluid().getFluid().isSame(stack.getFluid())) return true;
        }
        return false;
    }

    // ===== SAĞ TIK =====
    public InteractionResult handleInteraction(Player player, InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);
        ItemStack current = filterSlot.getStackInSlot(0);

        if (!current.isEmpty() && held.isEmpty()) {
            if (!player.getInventory().add(current.copy())) {
                Containers.dropItemStack(level,
                    worldPosition.getX() + 0.5,
                    worldPosition.getY() + 1.0,
                    worldPosition.getZ() + 0.5,
                    current.copy());
            }
            filterSlot.setStackInSlot(0, ItemStack.EMPTY);
            updateFilterState(false);
            notifyUpdate();
            return InteractionResult.SUCCESS;
        }

        if (!held.isEmpty() && current.isEmpty()
            && (held.getItem() instanceof PulpFilterItem || held.getItem() instanceof CreativePulpFilterItem)) {
            ItemStack toInsert = held.copy();
            toInsert.setCount(1);
            filterSlot.setStackInSlot(0, toInsert);
            if (!player.getAbilities().instabuild) held.shrink(1);
            updateFilterState(true);
            notifyUpdate();
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }

    private void updateFilterState(boolean hasFilter) {
        if (level == null) return;
        BlockState state = getBlockState();
        if (state.getValue(FilterBlock.HAS_FILTER) != hasFilter) {
            level.setBlockAndUpdate(worldPosition, state.setValue(FilterBlock.HAS_FILTER, hasFilter));
        }
    }

    // ===== COMPARATOR =====
    public int getComparatorOutput() {
        ItemStack filter = filterSlot.getStackInSlot(0);
        if (filter.getItem() instanceof CreativePulpFilterItem) return 15;
        if (filter.isEmpty()) return 0;

        int maxDamage = filter.getMaxDamage();
        if (maxDamage <= 0) return 15;

        int remaining = maxDamage - filter.getDamageValue();
        if (remaining <= 0) return 0;

        return Mth.clamp(
            (int) Math.ceil((double) remaining / maxDamage * 15.0),
            1, 15);
    }

    // ===== FİLTRE =====
    public boolean hasFilter() {
        ItemStack filter = filterSlot.getStackInSlot(0);
        if (filter.getItem() instanceof CreativePulpFilterItem) return true;
        if (filter.isEmpty()) return false;
        if (filter.getMaxDamage() > 0 && filter.getDamageValue() >= filter.getMaxDamage()) return false;
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

        if (level == null || level.isClientSide) return;

        float spd = Math.abs(getSpeed());
        if (spd <= 0) return;
        if (!hasFilter()) return;

        if (hasFlowPressure()) {
            pullFromNeighbour();
            pushToNeighbour();
        }

        // Tarif bul
        FilteringRecipe recipe = findRecipe(inTank.getFluid());
        if (recipe == null) {
            timer = 0;
            return;
        }

        // Yeterli input var mı?
        if (inTank.getFluidAmount() < recipe.getIngredientAmount()) {
            timer = 0;
            return;
        }

        // Output'a yer var mı?
        if (!canOutput(recipe)) {
            timer = 0;
            return;
        }

        currentRecipeTime = recipe.getProcessingTime();
        int rate = Mth.clamp((int) (spd / 16f), 1, 512);

        timer += rate;
        if (timer >= currentRecipeTime) {
            timer = 0;
            convert(recipe);
        }
        if (level.getGameTime() % 5 == 0) {
		    notifyUpdate();
		}
    }

    private boolean canOutput(FilteringRecipe recipe) {
        FluidStack result = recipe.getResultFluid();
        int space = outTank.getCapacity() - outTank.getFluidAmount();
        if (space < result.getAmount()) return false;
        return outTank.isEmpty() || outTank.getFluid().getFluid().isSame(result.getFluid());
    }

    private boolean hasFlowPressure() {
        FluidTransportBehaviour transport = getBehaviour(FluidTransportBehaviour.TYPE);
        if (transport == null) return false;
        return transport.hasAnyPressure();
    }

    private void pullFromNeighbour() {
        if (level == null || level.isClientSide) return;

        Direction inputDir = getInputSide();
        BlockPos neighbourPos = worldPosition.relative(inputDir);

        BlockEntity neighbour = level.getBlockEntity(neighbourPos);
        if (neighbour == null) return;

        FluidTransportBehaviour transport = BlockEntityBehaviour.get(level, neighbourPos, FluidTransportBehaviour.TYPE);
        if (transport != null) return;

        neighbour.getCapability(ForgeCapabilities.FLUID_HANDLER, inputDir.getOpposite()).ifPresent(handler -> {
            int space = inTank.getCapacity() - inTank.getFluidAmount();
            if (space <= 0) return;

            // inTank boş değilse sadece aynı sıvıyı çek
            FluidStack simulated;
            if (!inTank.isEmpty()) {
                simulated = handler.drain(
                    new FluidStack(inTank.getFluid().getFluid(), Math.min(space, 50)),
                    FluidAction.SIMULATE
                );
            } else {
                simulated = handler.drain(Math.min(space, 50), FluidAction.SIMULATE);
            }

            if (simulated.isEmpty()) return;
            if (!isValidInput(simulated)) return;

            FluidStack drained = handler.drain(simulated, FluidAction.EXECUTE);
            if (!drained.isEmpty()) {
                inTank.fill(drained, FluidAction.EXECUTE);
            }
        });
    }

    private void pushToNeighbour() {
        if (level == null || level.isClientSide) return;

        Direction outputDir = getOutputSide();
        BlockPos neighbourPos = worldPosition.relative(outputDir);

        BlockEntity neighbour = level.getBlockEntity(neighbourPos);
        if (neighbour == null) return;

        FluidTransportBehaviour transport = BlockEntityBehaviour.get(level, neighbourPos, FluidTransportBehaviour.TYPE);
        if (transport != null) return;

        if (outTank.isEmpty()) return;

        neighbour.getCapability(ForgeCapabilities.FLUID_HANDLER, outputDir.getOpposite()).ifPresent(handler -> {
            FluidStack available = outTank.getFluid().copy();
            available.setAmount(Math.min(available.getAmount(), 50));

            int filled = handler.fill(available, FluidAction.EXECUTE);
            if (filled > 0) {
                outTank.drain(filled, FluidAction.EXECUTE);
            }
        });
    }

    // ===== DÖNÜŞÜM =====
    private void convert(FilteringRecipe recipe) {
        inTank.drain(recipe.getIngredientAmount(), FluidAction.EXECUTE);
        outTank.fill(recipe.getResultFluid(), FluidAction.EXECUTE);
        damageFilter();
        setChanged();
    }

    private void damageFilter() {
        ItemStack filter = filterSlot.getStackInSlot(0);
        if (filter.getItem() instanceof CreativePulpFilterItem) return;
        if (filter.isEmpty()) return;
        if (filter.getMaxDamage() <= 0) return;

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
	    tag.putInt("RT", currentRecipeTime);
	    tag.put("Filter", filterSlot.serializeNBT());
	}
	
	@Override
	protected void read(CompoundTag tag, boolean client) {
	    super.read(tag, client);
	    inTank.readFromNBT(tag.getCompound("In"));
	    outTank.readFromNBT(tag.getCompound("Out"));
	    timer = tag.getInt("T");
	    currentRecipeTime = tag.getInt("RT");
	    filterSlot.deserializeNBT(tag.getCompound("Filter"));
	
	    if (level != null && !level.isClientSide) {
	        updateFilterState(!filterSlot.getStackInSlot(0).isEmpty());
	    }
	}

    // ===== GOGGLES =====
    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean sneak) {
        boolean added = super.addToGoggleTooltip(tooltip, sneak);

        tooltip.add(Component.empty());
        tooltip.add(Component.literal("    Filter Stats:"));

        ItemStack filter = filterSlot.getStackInSlot(0);

        if (filter.getItem() instanceof CreativePulpFilterItem) {
            StringBuilder bar = new StringBuilder();
            bar.append("\u00a7a");
            for (int i = 0; i < 50; i++) bar.append("|");
            tooltip.add(Component.literal("    " + bar + " \u00a7f100%"));

            addFluidInfo(tooltip);
            return true;
        }

        if (filter.isEmpty()) {
            tooltip.add(Component.literal("    \u00a7c\u00a7lNo Filter!"));
            addFluidInfo(tooltip);
            return true;
        }

        int maxDmg = filter.getMaxDamage();
        int remaining = maxDmg - filter.getDamageValue();

        if (remaining <= 0) {
            tooltip.add(Component.literal("    \u00a7c\u00a7lFilter Broke!"));
            addFluidInfo(tooltip);
            return true;
        }

        int percent = Math.max((remaining * 100) / maxDmg, 1);
        int bars = Math.max((remaining * 50) / maxDmg, 1);

        StringBuilder bar = new StringBuilder();
        bar.append("\u00a7a");
        for (int i = 0; i < bars; i++) bar.append("|");
        bar.append("\u00a78");
        for (int i = bars; i < 50; i++) bar.append("|");

        tooltip.add(Component.literal("    " + bar + " \u00a7f" + percent + "%"));

        addFluidInfo(tooltip);
        return true;
    }

    private void addFluidInfo(List<Component> tooltip) {
    	/*
        if (!inTank.isEmpty()) {
            String name = inTank.getFluid().getDisplayName().getString();
            String amount = String.format("%,d", inTank.getFluidAmount());
            tooltip.add(Component.literal("    \u00a77In: " + name + " \u00a79" + amount + "mB"));
        }
        if (!outTank.isEmpty()) {
            String name = outTank.getFluid().getDisplayName().getString();
            String amount = String.format("%,d", outTank.getFluidAmount());
            tooltip.add(Component.literal("    \u00a77Out: " + name + " \u00a79" + amount + "mB"));
        }
*/
        // İşlem yüzdesi
        if (hasFilter() && !inTank.isEmpty() && currentRecipeTime > 0 && timer > 0) {
            int percent = Math.min((timer * 100) / currentRecipeTime, 100);
            tooltip.add(Component.literal("     \u00a7eProcessing: " + percent + "%"));
        }
    }

    // ===== CAPABILITY =====
    @Override
    public <T> @NotNull LazyOptional<T> getCapability(
        @NotNull Capability<T> cap, @Nullable Direction side) {

        if (cap == ForgeCapabilities.FLUID_HANDLER) {
            if (side == getInputSide()) return inputFluidCap.cast();
            if (side == getOutputSide()) return outputFluidCap.cast();
            if (side == null) return allFluidCap.cast();
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
        public AttachmentTypes getRenderedRimAttachment(BlockAndTintGetter world, BlockPos pos,
                                                         BlockState state, Direction direction) {
            if (!canHaveFlowToward(state, direction))
                return AttachmentTypes.NONE;

            BlockPos offsetPos = pos.relative(direction);
            BlockState facingState = world.getBlockState(offsetPos);

            if (facingState.getBlock() instanceof PumpBlock
                && facingState.getValue(PumpBlock.FACING) == direction.getOpposite())
                return AttachmentTypes.NONE;

            if (AllBlocks.ENCASED_FLUID_PIPE.has(facingState)
                && facingState.getValue(EncasedPipeBlock.FACING_TO_PROPERTY_MAP.get(direction.getOpposite())))
                return AttachmentTypes.RIM;

            if (FluidPropagator.hasFluidCapability(world, offsetPos, direction.getOpposite())
                && !AllBlocks.HOSE_PULLEY.has(facingState))
                return AttachmentTypes.DRAIN;

            FluidTransportBehaviour neighbourPipe =
                BlockEntityBehaviour.get(world, offsetPos, FluidTransportBehaviour.TYPE);
            if (neighbourPipe != null
                && neighbourPipe.canHaveFlowToward(facingState, direction.getOpposite()))
                return AttachmentTypes.RIM;

            return AttachmentTypes.NONE;
        }
    }

    // ===== FLUID HANDLERS =====
    private class InHandler implements IFluidHandler {
        public int getTanks() { return 1; }
        public @NotNull FluidStack getFluidInTank(int t) { return inTank.getFluid(); }
        public int getTankCapacity(int t) { return inTank.getCapacity(); }

        public boolean isFluidValid(int t, @NotNull FluidStack s) {
            return isValidInput(s);
        }

        public int fill(FluidStack r, FluidAction a) {
            if (!isValidInput(r)) return 0;
            // Tank'ta farklı sıvı varsa kabul etme
            if (!inTank.isEmpty() && !inTank.getFluid().getFluid().isSame(r.getFluid())) return 0;
            return inTank.fill(r, a);
        }

        public @NotNull FluidStack drain(FluidStack s, FluidAction a) { return FluidStack.EMPTY; }
        public @NotNull FluidStack drain(int m, FluidAction a) { return FluidStack.EMPTY; }
    }

    private class OutHandler implements IFluidHandler {
        public int getTanks() { return 1; }
        public @NotNull FluidStack getFluidInTank(int t) { return outTank.getFluid(); }
        public int getTankCapacity(int t) { return outTank.getCapacity(); }
        public boolean isFluidValid(int t, @NotNull FluidStack s) { return false; }
        public int fill(FluidStack r, FluidAction a) { return 0; }

        public @NotNull FluidStack drain(FluidStack r, FluidAction a) {
            if (r.isEmpty() || outTank.isEmpty()) return FluidStack.EMPTY;
            if (!r.getFluid().isSame(outTank.getFluid().getFluid())) return FluidStack.EMPTY;
            return outTank.drain(r.getAmount(), a);
        }

        public @NotNull FluidStack drain(int m, FluidAction a) {
            return outTank.drain(m, a);
        }
    }

    private class BothHandler implements IFluidHandler {
        public int getTanks() { return 2; }

        public @NotNull FluidStack getFluidInTank(int t) {
            return t == 0 ? inTank.getFluid() : outTank.getFluid();
        }

        public int getTankCapacity(int t) {
            return t == 0 ? inTank.getCapacity() : outTank.getCapacity();
        }

        public boolean isFluidValid(int t, @NotNull FluidStack s) {
            return t == 0 && isValidInput(s);
        }

        public int fill(FluidStack r, FluidAction a) {
            if (!isValidInput(r)) return 0;
            if (!inTank.isEmpty() && !inTank.getFluid().getFluid().isSame(r.getFluid())) return 0;
            return inTank.fill(r, a);
        }

        public @NotNull FluidStack drain(FluidStack r, FluidAction a) {
            if (r.isEmpty() || outTank.isEmpty()) return FluidStack.EMPTY;
            if (!r.getFluid().isSame(outTank.getFluid().getFluid())) return FluidStack.EMPTY;
            return outTank.drain(r.getAmount(), a);
        }

        public @NotNull FluidStack drain(int m, FluidAction a) {
            return outTank.drain(m, a);
        }
    }
}