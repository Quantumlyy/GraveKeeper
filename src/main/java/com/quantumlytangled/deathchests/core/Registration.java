package com.quantumlytangled.deathchests.core;

import com.quantumlytangled.deathchests.block.BlockDeathChest;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.apache.logging.log4j.Logger;

public final class Registration {

    private static Logger logger;

    public void preInitialize(FMLPreInitializationEvent event)
    {
        logger = event.getModLog();
    }

    public void initialize(FMLInitializationEvent event)
    {
        // some example code
        logger.info("DIRT BLOCK >> {}", Blocks.DIRT.getRegistryName());
    }

    @SubscribeEvent
    public void registerBlocks(final RegistryEvent.Register<Block> event) {
        event.getRegistry().register(new BlockDeathChest());
    }
}
