package com.quantumlytangled.gravekeeper;

import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

import com.mojang.logging.LogUtils;
import com.quantumlytangled.gravekeeper.content.grave.GraveBlock;
import com.quantumlytangled.gravekeeper.content.grave.GraveBlockEntity;
import com.quantumlytangled.gravekeeper.foundation.events.DeathHandler;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.slf4j.Logger;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(GraveKeeper.MODID)
public class GraveKeeper {
	
	// Define mod id in a common place for everything to reference
	public static final String MODID = "gravekeeper";
	// Directly reference a slf4j logger
	public static final Logger LOGGER = LogUtils.getLogger();
	// Create a Deferred Register to hold Blocks which will all be registered under the "gravekeeper" namespace
	public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MODID);
	public static final DeferredBlock<Block> GRAVE_BLOCK =
			BLOCKS.register("grave", () -> new GraveBlock(
					                BlockBehaviour.Properties.of()
					                                         .mapColor(MapColor.STONE)
					                                         .noOcclusion()
					                                         .isSuffocating((state, level, pos) -> false)
					                                         .isViewBlocking((state, level, pos) -> false))
			               );
	public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES =
			DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, MODID);
	
	// The constructor for the mod class is the first code that is run when your mod is loaded.
	// FML will recognize some parameter types like IEventBus or ModContainer and pass them in automatically.
	public GraveKeeper(IEventBus modEventBus, ModContainer modContainer) {
		// Register the commonSetup method for modloading
		modEventBus.addListener(this::commonSetup);
		
		// Register the Deferred Register to the mod event bus so blocks get registered
		BLOCKS.register(modEventBus);
		
		BLOCK_ENTITY_TYPES.register(modEventBus);
		
		// Register ourselves for server and other game events we are interested in.
		// Note that this is necessary if and only if we want *this* class (GraveKeeper) to respond directly to events.
		// Do not add this line if there are no @SubscribeEvent-annotated functions in this class, like onServerStarting() below.
		NeoForge.EVENT_BUS.register(this);
		
		// Register our mod's ModConfigSpec so that FML can create and load the config file for us
		modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
		
		NeoForge.EVENT_BUS.register(new DeathHandler());
	}
	
	private void commonSetup(FMLCommonSetupEvent event) {
		// Some common setup code
		LOGGER.info("HELLO FROM COMMON SETUP");
	}
	
	// You can use SubscribeEvent and let the Event Bus discover methods to call
	@SubscribeEvent
	public void onServerStarting(ServerStartingEvent event) {
		// Do something when the server starts
		LOGGER.info("HELLO from server starting");
	}	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<GraveBlockEntity>> GRAVE_BLOCK_ENTITY =
			BLOCK_ENTITY_TYPES.register("grave", () ->
					                                     BlockEntityType.Builder.of(GraveBlockEntity::new, GRAVE_BLOCK.get()).build(null));
	
	// You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
	@EventBusSubscriber(modid = GraveKeeper.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
	static class ClientModEvents {
		
		@SubscribeEvent
		static void onClientSetup(FMLClientSetupEvent event) {
			// Some client setup code
			LOGGER.info("HELLO FROM CLIENT SETUP");
			LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().getUser().getName());
		}
	}
	

	
}
