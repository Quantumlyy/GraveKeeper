package dev.quantumly.gravekeeper.compatibility;

import dev.quantumly.gravekeeper.util.InventoryType;

import javax.annotation.Nonnull;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

public class CompatOffHand implements ICompatInventory {
  
  private static final CompatOffHand INSTANCE = new CompatOffHand();
  
  public static CompatOffHand getInstance() {
    return INSTANCE;
  }
  
  @Override
  public InventoryType getType() {
    return InventoryType.OFFHAND;
  }
  
  @Override
  public NonNullList<ItemStack> getAllContents(@Nonnull final ServerPlayerEntity player) {
    return player.inventory.offhand;
  }
  
  @Override
  public void removeItem(@Nonnull final ServerPlayerEntity player, final int slot) {
    getAllContents(player).set(slot, ItemStack.EMPTY);
  }
  
  @Override
  public ItemStack setItemReturnOverflow(@Nonnull final ServerPlayerEntity player, final int slot, @Nonnull final ItemStack itemStack) {
    if ( slot >= 0
      && slot < getAllContents(player).size()
      && getAllContents(player).get(slot).isEmpty()
      /* items are always valid */ ) {
      getAllContents(player).set(slot, itemStack);
      return ItemStack.EMPTY;
    }
    return itemStack;
  }
  
}
