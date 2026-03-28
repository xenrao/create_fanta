package net.xenrao.cf.mixin;

import com.simibubi.create.content.kinetics.fan.AirCurrent;
import com.simibubi.create.content.kinetics.fan.IAirCurrentSource;
import com.simibubi.create.content.kinetics.fan.processing.AllFanProcessingTypes;
import com.simibubi.create.content.kinetics.fan.processing.FanProcessingType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.xenrao.cf.block.GasConverterReservoirBlockEntity;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = AirCurrent.class, remap = false)
public abstract class AirCurrentMixin {

    @Shadow @Final
    public IAirCurrentSource source;

    @Shadow
    public Direction direction;

    @Shadow
    public float maxDistance;

    @Shadow
    @Nullable
    public abstract FanProcessingType getTypeAt(float offset);

    @Inject(method = "tick", at = @At("TAIL"))
    private void createfanta$checkGasConverters(CallbackInfo ci) {
        if (source == null) return;

        Level world = source.getAirCurrentWorld();
        if (world == null || world.isClientSide) return;
        if (direction == null || maxDistance < 0.25f) return;

        BlockPos start = source.getAirCurrentPos();

        // Create'in getLimit() mantığı + 1 (havayı durduran bloğu da kontrol et)
        int limit;
        if ((float) (int) maxDistance == maxDistance) {
            limit = (int) maxDistance;
        } else {
            limit = (int) maxDistance + 1;
        }

        // Fan yönünde adım adım tara (findAffectedHandlers ile aynı mantık)
        for (int i = 1; i <= limit + 1; i++) {
            BlockPos pos = start.relative(direction, i);
            BlockEntity be = world.getBlockEntity(pos);

            if (be instanceof GasConverterReservoirBlockEntity reservoir) {
                // Bu pozisyondaki processing type'ı al (findAffectedHandlers ile aynı offset)
                FanProcessingType type = getTypeAt(i - 1);

                if (type == AllFanProcessingTypes.BLASTING) {
                    reservoir.setBlasting(true);
                }
            }
        }
    }
}