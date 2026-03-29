package net.xenrao.cf.block;

import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.content.fluids.FluidTransportBehaviour;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.fluid.SmartFluidTank;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
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
import net.xenrao.cf.ModRegistry;
import net.xenrao.cf.init.CreateFantaModFluids;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class GasConverterReservoirBlockEntity extends SmartBlockEntity implements IHaveGoggleInformation {

    // ===== CONFIG =====
    private static final int FLUID_CAPACITY = 2000;
    private static final int LIMESTONE_PER_CONVERT = 3;
    private static final int CO2_PER_CONVERT = 50;
    private static final int PROCESS_TIME = 100;
    private static final int BLASTING_TIMEOUT = 5; // 5 tick içinde güncellenmezse false olur

    // ===== TAG =====
    private static final TagKey<Item> LIMESTONE_TAG =
        ItemTags.create(new ResourceLocation("create", "stone_types/limestone"));

    // ===== STATE =====
    private boolean isBlasting = false;
    private int blastingTimer = 0; // Mixin'den her tick güncellenir
    private int processTimer = 0;

    // ===== STORAGE =====
    private final ItemStackHandler inputSlot = new ItemStackHandler(1) {
        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return stack.is(LIMESTONE_TAG);
        }

        @Override
        public int getSlotLimit(int slot) {
            return 64;
        }

        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            notifyUpdate();
        }
    };

    private final SmartFluidTank co2Tank;

    // ===== CAPABILITIES =====
    private LazyOptional<IItemHandler> itemCap = LazyOptional.empty();
    private LazyOptional<IFluidHandler> fluidCap = LazyOptional.empty();

    public GasConverterReservoirBlockEntity(BlockPos pos, BlockState state) {
        super(ModRegistry.GAS_CONVERTER_BE.get(), pos, state);
        co2Tank = new SmartFluidTank(FLUID_CAPACITY, f -> {
            setChanged();
            notifyUpdate();
        });
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        //behaviours.add(new GasConverterTransport(this));
    }

    @Override
    public void onLoad() {
        super.onLoad();
        itemCap = LazyOptional.of(() -> inputSlot);
        fluidCap = LazyOptional.of(CO2OutputHandler::new);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        itemCap.invalidate();
        fluidCap.invalidate();
    }

    // ===== MİXİN'DEN ÇAĞRILACAK =====
    public void setBlasting(boolean blasting) {
        if (blasting) {
            this.isBlasting = true;
            this.blastingTimer = BLASTING_TIMEOUT;
        }
    }

    // ===== MAIN TICK =====
    public void serverTick() {
        if (level == null || level.isClientSide) return;

        // Blasting timeout kontrolü
        if (blastingTimer > 0) {
            blastingTimer--;
        } else {
            if (isBlasting) {
                isBlasting = false;
                updateActiveState(false);
                notifyUpdate();
            }
        }

        // BlockState güncelle
        if (isBlasting) {
            updateActiveState(true);
        }

        // İşlem
        if (isBlasting) {
            ItemStack input = inputSlot.getStackInSlot(0);

            if (!input.isEmpty() && canProduce()) {
                processTimer++;

                if (processTimer >= PROCESS_TIME) {
                    processTimer = 0;
                    convert();
                }
                notifyUpdate();
            } else {
                if (processTimer != 0) {
                    processTimer = 0;
                    notifyUpdate();
                }
            }
        } else {
            if (processTimer != 0) {
                processTimer = 0;
                notifyUpdate();
            }
        }
    }

    // ===== DÖNÜŞÜM =====
    private boolean canProduce() {
        int space = co2Tank.getCapacity() - co2Tank.getFluidAmount();
        if (space < CO2_PER_CONVERT) return false;

        if (!co2Tank.isEmpty()) {
            if (!co2Tank.getFluid().getFluid().isSame(CreateFantaModFluids.CO_2.get()))
                return false;
        }
        return true;
    }

    private void convert() {
        inputSlot.extractItem(0, LIMESTONE_PER_CONVERT, false);
        FluidStack co2 = new FluidStack(CreateFantaModFluids.CO_2.get(), CO2_PER_CONVERT);
        co2Tank.fill(co2, FluidAction.EXECUTE);
        setChanged();
        notifyUpdate();
    }

    // ===== SAĞ TIK =====
    public InteractionResult handleInteraction(Player player, InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);
        ItemStack current = inputSlot.getStackInSlot(0);

        if (held.isEmpty() && !current.isEmpty()) {
            if (!player.getInventory().add(current.copy())) {
                Containers.dropItemStack(level,
                    worldPosition.getX() + 0.5,
                    worldPosition.getY() + 1.0,
                    worldPosition.getZ() + 0.5,
                    current.copy());
            }
            inputSlot.setStackInSlot(0, ItemStack.EMPTY);
            notifyUpdate();
            return InteractionResult.SUCCESS;
        }

        if (!held.isEmpty() && inputSlot.isItemValid(0, held)) {
            ItemStack remaining = inputSlot.insertItem(0, held.copy(), false);
            if (!player.getAbilities().instabuild) {
                player.setItemInHand(hand, remaining);
            }
            notifyUpdate();
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }

    // ===== STATE =====
    private void updateActiveState(boolean active) {
        if (level == null) return;
        BlockState state = getBlockState();
        if (state.getValue(GasConverterReservoirBlock.ACTIVE) != active) {
            level.setBlockAndUpdate(worldPosition,
                state.setValue(GasConverterReservoirBlock.ACTIVE, active));
        }
    }

    // ===== DROP =====
    public void dropContents() {
        if (level == null) return;
        ItemStack stack = inputSlot.getStackInSlot(0);
        if (!stack.isEmpty()) {
            Containers.dropItemStack(level,
                worldPosition.getX() + 0.5,
                worldPosition.getY() + 0.5,
                worldPosition.getZ() + 0.5,
                stack.copy());
            inputSlot.setStackInSlot(0, ItemStack.EMPTY);
        }
    }

    // ===== COMPARATOR =====
    public int getComparatorOutput() {
        int fluidPercent = co2Tank.getCapacity() > 0
            ? (co2Tank.getFluidAmount() * 15) / co2Tank.getCapacity()
            : 0;
        return Math.min(fluidPercent, 15);
    }

    // ===== NBT =====
    @Override
    protected void write(CompoundTag tag, boolean client) {
        super.write(tag, client);
        tag.put("Input", inputSlot.serializeNBT());
        tag.put("CO2Tank", co2Tank.writeToNBT(new CompoundTag()));
        tag.putInt("ProcessTimer", processTimer);
        tag.putBoolean("IsBlasting", isBlasting);
    }

    @Override
    protected void read(CompoundTag tag, boolean client) {
        super.read(tag, client);
        inputSlot.deserializeNBT(tag.getCompound("Input"));
        co2Tank.readFromNBT(tag.getCompound("CO2Tank"));
        processTimer = tag.getInt("ProcessTimer");
        isBlasting = tag.getBoolean("IsBlasting");
    }

	// ===== GOGGLES =====
	@Override
	public boolean addToGoggleTooltip(List<Component> tooltip, boolean sneak) {
	
	    tooltip.add(Component.literal("    "
	        + Component.translatable("block.create_fanta.gas_converter_reservoir").getString()
	        + " Info: "));
	
	    ItemStack input = inputSlot.getStackInSlot(0);
	    int amount = co2Tank.getFluidAmount();
		
        if (!input.isEmpty()) {
            tooltip.add(Component.literal("     \u00a77" + input.getHoverName().getString() + " \u00a7ax" + input.getCount()));
        }

        // Amount'u 1,000 formatında göster
        String formattedAmount = String.format("%,d", amount);
        tooltip.add(Component.literal("     \u00a77CO\u2082 \u00a79" + formattedAmount + "mB"));
	    
	  	//tooltip.add(Component.empty());

	    if (isBlasting) {
	        tooltip.add(Component.literal("     \u00a7aBlasting Active \u2714"));
	    } else {
	        tooltip.add(Component.literal("     \u00a7cNo Blasting \u2716"));
	    }
	
	    if (isBlasting && !input.isEmpty() && canProduce()) {
	        int progressPercent = (processTimer * 100) / PROCESS_TIME;
	        tooltip.add(Component.literal("      \u00a7eProcessing: " + progressPercent + "%"));
	    }
	
	    return true;
	}

    // ===== GETTERS =====
    public boolean isCurrentlyBlasting() { return isBlasting; }
    public float getProcessProgress() { return PROCESS_TIME > 0 ? (float) processTimer / PROCESS_TIME : 0f; }
    public SmartFluidTank getCO2Tank() { return co2Tank; }

	public ItemStack getInputStack() {
	    return inputSlot.getStackInSlot(0);
	}
    // ===== CAPABILITY =====
	@Override
	public <T> @NotNull LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
	    if (cap == ForgeCapabilities.FLUID_HANDLER) {
	        // SADECE üstten fluid çıkışı
	        if (side == Direction.UP || side == null) return fluidCap.cast();
	        return LazyOptional.empty();
	    }
	
		if (cap == ForgeCapabilities.ITEM_HANDLER) {
	        // Item HER YÖNDEN girebilir
	        return itemCap.cast();
	    }
	
	    return super.getCapability(cap, side);
	}

    // ===== FLUID TRANSPORT =====
	private class GasConverterTransport extends FluidTransportBehaviour {
	    public GasConverterTransport(SmartBlockEntity be) {
	        super(be);
	    }
	
	    @Override
	    public boolean canHaveFlowToward(BlockState state, Direction dir) {
	        // Sadece üstten pipe bağlanabilir
	        return dir == Direction.UP;
	    }
	}

    // ===== CO₂ OUTPUT HANDLER =====
    private class CO2OutputHandler implements IFluidHandler {
        @Override public int getTanks() { return 1; }

        @Override
        public @NotNull FluidStack getFluidInTank(int tank) {
            return co2Tank.getFluid();
        }

        @Override
        public int getTankCapacity(int tank) {
            return co2Tank.getCapacity();
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
            if (resource.getFluid().isSame(CreateFantaModFluids.CO_2.get())) {
                return co2Tank.drain(resource.getAmount(), action);
            }
            return FluidStack.EMPTY;
        }

        @Override
        public @NotNull FluidStack drain(int maxDrain, FluidAction action) {
            return co2Tank.drain(maxDrain, action);
        }
    }
}