package net.xenrao.cf.block;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.createmod.catnip.animation.AnimationTickHolder;
import net.createmod.catnip.platform.ForgeCatnipServices;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

public class GasConverterReservoirRenderer implements BlockEntityRenderer<GasConverterReservoirBlockEntity> {

    public GasConverterReservoirRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(GasConverterReservoirBlockEntity be, float partialTicks,
                       PoseStack ms, MultiBufferSource buffer,
                       int light, int overlay) {
        float fluidLevel = renderFluid(be, ms, buffer, light);
        renderItem(be, fluidLevel, partialTicks, ms, buffer, light, overlay);
    }

    // ===== FLUID RENDER (Create'in kendi renderer'ı) =====
    private float renderFluid(GasConverterReservoirBlockEntity be,
                              PoseStack ms, MultiBufferSource buffer, int light) {

        FluidStack fluidStack = be.getCO2Tank().getFluid();
        if (fluidStack.isEmpty()) return 0;

        int amount = fluidStack.getAmount();
        int capacity = be.getCO2Tank().getCapacity();
        if (amount <= 0 || capacity <= 0) return 0;

        // Basin tarzı seviye eğrisi
        float fluidLevel = Mth.clamp((float) amount / capacity, 0, 1);
        fluidLevel = 1 - ((1 - fluidLevel) * (1 - fluidLevel));

        // İç duvar sınırları
        float xMin = 2f / 16f;
        float xMax = 14f / 16f;
        float yMin = 2f / 16f;
        float yMax = yMin + (12f / 16f) * fluidLevel;
        float zMin = 2f / 16f;
        float zMax = 14f / 16f;

        // Create'in kendi fluid renderer'ı - depth sorununu otomatik çözer
        ForgeCatnipServices.FLUID_RENDERER.renderFluidBox(
            fluidStack,
            xMin, yMin, zMin,
            xMax, yMax, zMax,
            buffer, ms, light,
            false,  // flipY
            false   // renderBottom
        );

        return yMax;
    }

	// ===== ITEM RENDER =====
	private void renderItem(GasConverterReservoirBlockEntity be, float fluidLevel,
	                        float partialTicks, PoseStack ms, MultiBufferSource buffer,
	                        int light, int overlay) {
	
	    ItemStack stack = be.getInputStack();
	    if (stack.isEmpty()) return;
	
	    int totalCount = stack.getCount();
	    int modelsToRender = Math.max(1, totalCount / 1);
	    modelsToRender = Math.min(modelsToRender, 64);
	
	    // Item için sıvı seviyesi: gerçek amount'un max %75'i kadar yükselir
	    int amount = be.getCO2Tank().getFluidAmount();
	    int capacity = be.getCO2Tank().getCapacity();
	    int cappedAmount = Math.min(amount, (int)(capacity * 0.70f));
	
	    float itemFluidLevel = Mth.clamp((float) cappedAmount / capacity, 0, 1);
	    itemFluidLevel = 1 - ((1 - itemFluidLevel) * (1 - itemFluidLevel));
	
	    // Sıvı seviyesiyle birebir yükselir ama %75'te durur
	    float yMin = 2f / 16f;
	    float yRange = 12f / 16f;
	    float baseItemY = yMin + yRange * itemFluidLevel;
	
	    // Sıvı yokken minimum yükseklik
	    if (amount <= 0) {
	        baseItemY = 3f / 16f;
	    }
	
	    long seed = be.getBlockPos().asLong();
	
	    for (int i = 0; i < modelsToRender; i++) {
	        ms.pushPose();
	
	        java.util.Random rand = new java.util.Random(seed + i * 7919L);
	
	        float spreadX = 4f / 16f + rand.nextFloat() * 8f / 16f;
	        float spreadZ = 4f / 16f + rand.nextFloat() * 8f / 16f;
	        float yOffset = rand.nextFloat() * 1.5f / 16f;
	        float yRotation = rand.nextFloat() * 360f;
	
	        ms.translate(spreadX, baseItemY + yOffset, spreadZ);
	        ms.mulPose(Axis.YP.rotationDegrees(yRotation));
	        ms.scale(0.3f, 0.3f, 0.3f);
	        ms.mulPose(Axis.XP.rotationDegrees(65));
	
	        Minecraft.getInstance().getItemRenderer()
	            .renderStatic(stack, ItemDisplayContext.FIXED,
	                light, overlay, ms, buffer, be.getLevel(), 0);
	
	        ms.popPose();
	    }
	}
}