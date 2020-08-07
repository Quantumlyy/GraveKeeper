package com.quantumlytangled.gravekeeper.util;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import com.quantumlytangled.gravekeeper.core.GraveKeeperConfig;
import com.quantumlytangled.gravekeeper.core.InventoryType;

public class SoulboundHandler {
  
  public enum Mode {
    NONE,
    ARMOUR_HELD,
    ARMOUR_HOTBAR,
    FULL
  }
  
  public static Mode updateMode(@Nullable final Mode modeCurrent, @Nonnull final ItemStack itemStack) {
    final Mode modeItem;
    if (GraveKeeperConfig.SOULBOUND_CHARM_ARMOR_HELD_ITEMS.contains(itemStack.getItem())) {
      modeItem = Mode.ARMOUR_HELD;
    } else if (GraveKeeperConfig.SOULBOUND_CHARM_ARMOR_HOTBAR_ITEMS.contains(itemStack.getItem())) {
      modeItem = Mode.ARMOUR_HOTBAR;
    } else if (GraveKeeperConfig.SOULBOUND_CHARM_FULL_ITEMS.contains(itemStack.getItem())) {
      modeItem = Mode.FULL;
    } else {
      modeItem = Mode.NONE;
    }
    return modeCurrent != null && modeCurrent.ordinal() > modeItem.ordinal() ? modeCurrent : modeItem;
  }
  
  public static boolean isSoulbinded(@Nonnull final Mode mode, final int slotHeld,
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
        if ( inventoryType == InventoryType.MAIN
          && slot < 9 ) {
          return true;
        }
        break;
        
      case FULL:
        return true;
    }
    // check for charm itself
    if (GraveKeeperConfig.SOULBOUND_CHARM_ITEMS.contains(itemStack.getItem())) {
      return true;
    }
    
    // check for enchantments
    final Map<Enchantment, Integer> enchantments = EnchantmentHelper.getEnchantments(itemStack);
    for (final Enchantment enchantment : enchantments.keySet()) {
      if (enchantment != null) {
        if ( GraveKeeperConfig.ANY_ENCHANT_IS_SOULBOUND
          || GraveKeeperConfig.SOULBOUND_ENCHANTMENTS.contains(enchantment) ) {
          return true;
        }
      }
    }
    
    // check for NBT tags
    final NBTTagCompound tagCompound = itemStack.getTagCompound();
    if (tagCompound != null) {
      for (final String tagBoolean : GraveKeeperConfig.SOULBOUND_TAG_BOOLEAN) {
        if ( tagCompound.hasKey(tagBoolean)
          && tagCompound.getBoolean(tagBoolean) ) {
          return true;
        }
      }
    }
    return false;
  }

}
