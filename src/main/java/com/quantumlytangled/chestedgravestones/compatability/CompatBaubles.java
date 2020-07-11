package com.quantumlytangled.chestedgravestones.compatability;

import baubles.api.BaublesApi;
import baubles.api.cap.IBaublesItemHandler;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

public class CompatBaubles implements ICompatInventory {

  private static final CompatBaubles _INSTANCE = new CompatBaubles();

  public static CompatBaubles INSTANCE() {
    return _INSTANCE;
  }

  public NonNullList<ItemStack> getAllContents(EntityPlayerMP player) {
    IBaublesItemHandler handler = BaublesApi.getBaublesHandler(player);

    NonNullList<ItemStack> invContents = NonNullList.withSize(handler.getSlots(), ItemStack.EMPTY);
    for (int i = 0; i < handler.getSlots(); i++) {
      invContents.set(i, handler.getStackInSlot(i));
    }

    return invContents;
  }

  public void setItem(int slot, ItemStack item, EntityPlayerMP player) {
    IBaublesItemHandler handler = BaublesApi.getBaublesHandler(player);
    handler.setStackInSlot(slot, item);
  }

  public boolean isSlotEmpty(int slot, EntityPlayerMP player) {
    IBaublesItemHandler handler = BaublesApi.getBaublesHandler(player);
    return handler.getStackInSlot(slot).isEmpty();
  }

  public void clearInventory(EntityPlayerMP player) {
    IBaublesItemHandler handler = BaublesApi.getBaublesHandler(player);

    for (int i = 0; i < handler.getSlots(); ++i) {
      handler.setStackInSlot(i, ItemStack.EMPTY);
    }
  }

}
