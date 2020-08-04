package com.quantumlytangled.gravekeeper;

import javax.annotation.Nonnull;
import com.quantumlytangled.gravekeeper.core.CommandRestore;
import com.quantumlytangled.gravekeeper.core.Registration;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;

@Mod(modid = GraveKeeper.MODID,
    name = GraveKeeper.NAME,
    version = GraveKeeper.VERSION)
public class GraveKeeper {

  public static final String MODID = "gravekeeper";
  public static final String NAME = "GraveKeeper";
  public static final String VERSION = "@VERSION@";

  @Instance(GraveKeeper.MODID)
  public static GraveKeeper INSTANCE;

  private final Registration registration;

  public GraveKeeper() {
    registration = new Registration();
    MinecraftForge.EVENT_BUS.register(registration);
  }

  @EventHandler
  private void preInit(@Nonnull final FMLPreInitializationEvent event) {
    registration.preInitialize(event);
  }

  @EventHandler
  public void onFMLServerStarting(@Nonnull final FMLServerStartingEvent event) {
    event.registerServerCommand(new CommandRestore());
  }
}