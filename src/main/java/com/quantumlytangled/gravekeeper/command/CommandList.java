package com.quantumlytangled.gravekeeper.command;

import com.quantumlytangled.gravekeeper.GraveKeeper;
import com.quantumlytangled.gravekeeper.core.CreationDate;
import com.quantumlytangled.gravekeeper.core.GraveData;
import com.quantumlytangled.gravekeeper.util.NBTFile;
import java.io.File;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import javax.annotation.Nonnull;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;

public class CommandList extends CommandBase {
  
  public ITextComponent getPrefix() {
    return new TextComponentString("/" + getName()).setStyle(new Style().setColor(TextFormatting.GOLD))
        .appendSibling(new TextComponentString(" "));
  }
  
  @Nonnull
  @Override
  public String getName() {
    return "gklist";
  }
  
  @Override
  public int getRequiredPermissionLevel() {
    return 2;
  }
  
  @Nonnull
  @Override
  public String getUsage(@Nonnull final ICommandSender commandSender) {
    return "/gklist <playerName>: list all known graves from an online player\n/gklist <playerUUID>: list all known graves from a player";
  }
  
  @Override
  public void execute(@Nonnull final MinecraftServer server, @Nonnull final ICommandSender commandSender, @Nonnull final String[] args) {
    // set defaults
    final String argument;
    
    // parse arguments
    EntityPlayerMP entityPlayer = commandSender instanceof EntityPlayerMP ? (EntityPlayerMP) commandSender : null;
    final UUID uuidPlayer;
    if (args.length != 1) {
      commandSender.sendMessage(new TextComponentString(getUsage(commandSender)));
      return;
      
    } else {
      if (args[0].equalsIgnoreCase("help") || args[0].equalsIgnoreCase("?")) {
        commandSender.sendMessage(new TextComponentString(getUsage(commandSender)));
        return;
      }

      argument = args[0];
      if (argument.length() == 36) {// 36 chars of UUID
        if (entityPlayer == null) {
          commandSender.sendMessage(getPrefix().appendSibling(
              new TextComponentString("Unable to list graves by UUID from console, sorry.").setStyle(new Style().setColor(TextFormatting.RED)) ));
          return;
        }
        try {
          uuidPlayer = UUID.fromString(argument);
        } catch (final IllegalArgumentException exception) {
          commandSender.sendMessage(getPrefix().appendSibling(
              new TextComponentString(String.format("Invalid player UUID %s",
                  argument )).setStyle(new Style().setColor(TextFormatting.RED)) ));
          return;
        }
      } else {
        boolean isMissing = true;
        final List<EntityPlayerMP> onlinePlayers = server.getPlayerList().getPlayers();
        for (final EntityPlayerMP onlinePlayer : onlinePlayers) {
          if ( onlinePlayer.getName().equalsIgnoreCase(argument)
            || onlinePlayer.getDisplayNameString().equalsIgnoreCase(argument) ) {
            entityPlayer = onlinePlayer;
            isMissing = false;
            break;
          }
        }
        if (isMissing) {
          commandSender.sendMessage(getPrefix().appendSibling(
              new TextComponentString(String.format("Unable to find player with name %s: player is offline or name is incorrect. Try using their UUID instead.",
                  argument )).setStyle(new Style().setColor(TextFormatting.RED)) ));
          return;
        }
        uuidPlayer = entityPlayer.getUniqueID();
      }
      
    }
    // assert uuidPlayer != null;
    final String identifier = uuidPlayer.toString();
    
    // open related folder
    final String stringDirectory = String.format("%s/data/%s/%s",
        entityPlayer.world.getSaveHandler().getWorldDirectory().getPath(),
        GraveKeeper.MODID,
        identifier.substring(0, 2) );
    final File fileDirectory = new File(stringDirectory);
    if (!fileDirectory.exists()) {
      commandSender.sendMessage(getPrefix().appendSibling(
          new TextComponentString(String.format("Unable to find archive directory for player %s",
              argument )).setStyle(new Style().setColor(TextFormatting.RED)) ));
      return;
    }
    
    final File[] rawFiles = fileDirectory.listFiles((dir, name) -> name.startsWith(identifier) && name.endsWith(".dat"));
    if (rawFiles == null || rawFiles.length == 0) {
      commandSender.sendMessage(getPrefix().appendSibling(
          new TextComponentString(String.format("Unable to find any backup for player %s",
              argument )).setStyle(new Style().setColor(TextFormatting.RED)) ));
      return;
    }
    final ITextComponent textSender = new TextComponentTranslation("gravekeeper.command.list_header", rawFiles.length, argument);
    textSender.getStyle().setColor(TextFormatting.GREEN);
    commandSender.sendMessage(textSender);
    
    // check permissions
    final List<ICommand> listCommands = server.getCommandManager().getPossibleCommands(commandSender);
    boolean canRestore = false;
    boolean canTeleport = false;
    boolean canTeleportPos = false;
    for (final ICommand command : listCommands) {
      canRestore |= command.getName().equals("gkrestore");
      canTeleport |= command.getName().equals("tp");
      canTeleportPos |= command.getName().equals("tppos");
    }
    
    // extract date from file names, then sort them
    final File[] sortedFiles = Arrays.stream(rawFiles)
        .sorted(Comparator.reverseOrder())
        .limit(10)
        .toArray(File[]::new);
    for (final File file : sortedFiles) {
      // extract file name
      final String name = file.getName().substring(0, file.getName().length() - 4);
      
      // extract pretty timestamp
      final String timestampMilliseconds = name.substring(name.lastIndexOf("_") - 10);
      final String timestampSeconds = name.substring(name.lastIndexOf("_") - 10, name.length() - 4);
      final CreationDate creationDate = new CreationDate(timestampMilliseconds);
      final String elapsedTime = creationDate.getElapsedTime();
      final String presentedTime = elapsedTime.length() > timestampSeconds.length() ? timestampSeconds : elapsedTime + " ago";
      
      final boolean isExpired = CreationDate.getRemainingSeconds(creationDate.seconds) <= 0;
      
      // read the file
      final NBTTagCompound tagData = NBTFile.read(file);
      GraveData graveData;
      try {
        graveData = tagData == null ? null : new GraveData(tagData);
      } catch (final Exception exception) {
        graveData = null;
        exception.printStackTrace();
      }
      
      // format timestamp, proposing to restore data
      final ITextComponent textTime = new TextComponentString(presentedTime);
      textTime.getStyle()
          .setColor(isExpired ? TextFormatting.YELLOW : TextFormatting.GREEN);
      if (canRestore) {
        textTime.getStyle()
            .setClickEvent(new ClickEvent(
                ClickEvent.Action.SUGGEST_COMMAND, String.format("/gkrestore %s", name) ))
            .setHoverEvent(new HoverEvent(
                HoverEvent.Action.SHOW_TEXT, new TextComponentString(String.format("%s\nClick to suggest restore command", timestampSeconds)) ));
      } else {
        textTime.getStyle()
            .setHoverEvent(new HoverEvent(
                HoverEvent.Action.SHOW_TEXT, new TextComponentString(timestampSeconds) ));
      }
      
      // format position, proposing to teleport if it's the same dimension
      final ITextComponent textPosition;
      if (graveData == null) {
        textPosition = new TextComponentString(" -invalid backup-");
        textPosition.getStyle()
            .setHoverEvent(new HoverEvent(
                HoverEvent.Action.SHOW_TEXT, new TextComponentString("Check console logs for further details") ))
            .setColor(TextFormatting.RED);
        
      } else if ( graveData.worldPositionPlayer == null
               || graveData.worldPositionGrave == null ) {
        textPosition = new TextComponentString(" -unknown position-");
        textPosition.getStyle()
            .setHoverEvent(new HoverEvent(
                HoverEvent.Action.SHOW_TEXT, new TextComponentString("Backup uses legacy format\nCheck console or client chat logs at time of death for coordinates") ))
            .setColor(TextFormatting.RED);
        
      } else {
        textPosition = new TextComponentString(" " + graveData.worldPositionPlayer.format());
        if (graveData.worldPositionGrave.isSameWorld(entityPlayer.world)) {
          final BlockPos blockPos = graveData.worldPositionGrave.blockPos;
          if (canTeleportPos) {
            textPosition.getStyle()
                .setClickEvent(new ClickEvent(
                    ClickEvent.Action.SUGGEST_COMMAND, String.format("/tppos %d %d %d", blockPos.getX(), blockPos.getY(), blockPos.getZ()) ))
                .setHoverEvent(new HoverEvent(
                    HoverEvent.Action.SHOW_TEXT, new TextComponentString("Click to suggest tp command") ))
                .setColor(TextFormatting.AQUA);
            
          } else if (canTeleport) {
            textPosition.getStyle()
                .setClickEvent(new ClickEvent(
                    ClickEvent.Action.SUGGEST_COMMAND, String.format("/tp %d %d %d", blockPos.getX(), blockPos.getY(), blockPos.getZ()) ))
                .setHoverEvent(new HoverEvent(
                    HoverEvent.Action.SHOW_TEXT, new TextComponentString("Click to suggest tp command") ))
                .setColor(TextFormatting.AQUA);
            
          } else {
            textPosition.getStyle()
              .setColor(TextFormatting.GREEN);
          }
          
        } else {
          textPosition.getStyle()
              .setHoverEvent(new HoverEvent(
                  HoverEvent.Action.SHOW_TEXT, new TextComponentString("Enter that dimension before you can teleport") ))
              .setColor(TextFormatting.YELLOW);
        }
      }
      
      commandSender.sendMessage(textTime.appendSibling(textPosition));
    }
  }
  
}