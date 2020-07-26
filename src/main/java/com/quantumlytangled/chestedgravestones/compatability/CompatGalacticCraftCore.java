package com.quantumlytangled.chestedgravestones.compatability;

import javax.annotation.Nonnull;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

import com.quantumlytangled.chestedgravestones.core.InventoryType;
import micdoodle8.mods.galacticraft.api.inventory.AccessInventoryGC;
import micdoodle8.mods.galacticraft.api.inventory.IInventoryGC;

public class CompatGalacticCraftCore implements ICompatInventory {
  
  private static final CompatGalacticCraftCore INSTANCE = new CompatGalacticCraftCore();
  
  public static CompatGalacticCraftCore getInstance() {
      return INSTANCE;
  }

  @Override
  public InventoryType getType() {
    return InventoryType.GALACTICRAFT;
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
  public void removeItem(@Nonnull final EntityPlayerMP player, final int slot) {
    final IInventoryGC inventory = AccessInventoryGC.getGCInventoryForPlayer(player);
    inventory.setInventorySlotContents(slot, ItemStack.EMPTY);
  }

  @Override
  public ItemStack setItemReturnOverflow(@Nonnull final EntityPlayerMP player, final int slot, @Nonnull final ItemStack itemStack) {
    final IInventoryGC inventory = AccessInventoryGC.getGCInventoryForPlayer(player);
    if ( slot >= 0
      && slot < inventory.getSizeInventory()
      && inventory.getStackInSlot(slot).isEmpty()
      && inventory.isItemValidForSlot(slot, itemStack) ) {
      inventory.setInventorySlotContents(slot, itemStack);
      return ItemStack.EMPTY;
    }
    return itemStack;
  }
}