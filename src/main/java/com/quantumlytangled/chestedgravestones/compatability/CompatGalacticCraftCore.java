package com.quantumlytangled.chestedgravestones.compatability;

import javax.annotation.Nonnull;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

import micdoodle8.mods.galacticraft.api.inventory.AccessInventoryGC;
import micdoodle8.mods.galacticraft.api.inventory.IInventoryGC;

public class CompatGalacticCraftCore implements ICompatInventory {
  
  private static final CompatGalacticCraftCore INSTANCE = new CompatGalacticCraftCore();
  
  public static CompatGalacticCraftCore getInstance() {
      return INSTANCE;
  }
  
  @Override
  public NonNullList<ItemStack> getAllContents(@Nonnull final EntityPlayerMP player) {
    final IInventoryGC inventory = AccessInventoryGC.getGCInventoryForPlayer(player);
    final NonNullList<ItemStack> invContents = NonNullList.withSize(inventory.getSizeInventory(), ItemStack.EMPTY);
    for (int i = 0; i < inventory.getSizeInventory(); i++) {
      invContents.set(i, inventory.getStackInSlot(i));
    }
    
    return invContents;
  }
  
  @Override
  public void setItem(@Nonnull final EntityPlayerMP player, final int slot, @Nonnull final ItemStack item) {
    final IInventoryGC inventory = AccessInventoryGC.getGCInventoryForPlayer(player);
    inventory.setInventorySlotContents(slot, item);
  }
  
  @Override
  public boolean isSlotEmpty(@Nonnull final EntityPlayerMP player, final int slot) {
    final IInventoryGC inventory = AccessInventoryGC.getGCInventoryForPlayer(player);
    return inventory.getStackInSlot(slot).isEmpty();
  }
}