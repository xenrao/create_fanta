package net.xenrao.cf;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.xenrao.cf.block.FilterBlock;
import net.xenrao.cf.block.FilterBlockEntity;
import net.xenrao.cf.block.GasConverterReservoirBlock;
import net.xenrao.cf.block.GasConverterReservoirBlockEntity;

public class ModRegistry {

    public static final DeferredRegister<Block> BLOCKS =
        DeferredRegister.create(ForgeRegistries.BLOCKS, CreateFantaMod.MODID);

    public static final DeferredRegister<Item> ITEMS =
        DeferredRegister.create(ForgeRegistries.ITEMS, CreateFantaMod.MODID);

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
        DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, CreateFantaMod.MODID);

    public static final RegistryObject<FilterBlock> FILTER_BLOCK =
        BLOCKS.register("filter_block",
            () -> new FilterBlock(BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .strength(3.0f)
                .sound(SoundType.METAL)
                .noOcclusion()));

    public static final RegistryObject<BlockItem> FILTER_BLOCK_ITEM =
        ITEMS.register("filter_block_item",
            () -> new BlockItem(FILTER_BLOCK.get(), new Item.Properties()));

    public static final RegistryObject<BlockEntityType<FilterBlockEntity>> FILTER_BE =
        BLOCK_ENTITIES.register("filter_block",
            () -> BlockEntityType.Builder
                .of(FilterBlockEntity::new, FILTER_BLOCK.get())
                .build(null));


	// ===== GAS CONVERTER RESERVOIR =====
	public static final RegistryObject<GasConverterReservoirBlock> GAS_CONVERTER_BLOCK =
	    BLOCKS.register("gas_converter_reservoir",
	        () -> new GasConverterReservoirBlock(BlockBehaviour.Properties.of()
	            .mapColor(MapColor.STONE)
	            .strength(3.5f)
	            .sound(SoundType.METAL)
	            .noOcclusion()));
	
	public static final RegistryObject<BlockItem> GAS_CONVERTER_BLOCK_ITEM =
	    ITEMS.register("gas_converter_reservoir",
	        () -> new BlockItem(GAS_CONVERTER_BLOCK.get(), new Item.Properties()));
	
	public static final RegistryObject<BlockEntityType<GasConverterReservoirBlockEntity>> GAS_CONVERTER_BE =
	    BLOCK_ENTITIES.register("gas_converter_reservoir",
	        () -> BlockEntityType.Builder
	            .of(GasConverterReservoirBlockEntity::new, GAS_CONVERTER_BLOCK.get())
	            .build(null));
}