package dev.quantumly.gravekeeper.compatibility;

import dev.quantumly.gravekeeper.util.InventoryType;

import javax.annotation.Nonnull;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

public class CompatMain implements ICompatInventory {
	
	private static final CompatMain INSTANCE = new CompatMain();
  
  public static CompatMain getInstance() {
    return INSTANCE;
  }
  
  @Override
  public InventoryType getType() {
    return InventoryType.MAIN;
  }
  
  @Override
  public NonNullList<ItemStack> getAllContents(@Nonnull final ServerPlayerEntity player) {
    return player.inventory.items;
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
