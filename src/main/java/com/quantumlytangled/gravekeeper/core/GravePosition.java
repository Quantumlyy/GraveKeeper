package com.quantumlytangled.gravekeeper.core;

import com.quantumlytangled.gravekeeper.util.WorldPosition;

import javax.annotation.Nonnull;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import com.quantumlytangled.gravekeeper.GraveKeeper;
import com.quantumlytangled.gravekeeper.GraveKeeperConfig;

public class GravePosition {
  
  // update proposed chest position and return target world
  @Nonnull
  public static WorldPosition get(@Nonnull final EntityPlayer entityPlayer, @Nonnull final BlockPos blockPosInitial) {
    WorldPosition worldPositionResult = new WorldPosition(entityPlayer.world, blockPosInitial);
    
    // force bed or spawn location as a starting point
    if (worldPositionResult.blockPos.getY() <= GraveKeeperConfig.USE_BED_OR_SPAWN_LOCATION_BELOW_Y) {
      updateWithBedOrSpawn(entityPlayer, worldPositionResult);
    }
    
    // adjust the starting altitude within the world bounds
    if (worldPositionResult.blockPos.getY() >= worldPositionResult.getWorld().getHeight()) {
      worldPositionResult.blockPos = new BlockPos(
          worldPositionResult.blockPos.getX(),
          worldPositionResult.getWorld().getHeight() - 1,
          worldPositionResult.blockPos.getZ() );
    } else if (worldPositionResult.blockPos.getY() < GraveKeeperConfig.SEARCH_MIN_ALTITUDE) {
      worldPositionResult.blockPos = new BlockPos(
		      worldPositionResult.blockPos.getX(),
		      GraveKeeperConfig.SEARCH_MIN_ALTITUDE,
		      worldPositionResult.blockPos.getZ() );
    }
    
    // stop here if we start on a good spot
    if (isFreeSpot(worldPositionResult.getWorld(), worldPositionResult.blockPos, true, false)) {
      return worldPositionResult;
    }
    
    // search for a free spot around the starting point, keeping the closest visible & hidden ones
    if (updateWithNearbyFreeSpot(worldPositionResult)) {
      return worldPositionResult;
    }
    
    // (at this point, we're either flying or in an area filled with block (for example, a cave collapsing with sand or a lava lake)
    
    // look for the ground if we are in the air, look above us otherwise
    final boolean isFlying = isFreeSpot(worldPositionResult.getWorld(), worldPositionResult.blockPos, false, false);
    if (updateVertically(worldPositionResult, isFlying)) {
      return worldPositionResult;
    }
    
    // look in the opposite direction
    GraveKeeper.logger.info(String.format("Player has no safe spot for its grave, releasing direction: %s %s",
        entityPlayer, worldPositionResult.blockPos ));
    if (updateVertically(worldPositionResult, isFlying)) {
      return worldPositionResult;
    }
    
    // flying in the void? just forget the solid block constrains
    if (isFlying) {
      if (GraveKeeperConfig.DEBUG_LOGS) {
        GraveKeeper.logger.info(String.format("Assuming void area, defaulting to (%d %d %d)",
            worldPositionResult.blockPos.getX(), worldPositionResult.blockPos.getY(), worldPositionResult.blockPos.getZ() ));
      }
      return worldPositionResult;
    }
    
    // we did our best, admins will fix it up from there <3
    final IBlockState blockState = worldPositionResult.getWorld().getBlockState(worldPositionResult.blockPos);
    final TileEntity tileEntity = worldPositionResult.getWorld().getTileEntity(worldPositionResult.blockPos);
    GraveKeeper.logger.warn(String.format("Can't find free slot for grave, deleting %s with tile entity %s at %s",
        blockState, tileEntity, worldPositionResult.blockPos ));
    if (tileEntity != null) {
      try {
        final NBTTagCompound nbtTagCompound = tileEntity.writeToNBT(new NBTTagCompound());
        GraveKeeper.logger.info(String.format("NBT is %s",
            nbtTagCompound));
      } catch(final Exception exception) {
        exception.printStackTrace(GraveKeeper.printStreamError);
      }
    }
    return worldPositionResult;
  }
  
  private static void updateWithBedOrSpawn(@Nonnull final EntityPlayer entityPlayer, @Nonnull final WorldPosition worldPositionResult) {
    BlockPos blockPosBed = entityPlayer.getBedLocation(entityPlayer.dimension);
    if (blockPosBed != null) {// (bed found in current world)
      worldPositionResult.blockPos = blockPosBed.up().south();
      
    } else {// (no bed defined in current dimension)
      // try spawn dimension
      final World world = FMLCommonHandler.instance().getMinecraftServerInstance().getWorld(GraveKeeperConfig.SPAWN_DIMENSION_ID);
      if (world != null) {
        // try bed location in spawn dimension
        blockPosBed = entityPlayer.getBedLocation(GraveKeeperConfig.SPAWN_DIMENSION_ID);
        worldPositionResult.setWorld(world);
        if (blockPosBed != null) {// (bed found in spawn dimension)
          worldPositionResult.blockPos = blockPosBed.up().south();
        } else {// (still no bed location)
          // use spawn location, its always defined, worse case it'll be at the center of the world border
          worldPositionResult.blockPos = worldPositionResult.getWorld().getSpawnPoint().up();
        }
        
      } else {
        GraveKeeper.logger.warn(String.format("Failed to load spawn dimension with id %s, check console for details and review your configuration accordingly.",
                                               GraveKeeperConfig.SPAWN_DIMENSION_ID));
      }
    }
  }

  // note: only updates worldPositionResult when returning true
  private static boolean updateWithNearbyFreeSpot(@Nonnull final WorldPosition worldPositionResult) {
    if (GraveKeeperConfig.DEBUG_LOGS) {
      GraveKeeper.logger.info(String.format("Starting position is %s, searching nearby",
          worldPositionResult.blockPos ));
    }
    
    // compute search area
    final int xMin = worldPositionResult.blockPos.getX() - GraveKeeperConfig.SEARCH_RADIUS_HORIZONTAL_M;
    final int xMax = worldPositionResult.blockPos.getX() + GraveKeeperConfig.SEARCH_RADIUS_HORIZONTAL_M;
    final int yMin = Math.max(worldPositionResult.blockPos.getY() - GraveKeeperConfig.SEARCH_RADIUS_BELOW_M,
                              GraveKeeperConfig.SEARCH_MIN_ALTITUDE);
    final int yMax = Math.min(worldPositionResult.blockPos.getY() + GraveKeeperConfig.SEARCH_RADIUS_ABOVE_M,
                              worldPositionResult.getWorld().getHeight() - 1 );
    final int zMin = worldPositionResult.blockPos.getZ() - GraveKeeperConfig.SEARCH_RADIUS_HORIZONTAL_M;
    final int zMax = worldPositionResult.blockPos.getZ() + GraveKeeperConfig.SEARCH_RADIUS_HORIZONTAL_M;
    
    // search vertically first
    final Vec3d vStarting = new Vec3d(
        worldPositionResult.blockPos.getX() + 0.5D,
        worldPositionResult.blockPos.getY() + 0.5D,
        worldPositionResult.blockPos.getZ() + 0.5D);

    final MutableBlockPos mutableBlockPos = new MutableBlockPos(worldPositionResult.blockPos);
    BlockPos blockPosVisible = worldPositionResult.blockPos;
    BlockPos blockPosHidden = worldPositionResult.blockPos;
    int distanceClosestVisible = Integer.MAX_VALUE;
    int distanceClosestHidden = Integer.MAX_VALUE;
    for (int x = xMin; x <= xMax; x++) {
      if (Math.abs(x) > Math.max(distanceClosestVisible, distanceClosestHidden)) {
        continue;
      }
      for (int z = zMin; z <= zMax; z++) {
        if (Math.abs(z) > Math.max(distanceClosestVisible, distanceClosestHidden)) {
          continue;
        }
        for (int y = yMin; y <= yMax; y++) {
          if (Math.abs(y) > Math.max(distanceClosestVisible, distanceClosestHidden)) {
            continue;
          }
          mutableBlockPos.setPos(x, y, z);
          if (isFreeSpot(worldPositionResult.getWorld(), mutableBlockPos, true, false)) {
            final int distanceCurrent = (int) Math.round(mutableBlockPos.distanceSqToCenter(
                worldPositionResult.blockPos.getX() + 0.5D,
                worldPositionResult.blockPos.getY() + 0.5D,
                worldPositionResult.blockPos.getZ() + 0.5D ));

            // check for visibility
            final Vec3d vTarget = new Vec3d(
                mutableBlockPos.getX() + 0.5D,
                mutableBlockPos.getY() + 0.5D,
                mutableBlockPos.getZ() + 0.5D);
            final RayTraceResult rayTraceResult = worldPositionResult.getWorld().rayTraceBlocks(vStarting, vTarget);
            final boolean isHidden = rayTraceResult == null || !rayTraceResult.getBlockPos().equals(mutableBlockPos);
            if (isHidden) {
              if (distanceCurrent < distanceClosestHidden) {
                if (GraveKeeperConfig.DEBUG_LOGS) {
                  GraveKeeper.logger.info(String.format("New hidden free spot is closer: %d -> %d",
                      distanceClosestHidden, distanceCurrent ));
                }
                distanceClosestHidden = distanceCurrent;
                blockPosHidden = mutableBlockPos.toImmutable();
              }

            } else if (distanceCurrent < distanceClosestVisible) {
              if (GraveKeeperConfig.DEBUG_LOGS) {
                GraveKeeper.logger.info(String.format("New visible free spot is closer: %d -> %d",
                    distanceClosestVisible, distanceCurrent ));
              }
              distanceClosestVisible = distanceCurrent;
              blockPosVisible = mutableBlockPos.toImmutable();
            }
          }
        }
      }
    }

    // return the closest visible, then closest hidden location
    if (distanceClosestVisible != Integer.MAX_VALUE) {
      if (GraveKeeperConfig.DEBUG_LOGS) {
        GraveKeeper.logger.info(String.format("Found closest visible block %d m away at (%d %d %d)",
            distanceClosestVisible,
            blockPosVisible.getX(), blockPosVisible.getY(), blockPosVisible.getZ() ));
      }
      worldPositionResult.blockPos = blockPosVisible;
      return true;
    }
    if (distanceClosestHidden != Integer.MAX_VALUE) {
      if (GraveKeeperConfig.DEBUG_LOGS) {
        GraveKeeper.logger.info(String.format("Found closest hidden block %d m away at (%d %d %d)",
            distanceClosestHidden,
            blockPosHidden.getX(), blockPosHidden.getY(), blockPosHidden.getZ() ));
      }
      worldPositionResult.blockPos = blockPosHidden;
      return true;
    }
    return false;
  }

  // note: only updates worldPositionResult when returning true
  private static boolean updateVertically(@Nonnull final WorldPosition worldPositionResult, final boolean isFlying) {
    if (GraveKeeperConfig.DEBUG_LOGS) {
      GraveKeeper.logger.info(String.format("Starting position is %s, searching %s",
          worldPositionResult.blockPos, isFlying ? "down below" : "up above" ));
    }

    final MutableBlockPos mutableBlockPos = new MutableBlockPos(worldPositionResult.blockPos);
    int y = worldPositionResult.blockPos.getY();
    while (y >= GraveKeeperConfig.SEARCH_MIN_ALTITUDE
         && y < worldPositionResult.getWorld().getHeight() - 1 ) {
      mutableBlockPos.setY(y);
      if (isFreeSpot(worldPositionResult.getWorld(), mutableBlockPos, true, true)) {
        if (GraveKeeperConfig.DEBUG_LOGS) {
          GraveKeeper.logger.info(String.format("Found vertical block at (%d %d %d)",
              mutableBlockPos.getX(), mutableBlockPos.getY(), mutableBlockPos.getZ() ));
        }
        worldPositionResult.blockPos = mutableBlockPos.toImmutable();
        return true;
      }
      if (isFlying) {
        y--;
      } else {
        y++;
      }
    }
    return false;
  }

  private static boolean isFreeSpot(@Nonnull final World world, @Nonnull final BlockPos blockPos,
      final boolean shouldCheckBase, final boolean allowLiquidBase) {
    // ignore bottom (we might need a solid block below) and top of world (some mods are using it)
    if ( blockPos.getY() < GraveKeeperConfig.SEARCH_MIN_ALTITUDE
      || blockPos.getY() > world.getHeight() - 1 ) {
      assert false;
      return false;
    }

    // require base block
    final IBlockState blockStateDown = world.getBlockState(blockPos.down());
    if ( shouldCheckBase
      && !blockStateDown.getMaterial().isSolid()
      && ( !allowLiquidBase
        || !blockStateDown.getMaterial().isLiquid() ) ) {
      return false;
    }
    
    // always exclude fluids
    final IBlockState blockState = world.getBlockState(blockPos);
    if (blockState.getMaterial().isLiquid()) {
      return false;
    }
    
    // accept air or replaceable
    final boolean isAirOrReplaceable = ( blockState.getBlock().isAir(blockState, world, blockPos)
                                    || blockState.getBlock().isReplaceable(world, blockPos) );
    if ( isAirOrReplaceable
      && GraveKeeperConfig.DEBUG_LOGS ) {
      GraveKeeper.logger.info(String.format("Found free spot at (%d %d %d)",
          blockPos.getY(), blockPos.getY(), blockPos.getZ()));
    }
    return isAirOrReplaceable;
  }

}