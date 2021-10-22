package com.quantumlytangled.gravekeeper.util;

import javax.annotation.Nonnull;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;

public class InventorySlot {
  
  public final ItemStack itemStack;
  public final int slot;
  public final InventoryType type;
  public boolean isSoulbound;
  
  public InventorySlot(@Nonnull final ItemStack itemStack, final int slot, @Nonnull final InventoryType type,
                       final boolean isSoulbound) {
    this.itemStack = itemStack.copy();
    this.slot = slot;
    this.type = type;
    this.isSoulbound = isSoulbound;
  }
  
  public InventorySlot(@Nonnull final CompoundNBT tagSlot) {
    this.itemStack = ItemStack.of(tagSlot);
    this.slot = tagSlot.getInt("slot");
    this.type = InventoryType.valueOf(tagSlot.getString("type"));
    this.isSoulbound = tagSlot.get("isSoulbound") != null && tagSlot.getBoolean("isSoulbound");
  }
  
  @Nonnull
  public CompoundNBT writeToNBT() {
    final CompoundNBT tagInventorySlot = new CompoundNBT();
    
    tagInventorySlot.putString("type", type.name());
    tagInventorySlot.putInt("slot", slot);
    if (isSoulbound) {
      tagInventorySlot.putBoolean("isSoulbound", true);
    }
    itemStack.save(tagInventorySlot);
    
    return tagInventorySlot;
  }
}
