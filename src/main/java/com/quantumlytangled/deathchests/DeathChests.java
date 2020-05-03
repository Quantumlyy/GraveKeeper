package com.quantumlytangled.deathchests;

import com.quantumlytangled.deathchests.core.Registration;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

import javax.annotation.Nonnull;

@Mod(modid = DeathChests.MODID, name = DeathChests.NAME, version = DeathChests.VERSION)
public class DeathChests
{
    public static final String MODID = "deathchests";
    public static final String NAME = "Death Chests";
    public static final String VERSION = "0.1";

    @Nonnull
    private static final DeathChests INSTANCE = new DeathChests();

    private final Registration registration;

    private DeathChests() {
        registration = new Registration();
        MinecraftForge.EVENT_BUS.register(this.registration);
    }

    @Nonnull
    @Mod.InstanceFactory
    public static DeathChests instance() {
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
