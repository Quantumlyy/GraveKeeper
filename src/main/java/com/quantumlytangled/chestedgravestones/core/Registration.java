package com.quantumlytangled.chestedgravestones.core;

import com.quantumlytangled.chestedgravestones.ChestedGravestones;
import com.quantumlytangled.chestedgravestones.block.BlockDeathChest;
import com.quantumlytangled.chestedgravestones.block.TileDeathChest;
import java.util.List;
import java.util.UUID;
import javax.annotation.Nonnull;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import com.quantumlytangled.chestedgravestones.util.InventoryHandler;
import org.apache.logging.log4j.Logger;

public final class Registration {

  public static Logger logger;

  public static Block blockDeathChest;

  public void preInitialize(@Nonnull final FMLPreInitializationEvent event) {
    logger = event.getModLog();

    ChestedGravestonesConfig.onFMLpreInitialization(event.getModConfigurationDirectory());

    blockDeathChest = new BlockDeathChest();
  }

  public void initialize(@Nonnull final FMLInitializationEvent event) {
    // no operation
  }

  @SubscribeEvent
  public void registerBlocks(@Nonnull final RegistryEvent.Register<Block> event) {
    event.getRegistry().register(blockDeathChest);
    GameRegistry.registerTileEntity(TileDeathChest.class,
        new ResourceLocation(ChestedGravestones.MODID, "death_chest"));
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

    final CreationDate creationDate = new CreationDate();
    final String stringTimestamp = creationDate.string;
    final String playerName = player.getDisplayNameString();
    final UUID playerUUID = player.getUniqueID();
    final String identifier = playerUUID + "." + playerName + "." + stringTimestamp;
    final List<InventorySlot> inventorySlots = InventoryHandler.collectOnDeath(player);
    if (inventorySlots.isEmpty()) {
      logger.warn(String.format("No item to save, ignoring death of player %s",
          player ));
      return;
    }

    final World world = player.world;
    double pX = player.posX;
    double pY = player.posY;
    double pZ = player.posZ;
    final BlockPos blockPosDeath = new BlockPos(MathHelper.floor(pX), MathHelper.floor(pY), MathHelper.floor(pZ));

    world.setBlockState(blockPosDeath, blockDeathChest.getDefaultState());

    final TileEntity tileEntity = world.getTileEntity(blockPosDeath);
    if (!(tileEntity instanceof TileDeathChest)) {
      logger.error(String.format("Missing tile entity %s, unable to save player %s inventory in world %s at %s",
          tileEntity, playerName, world, blockPosDeath ));
      return;
    }
    final TileDeathChest tileDeathChest = (TileDeathChest) tileEntity;
    
    tileDeathChest.setData(player, identifier, creationDate.seconds, inventorySlots);
    
    player.sendMessage(new TextComponentString(String.format("Chest placed at (%d %d %d)",
        blockPosDeath.getX(), blockPosDeath.getY(), blockPosDeath.getZ() )));
    logger.info(String.format("Generated DeathChest for %s(%s) at (%d %d %d)",
        playerName, playerUUID, blockPosDeath.getX(), blockPosDeath.getY(), blockPosDeath.getZ() ));
  }
}