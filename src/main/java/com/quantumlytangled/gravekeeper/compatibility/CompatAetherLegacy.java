package com.quantumlytangled.gravekeeper.compatibility;

import com.legacy.aether.api.AetherAPI;
import com.legacy.aether.api.player.IPlayerAether;
import com.legacy.aether.api.player.util.IAccessoryInventory;
import com.quantumlytangled.gravekeeper.util.InventoryType;
import javax.annotation.Nonnull;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

public class CompatAetherLegacy implements ICompatInventory {
  
  private static final CompatAetherLegacy INSTANCE = new CompatAetherLegacy();
  
  public static CompatAetherLegacy getInstance() {
    return INSTANCE;
  }
  
  @Override
  public InventoryType getType() {
    return InventoryType.AETHER_LEGACY;
  }
  
  @Override
  public NonNullList<ItemStack> getAllContents(@Nonnull final EntityPlayerMP player) {
    IPlayerAether playerAether = AetherAPI.getInstance().get(player);
    final IAccessoryInventory accessoryInventory = playerAether.getAccessoryInventory();
    final NonNullList<ItemStack> invContents = NonNullList.withSize(accessoryInventory.getSizeInventory(), ItemStack.EMPTY);
    for (int index = 0; index < accessoryInventory.getSizeInventory(); index++) {
      invContents.set(index, accessoryInventory.getStackInSlot(index));
    }
    
    return invContents;
  }
  
  @Override
  public void removeItem(@Nonnull final EntityPlayerMP player, final int slot) {
    IPlayerAether playerAether = AetherAPI.getInstance().get(player);
    final IAccessoryInventory accessoryInventory = playerAether.getAccessoryInventory();
    accessoryInventory.setInventorySlotContents(slot, ItemStack.EMPTY);
  }
  
  @Override
  public ItemStack setItemReturnOverflow(@Nonnull final EntityPlayerMP player, final int slot, @Nonnull final ItemStack itemStack) {
    IPlayerAether playerAether = AetherAPI.getInstance().get(player);
    final IAccessoryInventory accessoryInventory = playerAether.getAccessoryInventory();
    if ( slot >= 0
      && slot < accessoryInventory.getSizeInventory()
      && accessoryInventory.getStackInSlot(slot).isEmpty()
      && accessoryInventory.isItemValidForSlot(slot, itemStack) ) {
      accessoryInventory.setInventorySlotContents(slot, itemStack);
      return ItemStack.EMPTY;
    }
    return itemStack;
  }
}