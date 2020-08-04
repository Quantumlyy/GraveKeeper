package com.quantumlytangled.chestedgravestones.core;

import com.quantumlytangled.chestedgravestones.ChestedGravestones;
import com.quantumlytangled.chestedgravestones.block.BlockDeathChest;
import com.quantumlytangled.chestedgravestones.block.TileDeathChest;
import java.io.File;
import java.util.List;
import java.util.UUID;
import javax.annotation.Nonnull;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import com.quantumlytangled.chestedgravestones.util.InventoryHandler;
import com.quantumlytangled.chestedgravestones.util.LoggerPrintStream;
import com.quantumlytangled.chestedgravestones.util.NBTFile;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;

public final class Registration {

  public static Logger logger;
  public static LoggerPrintStream printStreamError;
  public static LoggerPrintStream printStreamWarn;
  public static LoggerPrintStream printStreamInfo;

  public static Block blockDeathChest;
  public static Item itemDeathCertificate;

  public void preInitialize(@Nonnull final FMLPreInitializationEvent event) {
    logger = event.getModLog();
    printStreamError = new LoggerPrintStream(Level.ERROR);
    printStreamWarn = new LoggerPrintStream(Level.WARN);
    printStreamInfo = new LoggerPrintStream(Level.INFO);

    ChestedGravestonesConfig.onFMLpreInitialization(event.getModConfigurationDirectory());

    blockDeathChest = new BlockDeathChest();
    
    itemDeathCertificate = new Item()
        .setRegistryName(ChestedGravestones.MODID + ":death_certificate")
        .setTranslationKey("chestedgravestones.death_certificate")
        .setCreativeTab(CreativeTabs.TOOLS);
  }
  
  @SubscribeEvent
  public void registerBlocks(@Nonnull final RegistryEvent.Register<Block> event) {
    event.getRegistry().register(blockDeathChest);
    GameRegistry.registerTileEntity(TileDeathChest.class,
        new ResourceLocation(ChestedGravestones.MODID, "death_chest"));
  }
  
  @SubscribeEvent
  public void onRegisterItems(@Nonnull final RegistryEvent.Register<Item> event) {
    final ItemBlock itemBlock = new ItemBlock(blockDeathChest);
    ResourceLocation resourceLocation = blockDeathChest.getRegistryName();
    assert resourceLocation != null;
    itemBlock.setRegistryName(blockDeathChest.getRegistryName()).setCreativeTab(CreativeTabs.TOOLS);
    event.getRegistry().register(itemBlock);
    ModelLoader.setCustomModelResourceLocation(itemBlock, 0, new ModelResourceLocation(resourceLocation, "inventory"));
    
    resourceLocation = itemDeathCertificate.getRegistryName();
    assert resourceLocation != null;
    event.getRegistry().register(itemDeathCertificate);
    ModelLoader.setCustomModelResourceLocation(itemDeathCertificate, 0, new ModelResourceLocation(resourceLocation, "inventory"));
  }

  @SubscribeEvent(priority = EventPriority.HIGHEST)
  public void onPlayerDeath(@Nonnull final LivingDeathEvent event) {
    if (event.isCanceled()) {
      logger.debug(String.format("Event is cancelled, ignoring death event of %s",
          event.getEntity() ));
      return;
    }
    
    final EntityLivingBase entityLiving = event.getEntityLiving();
    if ( !(entityLiving instanceof EntityPlayer)
      || !entityLiving.isServerWorld() ) {
      return;
    }
    final EntityPlayerMP player = (EntityPlayerMP) entityLiving;
    
    if ( !ChestedGravestonesConfig.IGNORE_KEEP_INVENTORY
      && player.world.getGameRules().getBoolean("keepInventory") ) {
      logger.debug(String.format("Keep inventory is enabled, ignoring death of player %s",
          player ));
      return;
    }
    
    // compute grave content
    final CreationDate creationDate = new CreationDate();
    final String stringTimestamp = creationDate.string;
    final String playerName = player.getDisplayNameString();
    final UUID playerUUID = player.getUniqueID();
    final String identifier = playerUUID + "_" + playerName + "_" + stringTimestamp;
    final List<InventorySlot> inventorySlots = InventoryHandler.collectOnDeath(player);
    if (inventorySlots.isEmpty()) {
      logger.warn(String.format("No item to save, ignoring death of player %s",
          player ));
      return;
    }
    
    // archive grave content
    final String stringDirectory = String.format("%s/data/%s/%s",
        player.world.getSaveHandler().getWorldDirectory().getPath(),
        ChestedGravestones.MODID,
        identifier.substring(0, 2) );
    final File fileDirectory = new File(stringDirectory);
    try {
      FileUtils.forceMkdir(fileDirectory);
      final NBTTagCompound nbtInventorySlots = InventoryHandler.writeToNBT(inventorySlots);
      final String stringFilePath = String.format("%s/%s.dat", stringDirectory, identifier);
      NBTFile.write(stringFilePath, nbtInventorySlots);
      logger.info(String.format("Archived DeathChest content for %s, restore it with /cgrestore %s",
          playerName,
          identifier ));
    } catch (final Exception exception) {
      exception.printStackTrace(printStreamWarn);
      logger.warn(String.format("Failed to create inventory backup for player %s",
          player ));
    }

    // find a position and place the grave
    double pX = player.posX;
    double pY = player.posY;
    double pZ = player.posZ;
    final WorldPosition worldPositionChest = GravePosition.get(player, new BlockPos(MathHelper.floor(pX), MathHelper.floor(pY), MathHelper.floor(pZ)));
    
    worldPositionChest.world.setBlockState(worldPositionChest.blockPos, blockDeathChest.getDefaultState());
    
    final TileEntity tileEntity = worldPositionChest.world.getTileEntity(worldPositionChest.blockPos);
    if (!(tileEntity instanceof TileDeathChest)) {
      logger.error(String.format("Missing tile entity %s, unable to save player %s inventory in world %s at %s",
          tileEntity, playerName, worldPositionChest.world, worldPositionChest.blockPos ));
      return;
    }
    final TileDeathChest tileDeathChest = (TileDeathChest) tileEntity;
    
    tileDeathChest.setData(player, identifier, creationDate.seconds, inventorySlots);

    // log to console
    logger.info(String.format("Generated DeathChest for %s (%s) in DIM%d at (%d %d %d).",
        playerName, playerUUID,
        worldPositionChest.world.provider.getDimension(),
        worldPositionChest.blockPos.getX(), worldPositionChest.blockPos.getY(), worldPositionChest.blockPos.getZ() ));

    // inform player
    final ITextComponent textLocation = new TextComponentString(worldPositionChest.format());
    textLocation.getStyle().setColor(TextFormatting.AQUA).setBold(true);
    final ITextComponent textMessage = new TextComponentTranslation("chestedgravestones.chat.grave_placed",
        textLocation );
    textMessage.getStyle().setColor(TextFormatting.GOLD);
    player.sendMessage(textMessage);
  }
  
}