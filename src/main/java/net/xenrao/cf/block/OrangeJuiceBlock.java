package net.xenrao.cf.block;

import net.xenrao.cf.init.CreateFantaModFluids;

import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.LiquidBlock;

public class OrangeJuiceBlock extends LiquidBlock {
	public OrangeJuiceBlock() {
		super(() -> CreateFantaModFluids.ORANGE_JUICE.get(), BlockBehaviour.Properties.of().mapColor(MapColor.WATER).strength(100f).noCollission().noLootTable().liquid().pushReaction(PushReaction.DESTROY).sound(SoundType.EMPTY).replaceable());
	}
}