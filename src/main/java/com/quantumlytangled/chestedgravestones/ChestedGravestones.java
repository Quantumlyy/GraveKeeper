package com.quantumlytangled.chestedgravestones;

import javax.annotation.Nonnull;
import com.quantumlytangled.chestedgravestones.core.Registration;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

@Mod(modid = ChestedGravestones.MODID,
    name = ChestedGravestones.NAME,
    version = ChestedGravestones.VERSION)
public class ChestedGravestones {

  public static final String MODID = "chestedgravestones";
  public static final String NAME = "ChestedGravestones";
  public static final String VERSION = "@VERSION@";

  @Instance(ChestedGravestones.MODID)
  public static ChestedGravestones INSTANCE;

  private final Registration registration;

  public ChestedGravestones() {
    registration = new Registration();
    MinecraftForge.EVENT_BUS.register(registration);
  }

  @EventHandler
  private void preInit(@Nonnull final FMLPreInitializationEvent event) {
    registration.preInitialize(event);
  }
}