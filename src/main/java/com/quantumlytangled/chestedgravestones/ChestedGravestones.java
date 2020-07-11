package com.quantumlytangled.chestedgravestones;

import com.quantumlytangled.chestedgravestones.core.Registration;
import javax.annotation.Nonnull;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

@Mod(modid = ChestedGravestones.MODID, name = ChestedGravestones.NAME, version = ChestedGravestones.VERSION)
public class ChestedGravestones {

  public static final String MODID = "chestedgravestones";
  public static final String NAME = "ChestedGravestones";
  public static final String VERSION = "@VERSION@";

  @Nonnull
  private static final ChestedGravestones INSTANCE = new ChestedGravestones();

  private final Registration registration;

  private ChestedGravestones() {
    registration = new Registration();
    MinecraftForge.EVENT_BUS.register(this.registration);
  }

  @Nonnull
  @Mod.InstanceFactory
  public static ChestedGravestones instance() {
    return INSTANCE;
  }

  @EventHandler
  private void preInit(final FMLPreInitializationEvent event) {
    this.registration.preInitialize(event);
  }

  @EventHandler
  private void init(final FMLInitializationEvent event) {
    this.registration.initialize(event);
  }
}
