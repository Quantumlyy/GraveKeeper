package com.quantumlytangled.chestedgravestones.util;

import java.util.function.Predicate;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;


public final class CustomEntitySelectors {

  public static final Predicate<Entity> IN_CREATIVE = entity -> !(entity instanceof EntityPlayer)
      || !((EntityPlayer) entity).isSpectator() && ((EntityPlayer) entity).isCreative();
}
