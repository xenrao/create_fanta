package net.xenrao.cf.block;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
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

        // Ponder uyumlu angle hesabı
        float angle = getAngleForBe(be, be.getBlockPos(), axis);

        SuperByteBuffer cog = CachedBuffers.partial(FilterPartialModels.FILTER_COG, state);

        ms.pushPose();
        ms.translate(0.5, 0.5, 0.5);

        switch (facing) {
            case UP -> {}
            case DOWN -> ms.mulPose(Axis.XP.rotationDegrees(180));
            case NORTH -> ms.mulPose(Axis.XP.rotationDegrees(-90));
            case SOUTH -> ms.mulPose(Axis.XP.rotationDegrees(90));
            case EAST -> ms.mulPose(Axis.ZP.rotationDegrees(-90));
            case WEST -> ms.mulPose(Axis.ZP.rotationDegrees(90));
        }

        float visualAngle = switch (facing) {
            case WEST, DOWN, NORTH -> -angle;
            default -> angle;
        };

        ms.mulPose(Axis.YP.rotationDegrees(visualAngle));

        ms.translate(-0.5, -0.5, -0.5);

        cog.light(light)
           .renderInto(ms, buffer.getBuffer(RenderType.solid()));

        ms.popPose();
    }

    private float getAngleForBe(FilterBlockEntity be, net.minecraft.core.BlockPos pos, Direction.Axis axis) {
        float time = AnimationTickHolder.getRenderTime(be.getLevel());
        float offset = getRotationOffsetForPosition(be, pos, axis);
        float speed = be.getSpeed();
        return (time * speed * 3f / 10f + offset) % 360;
    }

    @Override
    protected SuperByteBuffer getRotatedModel(FilterBlockEntity be, BlockState state) {
        return CachedBuffers.partialFacing(FilterPartialModels.FILTER_COG, state);
    }
}