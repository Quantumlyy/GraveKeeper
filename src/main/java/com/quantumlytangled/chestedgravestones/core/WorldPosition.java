package com.quantumlytangled.chestedgravestones.core;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class WorldPosition {

  public World world;
  public BlockPos blockPos;

  WorldPosition(final World world, final BlockPos blockPos) {
    this.world = world;
    this.blockPos = blockPos;
  }
  
}