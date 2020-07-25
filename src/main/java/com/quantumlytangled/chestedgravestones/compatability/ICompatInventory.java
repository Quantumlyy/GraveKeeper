package com.quantumlytangled.chestedgravestones.compatability;

import javax.annotation.Nonnull;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

public interface ICompatInventory {

  NonNullList<ItemStack> getAllContents(@Nonnull final EntityPlayerMP player);

  void setItem(@Nonnull final EntityPlayerMP player, final int slot, @Nonnull final ItemStack item);

  boolean isSlotEmpty(@Nonnull final EntityPlayerMP player, final int slot);
}
