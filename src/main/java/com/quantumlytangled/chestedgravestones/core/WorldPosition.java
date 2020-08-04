package com.quantumlytangled.chestedgravestones.core;

import javax.annotation.Nonnull;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;

public class WorldPosition {

  public World world;
  public BlockPos blockPos;

  WorldPosition(final World world, final BlockPos blockPos) {
    this.world = world;
    this.blockPos = blockPos;
  }
  
  public String format() {
    return new TextComponentTranslation("chestedgravestones.chat.world_xyz",
        format(world),
        blockPos.getX(), blockPos.getY(), blockPos.getZ() )
        .getUnformattedText();
  }
  
  @Nonnull
  public static String format(final World world) {
    if (world == null) {
      return "~NULL~";
    }
    
    // world.getProviderName() is MultiplayerChunkCache on client, ServerChunkCache on local server, (undefined method) on dedicated server

    // world.provider.getSaveFolder() is null for the Overworld, other dimensions shall define it
    String saveFolder;
    try {
      saveFolder = world.provider.getSaveFolder();
    } catch (final Exception exception) {
      exception.printStackTrace(Registration.printStreamError);
      saveFolder = "<Exception DIM" + world.provider.getDimension() + ">";
    }
    if (saveFolder == null || saveFolder.isEmpty()) {
      final int dimension = world.provider.getDimension();
      if (dimension != 0) {
        assert false;
        return String.format("~invalid dimension %d~", dimension);
      }
      
      // world.getWorldInfo().getWorldName() is MpServer on client side, or the server.properties' world name on server side
      final String worldName = world.getWorldInfo().getWorldName();
      if (worldName.equals("MpServer")) {
        return "overworld";
      }
      return worldName;
    }
    return saveFolder;
  }

}