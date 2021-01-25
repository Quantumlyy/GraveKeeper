package com.quantumlytangled.gravekeeper.compatibility;

import com.quantumlytangled.gravekeeper.util.InventoryType;
import lain.mods.cos.CosmeticArmorReworked;
import javax.annotation.Nonnull;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

public class CompatCosmeticArmorReworked implements ICompatInventory {
  
  private static final CompatCosmeticArmorReworked INSTANCE = new CompatCosmeticArmorReworked();
  
  public static CompatCosmeticArmorReworked getInstance() {
    return INSTANCE;
  }
  
  @Override
  public InventoryType getType() {
    return InventoryType.COSMETIC_ARMOR_REWORKED;
  }
  
  @Override
  public NonNullList<ItemStack> getAllContents(@Nonnull final EntityPlayerMP player) {
    final IInventory inventoryCosArmor = CosmeticArmorReworked.invMan.getCosArmorInventory(player.getUniqueID());
    final NonNullList<ItemStack> invContents = NonNullList.withSize(inventoryCosArmor.getSizeInventory(), ItemStack.EMPTY);
    for (int index = 0; index < inventoryCosArmor.getSizeInventory(); index++) {
      invContents.set(index, inventoryCosArmor.getStackInSlot(index));
    }
    
    return invContents;
  }
  
  @Override
  public void removeItem(@Nonnull final EntityPlayerMP player, final int slot) {
    final IInventory inventoryCosArmor = CosmeticArmorReworked.invMan.getCosArmorInventory(player.getUniqueID());
    inventoryCosArmor.setInventorySlotContents(slot, ItemStack.EMPTY);
  }
  
  @Override
  public ItemStack setItemReturnOverflow(@Nonnull final EntityPlayerMP player, final int slot, @Nonnull final ItemStack itemStack) {
    final IInventory inventoryCosArmor = CosmeticArmorReworked.invMan.getCosArmorInventory(player.getUniqueID());
    if ( slot >= 0
      && slot < inventoryCosArmor.getSizeInventory()
      && inventoryCosArmor.getStackInSlot(slot).isEmpty()
      && inventoryCosArmor.isItemValidForSlot(slot, itemStack) ) {
      inventoryCosArmor.setInventorySlotContents(slot, itemStack);
      return ItemStack.EMPTY;
    }
    return itemStack;
  }
}