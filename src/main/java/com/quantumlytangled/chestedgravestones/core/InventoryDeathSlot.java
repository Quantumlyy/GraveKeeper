package com.quantumlytangled.chestedgravestones.core;

import net.minecraft.item.ItemStack;

public class InventoryDeathSlot {

  public final ItemStack content;
  public final int slot;
  public final InventoryType type;

  public InventoryDeathSlot(ItemStack content, int slot, InventoryType type) {
    this.content = content.copy();
    this.slot = slot;
    this.type = type;
  }

}
