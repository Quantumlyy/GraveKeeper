package com.quantumlytangled.chestedgravestones.compatability;

import javax.annotation.Nonnull;
import baubles.api.BaublesApi;
import baubles.api.cap.IBaublesItemHandler;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

public class CompatBaubles implements ICompatInventory {

  private static final CompatBaubles INSTANCE = new CompatBaubles();

  public static CompatBaubles getInstance() {
    return INSTANCE;
  }

  @Override
  public NonNullList<ItemStack> getAllContents(@Nonnull final EntityPlayerMP player) {
    final IBaublesItemHandler handler = BaublesApi.getBaublesHandler(player);
    final NonNullList<ItemStack> invContents = NonNullList.withSize(handler.getSlots(), ItemStack.EMPTY);
    for (int index = 0; index < handler.getSlots(); index++) {
      invContents.set(index, handler.getStackInSlot(index));
    }

    return invContents;
  }

  @Override
  public void setItem(@Nonnull final EntityPlayerMP player, final int slot, @Nonnull final ItemStack itemStack) {
    final IBaublesItemHandler handler = BaublesApi.getBaublesHandler(player);
    handler.setStackInSlot(slot, itemStack);
  }

  @Override
  public boolean isSlotEmpty(@Nonnull final EntityPlayerMP player, final int slot) {
    final IBaublesItemHandler handler = BaublesApi.getBaublesHandler(player);
    return handler.getStackInSlot(slot).isEmpty();
  }
}