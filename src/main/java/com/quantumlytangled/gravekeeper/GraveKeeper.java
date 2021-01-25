package com.quantumlytangled.gravekeeper;

import javax.annotation.Nonnull;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

import com.quantumlytangled.gravekeeper.core.DeathHandler;
import com.quantumlytangled.gravekeeper.util.LoggerPrintStream;

@Mod(GraveKeeper.MODID)
public class GraveKeeper {
	public static final String MODID = "gravekeeper";
	public static final String NAME = "GraveKeeper";
	public static final String VERSION = "@VERSION@";
	
	public static GraveKeeper INSTANCE;
	
	public static final Logger logger = LogManager.getLogger(MODID);
	public static final LoggerPrintStream printStreamError = new LoggerPrintStream(Level.ERROR);
	public static final LoggerPrintStream printStreamWarn = new LoggerPrintStream(Level.WARN);
	public static final LoggerPrintStream printStreamInfo = new LoggerPrintStream(Level.INFO);
	
	public GraveKeeper() {
		if (INSTANCE != null) {
			throw new IllegalStateException();
		}
		INSTANCE = this;
		
		MinecraftForge.EVENT_BUS.register(this);
		MinecraftForge.EVENT_BUS.register(new DeathHandler());
	}
	
	@SubscribeEvent
	private void commonSetup(@Nonnull final FMLCommonSetupEvent event) {
	}
	
	@Nonnull
	public static GraveKeeper instance() {
		if (INSTANCE == null) {
			throw new IllegalStateException();
		}
		return INSTANCE;
	}
	
}
