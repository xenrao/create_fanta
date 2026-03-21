package net.xenrao.cf.block;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
import net.createmod.catnip.animation.AnimationTickHolder;
import net.createmod.catnip.render.CachedBuffers;
import net.createmod.catnip.render.SuperByteBuffer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

public class FilterRenderer extends KineticBlockEntityRenderer<FilterBlockEntity> {

    public FilterRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
	protected void renderSafe(FilterBlockEntity be, float partialTicks,
	                           PoseStack ms, MultiBufferSource buffer,
	                           int light, int overlay) {
	
	    BlockState state = be.getBlockState();
	    Direction facing = state.getValue(FilterBlock.FACING);
	    Direction.Axis axis = facing.getAxis();
	
	    float speed = be.getSpeed();
	    float time = AnimationTickHolder.getRenderTime();
	    float offset = getRotationOffsetForPosition(be, be.getBlockPos(), axis);
	    float angleDeg = (time * speed * 3f / 10f + offset) % 360;
	
        // Kendi modelimizi kullan
        SuperByteBuffer cog = CachedBuffers.partial(
            FilterPartialModels.FILTER_COG, state);
	
	    ms.pushPose();
	    ms.translate(0.5, 0.5, 0.5);
	
	    switch (facing) {
	        case UP -> {}
	        case DOWN -> ms.mulPose(Axis.XP.rotationDegrees(180));
	        case NORTH -> ms.mulPose(Axis.XP.rotationDegrees(90));
	        case SOUTH -> ms.mulPose(Axis.XP.rotationDegrees(-90));
	        case EAST -> ms.mulPose(Axis.ZP.rotationDegrees(-90));
	        case WEST -> ms.mulPose(Axis.ZP.rotationDegrees(90));
	    }
	
	    ms.mulPose(Axis.YP.rotationDegrees(angleDeg));
	
	    ms.translate(-0.5, -0.45, -0.5);
	
	    cog.light(light)
	       .renderInto(ms, buffer.getBuffer(RenderType.solid()));
	
	    ms.popPose();
	}
}