package com.quantumlytangled.chestedgravestones.compatability;

import javax.annotation.Nonnull;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

import techguns.capabilities.TGExtendedPlayer;

public class CompatTechGuns implements ICompatInventory {

  private static final CompatTechGuns INSTANCE = new CompatTechGuns();

  public static CompatTechGuns getInstance() {
    return INSTANCE;
  }
  
  @Override
  public NonNullList<ItemStack> getAllContents(@Nonnull final EntityPlayerMP player) {
    final IInventory inventory = TGExtendedPlayer.get(player).tg_inventory;
    final NonNullList<ItemStack> itemStacks = NonNullList.withSize(inventory.getSizeInventory(), ItemStack.EMPTY);
    for (int index = 0; index < inventory.getSizeInventory(); index++) {
      itemStacks.set(index, inventory.getStackInSlot(index));
    }

    return itemStacks;
  }

  @Override
  public void setItem(@Nonnull final EntityPlayerMP player, final int slot, @Nonnull final ItemStack itemStack) {
    final IInventory inventory = TGExtendedPlayer.get(player).tg_inventory;
    inventory.setInventorySlotContents(slot, itemStack);
  }

  @Override
  public boolean isSlotEmpty(@Nonnull final EntityPlayerMP player, final int slot) {
    final IInventory inventory = TGExtendedPlayer.get(player).tg_inventory;
    return inventory.getStackInSlot(slot).isEmpty();
  }
}
