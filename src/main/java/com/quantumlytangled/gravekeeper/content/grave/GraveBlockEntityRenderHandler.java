package com.quantumlytangled.gravekeeper.content.grave;

import net.minecraft.resources.ResourceLocation;

import com.quantumlytangled.gravekeeper.GraveKeeper;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.ModelEvent;

@EventBusSubscriber(modid = GraveKeeper.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class GraveBlockEntityRenderHandler {
	
	@SubscribeEvent
	static void onRegisterAdditionalModels(ModelEvent.RegisterAdditional event) {
		event.register(new ResourceLocation("gravekeeper", "block/grave_cross"));
	}
	
	@SubscribeEvent
	static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
		event.registerBlockEntityRenderer(
				GraveKeeper.GRAVE_BLOCK_ENTITY.get(),
				GraveBlockEntityRenderer::new);
	}
}
