package net.xenrao.cf.block;

import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.content.kinetics.fan.processing.AllFanProcessingTypes;
import com.simibubi.create.content.kinetics.fan.processing.FanProcessingType;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.fluid.SmartFluidTank;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
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
import net.xenrao.cf.recipe.GasConverterHeatingRecipe;

import java.util.List;

public class GasConverterReservoirBlockEntity extends SmartBlockEntity implements IHaveGoggleInformation {

    private static final int FLUID_CAPACITY = 2000;
    private static final int FAN_TIMEOUT = 5;

    public enum BasinProcessingMode {
        HEATING,
        COOLING,
        WARMING,
        HAUNTING;

        public String getDisplayName() {
            return switch (this) {
                case HEATING -> "Heating";
                case COOLING -> "Cooling";
                case WARMING -> "Warming";
                case HAUNTING -> "Haunting";
            };
        }

        public String getColor() {
            return switch (this) {
                case HEATING -> "\u00a7c";
                case COOLING -> "\u00a76";
                case WARMING -> "\u00a7e";
                case HAUNTING -> "\u00a75";
            };
        }
    }

    private int fanTimer = 0;
    private int processTimer = 0;

    @Nullable
    private FanProcessingType activeFanType = null;

    @Nullable
    private BasinProcessingMode activeMode = null;

    private final ItemStackHandler inputSlot = new ItemStackHandler(1) {
        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return findHeatingRecipe(stack) != null;
        }

        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            notifyUpdate();
        }
    };

    private final SmartFluidTank outTank;

    private LazyOptional<IItemHandler> itemCap = LazyOptional.empty();
    private LazyOptional<IFluidHandler> fluidCap = LazyOptional.empty();

    public GasConverterReservoirBlockEntity(BlockPos pos, BlockState state) {
        super(ModRegistry.GAS_CONVERTER_BE.get(), pos, state);
        outTank = new SmartFluidTank(FLUID_CAPACITY, f -> {
            setChanged();
            notifyUpdate();
        });
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {}

    @Override
    public void onLoad() {
        super.onLoad();
        itemCap = LazyOptional.of(() -> inputSlot);
        fluidCap = LazyOptional.of(OutputFluidHandler::new);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        itemCap.invalidate();
        fluidCap.invalidate();
    }

public void setFanProcessing(@NotNull FanProcessingType type) {
    BasinProcessingMode newMode = mapFanType(type);
    
    // Mode değiştiyse client'a bildir
    if (this.activeMode != newMode) {
        this.activeMode = newMode;
        notifyUpdate();
    }
    
    this.activeFanType = type;
    this.fanTimer = FAN_TIMEOUT;
}

    @Nullable
    private BasinProcessingMode mapFanType(FanProcessingType type) {
        if (type == AllFanProcessingTypes.BLASTING) return BasinProcessingMode.HEATING;
        if (type == AllFanProcessingTypes.SPLASHING) return BasinProcessingMode.COOLING;
        if (type == AllFanProcessingTypes.SMOKING) return BasinProcessingMode.WARMING;
        if (type == AllFanProcessingTypes.HAUNTING) return BasinProcessingMode.HAUNTING;
        return null;
    }

    private boolean isImplemented(@Nullable BasinProcessingMode mode) {
        if (mode == null) return false;

        return switch (mode) {
            case HEATING -> true;
            case COOLING -> false;
            case WARMING -> false;
            case HAUNTING -> false;
        };
    }

    public void serverTick() {
        if (level == null || level.isClientSide) return;

        if (fanTimer > 0) {
            fanTimer--;
        } else {
            if (activeFanType != null || activeMode != null || processTimer != 0) {
                activeFanType = null;
                activeMode = null;
                processTimer = 0;
                updateActiveState(false);
                notifyUpdate();
            }
            return;
        }

        if (activeMode == null) {
            if (processTimer != 0) {
                processTimer = 0;
                notifyUpdate();
            }
            updateActiveState(false);
            return;
        }

        if (!isImplemented(activeMode)) {
            if (processTimer != 0) {
                processTimer = 0;
                notifyUpdate();
            }
            updateActiveState(false);
            return;
        }

        switch (activeMode) {
            case HEATING -> tickHeating();
            case COOLING, WARMING, HAUNTING -> {
                if (processTimer != 0) {
                    processTimer = 0;
                    notifyUpdate();
                }
                updateActiveState(false);
            }
        }
    }

    private void tickHeating() {
        updateActiveState(true);

        ItemStack in = inputSlot.getStackInSlot(0);
        GasConverterHeatingRecipe recipe = findHeatingRecipe(in);

        if (recipe == null || in.getCount() < recipe.getIngredientCount() || !canProduce(recipe)) {
            if (processTimer != 0) {
                processTimer = 0;
                notifyUpdate();
            }
            return;
        }

        processTimer++;

        if (processTimer % 5 == 0) {
            notifyUpdate();
        }

        if (processTimer >= recipe.getProcessingTime()) {
            processTimer = 0;
            craftHeating(recipe);
        }
    }

    private boolean canProduce(GasConverterHeatingRecipe recipe) {
        FluidStack result = recipe.getResultFluid();
        int space = outTank.getCapacity() - outTank.getFluidAmount();
        if (space < result.getAmount()) return false;
        return outTank.isEmpty() || outTank.getFluid().getFluid().isSame(result.getFluid());
    }

    private void craftHeating(GasConverterHeatingRecipe recipe) {
        inputSlot.extractItem(0, recipe.getIngredientCount(), false);
        outTank.fill(recipe.getResultFluid(), FluidAction.EXECUTE);
        setChanged();
        notifyUpdate();
    }

    @Nullable
    private GasConverterHeatingRecipe findHeatingRecipe(ItemStack stack) {
        if (level == null || stack.isEmpty()) return null;

        for (GasConverterHeatingRecipe r : level.getRecipeManager().getAllRecipesFor(ModRecipes.HEATING_TYPE.get())) {
            if (r.getIngredient().test(stack)) return r;
        }

        return null;
    }

    public InteractionResult handleInteraction(Player player, InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);
        ItemStack current = inputSlot.getStackInSlot(0);

        if (held.isEmpty() && !current.isEmpty()) {
            if (!player.getInventory().add(current.copy())) {
                Containers.dropItemStack(
                    level,
                    worldPosition.getX() + 0.5,
                    worldPosition.getY() + 1.0,
                    worldPosition.getZ() + 0.5,
                    current.copy()
                );
            }
            inputSlot.setStackInSlot(0, ItemStack.EMPTY);
            notifyUpdate();
            return InteractionResult.SUCCESS;
        }

        if (!held.isEmpty() && inputSlot.isItemValid(0, held)) {
            ItemStack remaining = inputSlot.insertItem(0, held.copy(), false);
            if (!player.getAbilities().instabuild)
                player.setItemInHand(hand, remaining);
            notifyUpdate();
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }

    private void updateActiveState(boolean active) {
        if (level == null) return;
        BlockState state = getBlockState();
        if (state.getValue(GasConverterReservoirBlock.ACTIVE) != active) {
            level.setBlockAndUpdate(worldPosition, state.setValue(GasConverterReservoirBlock.ACTIVE, active));
        }
    }

    public void dropContents() {
        if (level == null) return;
        ItemStack stack = inputSlot.getStackInSlot(0);
        if (!stack.isEmpty()) {
            Containers.dropItemStack(
                level,
                worldPosition.getX() + 0.5,
                worldPosition.getY() + 0.5,
                worldPosition.getZ() + 0.5,
                stack.copy()
            );
            inputSlot.setStackInSlot(0, ItemStack.EMPTY);
        }
    }

    public int getComparatorOutput() {
        int cap = outTank.getCapacity();
        if (cap <= 0) return 0;
        return Math.min(15, (outTank.getFluidAmount() * 15) / cap);
    }

    public ItemStack getInputStack() {
        return inputSlot.getStackInSlot(0);
    }

    public SmartFluidTank getCO2Tank() {
        return outTank;
    }

    @Nullable
    public BasinProcessingMode getActiveMode() {
        return activeMode;
    }

    public float getProcessProgress() {
        if (activeMode != BasinProcessingMode.HEATING) return 0f;

        ItemStack input = inputSlot.getStackInSlot(0);
        GasConverterHeatingRecipe recipe = findHeatingRecipe(input);
        if (recipe == null || recipe.getProcessingTime() <= 0) return 0f;

        return (float) processTimer / recipe.getProcessingTime();
    }

    @Override
    protected void write(CompoundTag tag, boolean client) {
        super.write(tag, client);
        tag.put("Input", inputSlot.serializeNBT());
        tag.put("CO2Tank", outTank.writeToNBT(new CompoundTag()));
        tag.putInt("ProcessTimer", processTimer);

        if (activeMode != null) {
            tag.putString("ActiveMode", activeMode.name());
        }
    }

    @Override
    protected void read(CompoundTag tag, boolean client) {
        super.read(tag, client);
        inputSlot.deserializeNBT(tag.getCompound("Input"));
        outTank.readFromNBT(tag.getCompound("CO2Tank"));
        processTimer = tag.getInt("ProcessTimer");

        if (tag.contains("ActiveMode")) {
            try {
                activeMode = BasinProcessingMode.valueOf(tag.getString("ActiveMode"));
            } catch (IllegalArgumentException e) {
                activeMode = null;
            }
        } else {
            activeMode = null;
        }
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean sneak) {
        tooltip.add(Component.literal("    "
            + Component.translatable("block.create_fanta.refinery_basin").getString()
            + " Info:"));

        ItemStack input = inputSlot.getStackInSlot(0);
        int amount = outTank.getFluidAmount();

        if (!input.isEmpty()) {
            tooltip.add(Component.literal("     \u00a77"
                + input.getHoverName().getString()
                + " \u00a7ax" + input.getCount()));
        }

        if (amount > 0) {
            String fluidName = outTank.getFluid().getDisplayName().getString();
            String formattedAmount = String.format("%,d", amount);
            tooltip.add(Component.literal("     \u00a77" + fluidName + " \u00a79" + formattedAmount + "mB"));
        }

        if (activeMode != null) {
            if (isImplemented(activeMode)) {
                tooltip.add(Component.literal("     "
                    + activeMode.getColor()
                    + activeMode.getDisplayName()));
            } else {
                tooltip.add(Component.literal("     \u00a7e"
                    + activeMode.getDisplayName()
                    + " (not yet supported) \u26A0"));
            }
        } else {
            tooltip.add(Component.literal("     \u00a7aIdle"));
        }

        if (activeMode == BasinProcessingMode.HEATING && !input.isEmpty()) {
            GasConverterHeatingRecipe recipe = findHeatingRecipe(input);
            if (recipe != null && canProduce(recipe)) {
                int time = recipe.getProcessingTime();
                int percent = time > 0 ? (processTimer * 100) / time : 0;
                tooltip.add(Component.literal("      \u00a7eProcessing: " + percent + "%"));
            }
        }

        return true;
    }

    @Override
    public <T> @NotNull LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.FLUID_HANDLER) {
            if (side == Direction.UP || side == null)
                return fluidCap.cast();
            return LazyOptional.empty();
        }

        if (cap == ForgeCapabilities.ITEM_HANDLER)
            return itemCap.cast();

        return super.getCapability(cap, side);
    }

    private class OutputFluidHandler implements IFluidHandler {
        @Override
        public int getTanks() {
            return 1;
        }

        @Override
        public @NotNull FluidStack getFluidInTank(int tank) {
            return outTank.getFluid();
        }

        @Override
        public int getTankCapacity(int tank) {
            return outTank.getCapacity();
        }

        @Override
        public boolean isFluidValid(int tank, @NotNull FluidStack stack) {
            return false;
        }

        @Override
        public int fill(FluidStack resource, FluidAction action) {
            return 0;
        }

        @Override
        public @NotNull FluidStack drain(FluidStack resource, FluidAction action) {
            if (resource.isEmpty()) return FluidStack.EMPTY;
            if (!resource.getFluid().isSame(outTank.getFluid().getFluid())) return FluidStack.EMPTY;
            return outTank.drain(resource.getAmount(), action);
        }

        @Override
        public @NotNull FluidStack drain(int maxDrain, FluidAction action) {
            return outTank.drain(maxDrain, action);
        }
    }
}