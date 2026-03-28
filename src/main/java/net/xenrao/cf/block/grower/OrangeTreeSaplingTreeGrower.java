package net.xenrao.cf.block.grower;

import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.block.grower.AbstractTreeGrower;
import net.minecraft.util.RandomSource;
import net.minecraft.resources.ResourceKey;
import net.minecraft.data.worldgen.features.FeatureUtils;

public class OrangeTreeSaplingTreeGrower extends AbstractTreeGrower {
	protected ResourceKey<ConfiguredFeature<?, ?>> getConfiguredFeature(RandomSource randomSource, boolean hasFlower) {
		return FeatureUtils.createKey("create_fanta:orange_tree");
	}
}