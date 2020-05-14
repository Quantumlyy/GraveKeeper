package com.quantumlytangled.deathchests.core;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;

import java.util.function.Predicate;


public final class CustomEntitySelectors {
    public static final Predicate<Entity> IN_CREATIVE = entity -> !(entity instanceof EntityPlayer) || !((EntityPlayer) entity).isSpectator() && ((EntityPlayer) entity).isCreative();
}
