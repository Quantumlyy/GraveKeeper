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
		
		// render cross model
		poseStack.pushPose();
		Minecraft.getInstance().getBlockRenderer().getModelRenderer().renderModel(
				poseStack.last(),
				bufferSource.getBuffer(RenderType.cutout()),
				null,
				Minecraft.getInstance().getModelManager().getModel(GRAVE_CROSS_MODEL),
				1f, 1f, 1f,
				packedLight, packedOverlay,
				ModelData.EMPTY, RenderType.cutout());
		poseStack.popPose();

//		final String ownerName = entity.getOwnerName();
//		if (ownerName != null && !ownerName.isEmpty()) {
//			poseStack.pushPose();
//
//			poseStack.translate(0.15, 1.22, 0.87);
//			poseStack.mulPose(Axis.YP.rotationDegrees(180));
//			poseStack.mulPose(Axis.ZP.rotationDegrees(22.5f)); // positive instead of negative
//			poseStack.scale(-0.008f, 0.008f, 0.008f);
//
//			Font font = Minecraft.getInstance().font;
//			MultiBufferSource.BufferSource immediate = Minecraft.getInstance().renderBuffers().bufferSource();
//
//			final int maxWidth = 120;
//			String displayName = ownerName;
//			if (font.width(displayName) > maxWidth) {
//				while (font.width(displayName + "...") > maxWidth && !displayName.isEmpty()) {
//					displayName = displayName.substring(0, displayName.length() - 1);
//				}
//				displayName += "...";
//			}
//
//			int textWidth = font.width(displayName);
//
//			poseStack.pushPose();
//			poseStack.translate(0, 0, 1);
//			font.drawInBatch(displayName,
//			                 -textWidth / 2f, 0,
//			                 0x333333, false,
//			                 poseStack.last().pose(),
//			                 immediate,
//			                 Font.DisplayMode.NORMAL,
//			                 0, LightTexture.FULL_BRIGHT);
//			poseStack.popPose();
//
//			font.drawInBatch(displayName,
//			                 -textWidth / 2f, 0,
//			                 0x000000, false,
//			                 poseStack.last().pose(),
//			                 immediate,
//			                 Font.DisplayMode.NORMAL,
//			                 0, LightTexture.FULL_BRIGHT);
//
//			immediate.endBatch();
//
//			poseStack.popPose();
//		}
	}
	
	@Override
	public boolean shouldRenderOffScreen(GraveBlockEntity entity) {
		return true;
	}
}