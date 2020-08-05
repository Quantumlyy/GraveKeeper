package com.quantumlytangled.gravekeeper.core;

import javax.annotation.Nonnull;

import java.io.File;
import java.util.List;
import java.util.UUID;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import com.quantumlytangled.gravekeeper.GraveKeeper;
import com.quantumlytangled.gravekeeper.util.InventoryHandler;
import com.quantumlytangled.gravekeeper.util.NBTFile;

public class CommandRestore extends CommandBase {

  public ITextComponent getPrefix() {
    return new TextComponentString("/" + getName()).setStyle(new Style().setColor(TextFormatting.GOLD))
        .appendSibling(new TextComponentString(" "));
  }

  @Nonnull
  @Override
  public String getName() {
    return "gkrestore";
  }

  @Override
  public int getRequiredPermissionLevel() {
    return 2;
  }

  @Nonnull
  @Override
  public String getUsage(@Nonnull final ICommandSender commandSender) {
    return "/gkrestore <identifier>: restore an online player's inventory\n/gkrestore <playerName> <identifier>: restore an inventory to another online player";
  }

  @Override
  public void execute(@Nonnull final MinecraftServer server, @Nonnull final ICommandSender commandSender, @Nonnull final String[] args) {
    // set defaults
    final String identifier;
    EntityPlayerMP entityPlayer = null;
    
    // parse arguments
    if ( args.length == 0
      || args.length > 2 ) {
      commandSender.sendMessage(new TextComponentString(getUsage(commandSender)));
      return;
      
    } else if (args.length == 1) {
      if (args[0].equalsIgnoreCase("help") || args[0].equalsIgnoreCase("?")) {
        commandSender.sendMessage(new TextComponentString(getUsage(commandSender)));
        return;
      }
      
      identifier = args[0];
      if (identifier.length() < 61) {// 36 chars of UUID + 2 _ + x name + 23 date = 60 + x
        commandSender.sendMessage(getPrefix().appendSibling(
            new TextComponentString(String.format("Invalid identifier %s",
                identifier )).setStyle(new Style().setColor(TextFormatting.RED)) ));
        return;
      }
      final UUID uuidPlayer = UUID.fromString(identifier.substring(0, 36));
      
      final List<EntityPlayerMP> onlinePlayers = server.getPlayerList().getPlayers();
      for (final EntityPlayerMP onlinePlayer : onlinePlayers) {
        if (onlinePlayer.getPersistentID().equals(uuidPlayer)) {
          entityPlayer = onlinePlayer;
          break;
        }
      }
      if (entityPlayer == null) {
        commandSender.sendMessage(getPrefix().appendSibling(
            new TextComponentString(String.format("Unable to find player with UUID %s: player is offline or identifier is incorrect",
                uuidPlayer )).setStyle(new Style().setColor(TextFormatting.RED)) ));
        return;
      }
      
    } else {
      // assert args.length == 2;
      
      final String namePlayer = args[0];
      identifier = args[1];
      if (identifier.length() < 61) {// 36 chars of UUID + 2 _ + x name + 23 date = 60 + x
        commandSender.sendMessage(getPrefix().appendSibling(
            new TextComponentString(String.format("Invalid identifier %s",
                identifier )).setStyle(new Style().setColor(TextFormatting.RED)) ));
        return;
      }
      
      final List<EntityPlayerMP> onlinePlayers = server.getPlayerList().getPlayers();
      for (final EntityPlayerMP onlinePlayer : onlinePlayers) {
        if (onlinePlayer.getName().equalsIgnoreCase(namePlayer)) {
          entityPlayer = onlinePlayer;
          break;
        }
      }
      if (entityPlayer == null) {
        commandSender.sendMessage(getPrefix().appendSibling(
            new TextComponentString(String.format("Unable to find player with name %s: player is offline or name is incorrect",
                namePlayer )).setStyle(new Style().setColor(TextFormatting.RED)) ));
        return;
      }
    }
    // assert identifier != null;
    // assert entityPlayer != null;
    
    // reload grave content
    final String stringDirectory = String.format("%s/data/%s/%s",
        entityPlayer.world.getSaveHandler().getWorldDirectory().getPath(),
        GraveKeeper.MODID,
        identifier.substring(0, 2) );
    final File fileDirectory = new File(stringDirectory);
    if (!fileDirectory.exists()) {
      commandSender.sendMessage(getPrefix().appendSibling(
          new TextComponentString(String.format("Unable to find archive directory for identifier %s",
              identifier )).setStyle(new Style().setColor(TextFormatting.RED)) ));
      return;
    }
    final List<InventorySlot> inventorySlots;
    try {
      final String stringFilePath = String.format("%s/%s.dat", stringDirectory, identifier);
      final NBTTagCompound nbtInventorySlots = NBTFile.read(stringFilePath);
      if (nbtInventorySlots == null) {
        commandSender.sendMessage(getPrefix().appendSibling(
            new TextComponentString(String.format("Unable to read archived inventory for identifier %s",
                identifier )).setStyle(new Style().setColor(TextFormatting.RED)) ));
        return;
      }
      inventorySlots = InventoryHandler.readFromNBT(nbtInventorySlots);
    } catch (final Exception exception) {
      exception.printStackTrace(Registration.printStreamError);
      commandSender.sendMessage(getPrefix().appendSibling(
          new TextComponentString(String.format("Error trying to read archived inventory for identifier %s, check console for details",
              identifier )).setStyle(new Style().setColor(TextFormatting.RED)) ));
      return;
    }
    // assert inventorySlots != null;
    
    // restore
    final List<ItemStack> overflow = InventoryHandler.restoreOrOverflow(entityPlayer, inventorySlots, entityPlayer.isCreative());
    for (final ItemStack itemStack : overflow) {
      if (entityPlayer.inventory.addItemStackToInventory(itemStack.copy())) {
        continue;
      }
      entityPlayer.world.spawnEntity(new EntityItem(entityPlayer.world, entityPlayer.posX, entityPlayer.posY, entityPlayer.posZ, itemStack));
    }
    
    // inform player & command sender
    final ITextComponent textSender = new TextComponentTranslation("gravekeeper.command.restored_sender", entityPlayer.getDisplayName());
    textSender.getStyle().setColor(TextFormatting.GREEN);
    commandSender.sendMessage(textSender);
    Registration.logger.info(textSender.getUnformattedText());
    
    if (commandSender != entityPlayer) {
      final ITextComponent textTarget = new TextComponentTranslation("gravekeeper.command.restored_target");
      textTarget.getStyle().setColor(TextFormatting.GREEN);
      entityPlayer.sendMessage(textTarget);
    }
  }
}