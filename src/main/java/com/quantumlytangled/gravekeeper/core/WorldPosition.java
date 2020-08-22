package com.quantumlytangled.gravekeeper.core;

import javax.annotation.Nonnull;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.common.FMLCommonHandler;

public class WorldPosition {
  
  private int dimensionId;
  private String worldName;
  private World world;
  public BlockPos blockPos;
  
  WorldPosition(@Nonnull final World world, @Nonnull final BlockPos blockPos) {
    this.dimensionId = -666;
    this.worldName = null;
    this.world = world;
    this.blockPos = blockPos;
  }
  
  WorldPosition(final int dimensionId, @Nonnull final String worldName, @Nonnull final BlockPos blockPos) {
    this.dimensionId = dimensionId;
    this.worldName = worldName;
    this.blockPos = blockPos;
  }
  
  @Nonnull
  public World getWorld() {
    if (world == null) {
      world = getOrCreateWorldServer(dimensionId);
      if (world == null) {
        Registration.logger.warn(String.format("Failed to load DIM%d (%s), defaulting to the overworld",
            dimensionId, worldName ));
        world = getOrCreateWorldServer(0);
      }
    }
    return world;
  }
  
  public void setWorld(@Nonnull final World world) {
    this.dimensionId = -666;
    this.worldName = null;
    this.world = world;
  }
  
  @Nonnull
  public NBTTagCompound writeToNBT(@Nonnull final NBTTagCompound tagCompound) {
    tagCompound.setInteger("dimensionId", world.provider.getDimension());
    tagCompound.setString("worldName", format(world));
    tagCompound.setInteger("x", blockPos.getX());
    tagCompound.setInteger("y", blockPos.getY());
    tagCompound.setInteger("z", blockPos.getZ());
    return tagCompound;
  }
  
  @Nonnull
  public WorldPosition(@Nonnull final NBTTagCompound tagCompound) {
    dimensionId = tagCompound.getInteger("dimensionId");
    worldName = tagCompound.getString("worldName");
    world = null;
    blockPos = new BlockPos(tagCompound.getInteger("x"), tagCompound.getInteger("y"), tagCompound.getInteger("z"));
  }
  
  @Nonnull
  public String format() {
    return new TextComponentTranslation("gravekeeper.chat.world_xyz",
        world == null ? worldName : format(world),
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
  
  public static WorldServer getOrCreateWorldServer(final int dimensionId) {
    WorldServer worldServer = DimensionManager.getWorld(dimensionId);
    
    if (worldServer == null) {
      try {
        final MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
        worldServer = server.getWorld(dimensionId);
        if (worldServer.provider.getDimension() != dimensionId) {
          throw new RuntimeException(String.format("Inconsistent dimension id %d, expecting %d",
              worldServer.provider.getDimension(), dimensionId ));
        }
      } catch (final Exception exception) {
        Registration.logger.error(String.format("%s: Failed to initialize dimension %d",
            exception.getMessage(),
            dimensionId));
        if (GraveKeeperConfig.DEBUG_LOGS) {
          exception.printStackTrace(Registration.printStreamError);
        }
        worldServer = null;
      }
    }
    
    return worldServer;
  }
}