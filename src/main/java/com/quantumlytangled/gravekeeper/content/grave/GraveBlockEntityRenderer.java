package com.quantumlytangled.gravekeeper.content.grave;

import javax.annotation.ParametersAreNonnullByDefault;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.LightLayer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.neoforged.neoforge.client.model.data.ModelData;

@ParametersAreNonnullByDefault
public class GraveBlockEntityRenderer implements BlockEntityRenderer<GraveBlockEntity> {
	
	private static final ResourceLocation GRAVE_CROSS_MODEL =
			new ResourceLocation("gravekeeper", "block/grave_cross");
	
	public GraveBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
	}
	
	@Override
	public void render(GraveBlockEntity entity, float partialTick, PoseStack poseStack,
	                   MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
		
		if (entity.getLevel() != null) {
			packedLight = LightTexture.pack(
					entity.getLevel().getBrightness(LightLayer.BLOCK, entity.getBlockPos().above()),
					entity.getLevel().getBrightness(LightLayer.SKY, entity.getBlockPos().above()));
		}
		
		poseStack.pushPose();
		
		Minecraft.getInstance().getBlockRenderer().getModelRenderer().renderModel(
				poseStack.last(),
				bufferSource.getBuffer(RenderType.cutout()),
				null,
				Minecraft.getInstance().getModelManager().getModel(GRAVE_CROSS_MODEL),
				1f, 1f, 1f,
				packedLight,
				packedOverlay,
				ModelData.EMPTY,
				RenderType.cutout());
		
		poseStack.popPose();
	}
	
	@Override
	public boolean shouldRenderOffScreen(GraveBlockEntity entity) {
		return true;
	}
}