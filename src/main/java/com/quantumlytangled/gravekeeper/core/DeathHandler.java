package com.quantumlytangled.gravekeeper.core;

import com.quantumlytangled.gravekeeper.GraveKeeper;
import com.quantumlytangled.gravekeeper.GraveKeeperConfig;
import com.quantumlytangled.gravekeeper.block.TileDeathChest;
import com.quantumlytangled.gravekeeper.util.InventorySlot;
import com.quantumlytangled.gravekeeper.util.NBTFile;
import com.quantumlytangled.gravekeeper.util.WorldPosition;
import java.io.File;
import java.util.List;
import java.util.UUID;
import javax.annotation.Nonnull;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.apache.commons.io.FileUtils;

public final class DeathHandler {

  @SubscribeEvent(priority = EventPriority.HIGHEST)
  public void onPlayerDeath(@Nonnull final LivingDeathEvent event) {
    if (event.isCanceled()) {
      GraveKeeper.logger.debug(String.format("Event is cancelled, ignoring death event of %s",
          event.getEntity()));
      return;
    }

    final EntityLivingBase entityLiving = event.getEntityLiving();
    if (!(entityLiving instanceof EntityPlayer)
        || !entityLiving.isServerWorld()) {
      return;
    }
    final EntityPlayerMP player = (EntityPlayerMP) entityLiving;

    if (!GraveKeeperConfig.IGNORE_KEEP_INVENTORY
        && player.world.getGameRules().getBoolean("keepInventory")) {
      GraveKeeper.logger
          .debug(String.format("Keep inventory is enabled, ignoring death of player %s",
              player));
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
      GraveKeeper.logger.warn(String.format("No item to save, ignoring death of player %s",
          player));
      return;
    }

    // find a grave position
    final WorldPosition worldPositionPlayer = new WorldPosition(player.world,
        new BlockPos(MathHelper.floor(player.posX), MathHelper.floor(player.posY),
            MathHelper.floor(player.posZ)));
    final WorldPosition worldPositionGrave = GravePosition
        .get(player, worldPositionPlayer.blockPos);
    final GraveData graveData = new GraveData(inventorySlots, worldPositionPlayer,
        worldPositionGrave);

    // archive grave content
    final String stringDirectory = String.format("%s/data/%s/%s",
        player.world.getSaveHandler().getWorldDirectory().getPath(),
        GraveKeeper.MODID,
        identifier.substring(0, 2));
    final File fileDirectory = new File(stringDirectory);
    try {
      FileUtils.forceMkdir(fileDirectory);
      final NBTTagCompound nbtGraveData = graveData.writeToNBT();
      final String stringFilePath = String.format("%s/%s.dat", stringDirectory, identifier);
      NBTFile.write(stringFilePath, nbtGraveData);
      GraveKeeper.logger
          .info(String.format("Archived DeathChest content for %s, restore it with /gkrestore %s",
              playerName,
              identifier));
    } catch (final Exception exception) {
      exception.printStackTrace(GraveKeeper.printStreamWarn);
      GraveKeeper.logger.warn(String.format("Failed to create inventory backup for player %s",
          player));
    }

    final boolean anyGraveContent = inventorySlots.stream()
        .anyMatch(inventorySlot -> !inventorySlot.isCharmed
            && !inventorySlot.isSoulbound);
    if (!anyGraveContent) {
      GraveKeeper.logger.warn(String.format("No item to save, ignoring death of player %s",
          player));
      return;
    }

    // place the grave
    worldPositionGrave.getWorld()
        .setBlockState(worldPositionGrave.blockPos, GraveKeeper.blockDeathChest.getDefaultState());

    final TileEntity tileEntity = worldPositionGrave.getWorld()
        .getTileEntity(worldPositionGrave.blockPos);
    if (!(tileEntity instanceof TileDeathChest)) {
      GraveKeeper.logger.error(String
          .format("Missing tile entity %s, unable to save player %s inventory in world %s at %s",
              tileEntity, playerName, worldPositionGrave.getWorld(), worldPositionGrave.blockPos));
      return;
    }
    final TileDeathChest tileDeathChest = (TileDeathChest) tileEntity;

    tileDeathChest.setData(player, identifier, creationDate.seconds, inventorySlots);

    // log to console
    GraveKeeper.logger
        .info(String.format("Generated DeathChest for %s (%s) in DIM%d at (%d %d %d).",
            playerName, playerUUID,
            worldPositionGrave.getWorld().provider.getDimension(),
            worldPositionGrave.blockPos.getX(), worldPositionGrave.blockPos.getY(),
            worldPositionGrave.blockPos.getZ()));

    // inform player
    final ITextComponent textLocation = new TextComponentString(worldPositionGrave.format());
    textLocation.getStyle().setColor(TextFormatting.AQUA).setBold(true);
    final ITextComponent textMessage = new TextComponentTranslation("gravekeeper.chat.grave_placed",
        textLocation);
    textMessage.getStyle().setColor(TextFormatting.GOLD);
    player.sendMessage(textMessage);
  }
}