package com.quantumlytangled.gravekeeper.util;

import com.quantumlytangled.gravekeeper.core.GraveKeeperConfig;
import com.quantumlytangled.gravekeeper.core.InventoryType;
import javax.annotation.Nonnull;
import net.minecraft.item.ItemStack;

public class CharmHandler {
  
  public enum Mode {
    NONE,
    ARMOUR_HELD,
    ARMOUR_HOTBAR,
    FULL
  }
  
  public static Mode updateMode(@Nonnull final Mode modeCurrent, @Nonnull final ItemStack itemStack) {
    final Mode modeItem;
    if (GraveKeeperConfig.CHARM_ARMOR_HELD_ITEMS.contains(itemStack.getItem())) {
      modeItem = Mode.ARMOUR_HELD;
    } else if (GraveKeeperConfig.CHARM_ARMOR_HOTBAR_ITEMS.contains(itemStack.getItem())) {
      modeItem = Mode.ARMOUR_HOTBAR;
    } else if (GraveKeeperConfig.CHARM_FULL_ITEMS.contains(itemStack.getItem())) {
      modeItem = Mode.FULL;
    } else {
      modeItem = Mode.NONE;
    }
    return modeCurrent.ordinal() > modeItem.ordinal() ? modeCurrent : modeItem;
  }
  
  public static boolean isCharmed(@Nonnull final Mode mode, final int slotHeld,
      @Nonnull final InventoryType inventoryType, final int slot, @Nonnull final ItemStack itemStack) {
    // check for charm effects
    switch (mode) {
      case NONE:
      default:
        break;
        
      case ARMOUR_HELD:
        if (inventoryType == InventoryType.ARMOUR) {
          return true;
        }
        if ( inventoryType == InventoryType.MAIN
          && slot == slotHeld ) {
          return true;
        }
        break;
        
      case ARMOUR_HOTBAR:
        if (inventoryType == InventoryType.ARMOUR) {
          return true;
        }
        if ( inventoryType == InventoryType.MAIN
          && slot < 9 ) {
          return true;
        }
        break;
        
      case FULL:
        if ( inventoryType == InventoryType.ARMOUR
          || inventoryType == InventoryType.OFFHAND
          || inventoryType == InventoryType.MAIN
          || inventoryType == InventoryType.BAUBLES ) {
          return true;
        }
        break;
    }
    // check for charm itself
    return ( inventoryType == InventoryType.MAIN
          || inventoryType == InventoryType.BAUBLES )
        && GraveKeeperConfig.CHARM_ITEMS.contains(itemStack.getItem());
  }
  
}