package com.quantumlytangled.gravekeeper;

import javax.annotation.Nonnull;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.ResourceLocation;
import com.quantumlytangled.gravekeeper.block.BlockDeathChest;
import com.quantumlytangled.gravekeeper.block.TileDeathChest;
import com.quantumlytangled.gravekeeper.command.CommandList;
import com.quantumlytangled.gravekeeper.command.CommandRestore;
import com.quantumlytangled.gravekeeper.core.DeathHandler;
import com.quantumlytangled.gravekeeper.util.LoggerPrintStream;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;

@Mod(modid = GraveKeeper.MODID,
    name = GraveKeeper.NAME,
    version = GraveKeeper.VERSION)
public class GraveKeeper {
  
  public static final String MODID = "gravekeeper";
  public static final String NAME = "GraveKeeper";
  public static final String VERSION = "@VERSION@";
  
  @Instance(GraveKeeper.MODID)
  public static GraveKeeper INSTANCE;
  
  public static Logger logger;
  public static LoggerPrintStream printStreamError;
  public static LoggerPrintStream printStreamWarn;
  public static LoggerPrintStream printStreamInfo;
  public static Block blockDeathChest;
  public static Item itemDeathCertificate;
  
  public GraveKeeper() {
    MinecraftForge.EVENT_BUS.register(this);
    MinecraftForge.EVENT_BUS.register(new DeathHandler());
  }
  
  @EventHandler
  private void preInit(@Nonnull final FMLPreInitializationEvent event) {
    logger = event.getModLog();
    printStreamError = new LoggerPrintStream(Level.ERROR);
    printStreamWarn = new LoggerPrintStream(Level.WARN);
    printStreamInfo = new LoggerPrintStream(Level.INFO);

    GraveKeeperConfig.onFMLpreInitialization(event.getModConfigurationDirectory());

    blockDeathChest = new BlockDeathChest();

    itemDeathCertificate = new Item()
        .setRegistryName(GraveKeeper.MODID + ":death_certificate")
        .setTranslationKey("gravekeeper.death_certificate")
        .setCreativeTab(CreativeTabs.TOOLS);
  }
  
  @EventHandler
  private void preInit(@Nonnull final FMLPostInitializationEvent event) {
    GraveKeeperConfig.onFMLpostInitialization();
  }
  
  @SubscribeEvent
  public void registerBlocks(@Nonnull final RegistryEvent.Register<Block> event) {
    event.getRegistry().register(blockDeathChest);
    GameRegistry.registerTileEntity(TileDeathChest.class,
        new ResourceLocation(GraveKeeper.MODID, "death_chest"));
  }
  
  @SubscribeEvent
  public void onRegisterItems(@Nonnull final RegistryEvent.Register<Item> event) {
    final ItemBlock itemBlock = new ItemBlock(blockDeathChest);
    ResourceLocation resourceLocation = blockDeathChest.getRegistryName();
    assert resourceLocation != null;
    itemBlock.setRegistryName(blockDeathChest.getRegistryName()).setCreativeTab(CreativeTabs.TOOLS);
    event.getRegistry().register(itemBlock);
    if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT) {
      ModelLoader.setCustomModelResourceLocation(itemBlock, 0, new ModelResourceLocation(resourceLocation, "inventory"));
    }
    
    resourceLocation = itemDeathCertificate.getRegistryName();
    assert resourceLocation != null;
    event.getRegistry().register(itemDeathCertificate);
    if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT) {
      ModelLoader.setCustomModelResourceLocation(itemDeathCertificate, 0, new ModelResourceLocation(resourceLocation, "inventory"));
    }
  }
  
  @EventHandler
  public void onFMLServerStarting(@Nonnull final FMLServerStartingEvent event) {
    event.registerServerCommand(new CommandList());
    event.registerServerCommand(new CommandRestore());
  }
}