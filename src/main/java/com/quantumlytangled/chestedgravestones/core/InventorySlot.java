package com.quantumlytangled.chestedgravestones.core;

import net.minecraft.item.ItemStack;

public class InventorySlot {

  public final ItemStack itemStack;
  public final int slot;
  public final InventoryType type;

  public InventorySlot(ItemStack itemStack, int slot, InventoryType type) {
    this.itemStack = itemStack.copy();
    this.slot = slot;
    this.type = type;
  }

}