package com.quantumlytangled.gravekeeper.compatability;

import javax.annotation.Nonnull;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import com.quantumlytangled.gravekeeper.util.InventoryType;

public interface ICompatInventory {

  InventoryType getType();
  
  NonNullList<ItemStack> getAllContents(@Nonnull final EntityPlayerMP player);

  void removeItem(@Nonnull final EntityPlayerMP player, final int slot);

  ItemStack setItemReturnOverflow(@Nonnull final EntityPlayerMP player, final int slot, @Nonnull final ItemStack item);
}
