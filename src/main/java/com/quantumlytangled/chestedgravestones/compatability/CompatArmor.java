package com.quantumlytangled.chestedgravestones.compatability;

import javax.annotation.Nonnull;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import com.quantumlytangled.chestedgravestones.core.InventoryType;

public class CompatArmor implements ICompatInventory {

  private static final CompatArmor INSTANCE = new CompatArmor();

  public static CompatArmor getInstance() {
    return INSTANCE;
  }

  @Override
  public InventoryType getType() {
    return InventoryType.ARMOUR;
  }

  @Override
  public NonNullList<ItemStack> getAllContents(@Nonnull final EntityPlayerMP player) {
    return player.inventory.armorInventory;
  }

  @Override
  public void removeItem(@Nonnull final EntityPlayerMP player, final int slot) {
    player.inventory.armorInventory.set(slot, ItemStack.EMPTY);
  }

  @Override
  public ItemStack setItemReturnOverflow(@Nonnull final EntityPlayerMP player, final int slot, @Nonnull final ItemStack itemStack) {
    if (player.inventory.armorInventory.get(slot).isEmpty()) {
      player.inventory.armorInventory.set(slot, itemStack);
      return ItemStack.EMPTY;
    }
    return itemStack;
  }

  @Override
  public boolean isSlotEmpty(@Nonnull final EntityPlayerMP player, final int slot) {
    return player.inventory.armorInventory.get(slot).isEmpty();
  }
}
