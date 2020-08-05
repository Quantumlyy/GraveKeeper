package com.quantumlytangled.gravekeeper.core;

import javax.annotation.Nonnull;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class InventorySlot {
  
  public final ItemStack itemStack;
  public final int slot;
  public final InventoryType type;
  public boolean isSoulbound;
  
  public InventorySlot(@Nonnull final ItemStack itemStack, final int slot, @Nonnull final InventoryType type, final boolean isSoulbound) {
    this.itemStack = itemStack.copy();
    this.slot = slot;
    this.type = type;
    this.isSoulbound = isSoulbound;
  }
  
  public InventorySlot(@Nonnull final NBTTagCompound tagSlot) {
    this.itemStack = new ItemStack(tagSlot);
    this.slot = tagSlot.getInteger("slot");
    this.type = InventoryType.valueOf(tagSlot.getString("type"));
    this.isSoulbound = tagSlot.hasKey("isSoulbound") && tagSlot.getBoolean("isSoulbound");
  }
  
  @Nonnull
  public NBTTagCompound writeToNBT() {
    final NBTTagCompound tagInventorySlot = new NBTTagCompound();
    
    tagInventorySlot.setString("type", type.name());
    tagInventorySlot.setInteger("slot", slot);
    tagInventorySlot.setBoolean("isSoulbound", isSoulbound);
    itemStack.writeToNBT(tagInventorySlot);
    
    return tagInventorySlot;
  }
}