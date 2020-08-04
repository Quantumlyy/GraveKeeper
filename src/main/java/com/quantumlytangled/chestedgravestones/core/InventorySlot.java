package com.quantumlytangled.chestedgravestones.core;

import javax.annotation.Nonnull;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class InventorySlot {

  public final ItemStack itemStack;
  public final int slot;
  public final InventoryType type;

  public InventorySlot(ItemStack itemStack, int slot, InventoryType type) {
    this.itemStack = itemStack.copy();
    this.slot = slot;
    this.type = type;
  }

  public InventorySlot(@Nonnull final NBTTagCompound tagSlot) {
    this.itemStack = new ItemStack(tagSlot);
    this.slot = tagSlot.getInteger("slot");
    this.type = InventoryType.valueOf(tagSlot.getString("type"));
  }

  @Nonnull
  public NBTTagCompound writeToNBT() {
    final NBTTagCompound tagInventorySlot = new NBTTagCompound();

    tagInventorySlot.setString("type", type.name());
    tagInventorySlot.setInteger("slot", slot);
    itemStack.writeToNBT(tagInventorySlot);
    
    return tagInventorySlot;
  }
}