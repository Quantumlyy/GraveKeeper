package com.quantumlytangled.chestedgravestones.util;

import javax.annotation.Nonnull;
import com.quantumlytangled.chestedgravestones.compatability.ICompatInventory;
import com.quantumlytangled.chestedgravestones.core.InventorySlot;
import com.quantumlytangled.chestedgravestones.core.InventoryType;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

public class InventoryHandler {

  public static ICompatInventory compatBaubles = null;
  public static ICompatInventory compatGalacticCraft = null;
  public static ICompatInventory compatTechGuns = null;

  @Nonnull
  public static List<InventorySlot> collectOnDeath(@Nonnull final EntityPlayerMP player) {
    final InventoryPlayer inventory = player.inventory;
    final List<InventorySlot> inventorySlots = new ArrayList<>();

    collectOnDeath(player, inventorySlots, inventory.armorInventory, InventoryType.ARMOUR);
    collectOnDeath(player, inventorySlots, inventory.offHandInventory, InventoryType.OFFHAND);
    collectOnDeath(player, inventorySlots, inventory.mainInventory, InventoryType.MAIN);

    if (compatBaubles != null) {
      collectOnDeath(player, inventorySlots, compatBaubles.getAllContents(player), InventoryType.BAUBLES);
    }
    if (compatGalacticCraft != null) {
      collectOnDeath(player, inventorySlots, compatGalacticCraft.getAllContents(player), InventoryType.GCC);
    }
    if (compatTechGuns != null) {
      collectOnDeath(player, inventorySlots, compatTechGuns.getAllContents(player), InventoryType.TECHGUNS);
    }
    
    return inventorySlots;
  }

  private static void collectOnDeath(@Nonnull final EntityPlayerMP player, @Nonnull final List<InventorySlot> inventorySlots, @Nonnull final NonNullList<ItemStack> itemStacks, @Nonnull final InventoryType inventoryType) {
    for (int index = 0; index < itemStacks.size(); index++) {
      if (itemStacks.get(index).isEmpty()) {
        continue;
      }
      final InventorySlot inventorySlot = new InventorySlot(itemStacks.get(index), index, inventoryType);
      inventorySlots.add(inventorySlot);
      switch (inventoryType) {
        case MAIN:
          player.inventory.mainInventory.set(index, ItemStack.EMPTY);
          break;
        case ARMOUR:
          player.inventory.armorInventory.set(index, ItemStack.EMPTY);
          break;
        case OFFHAND:
          player.inventory.offHandInventory.set(index, ItemStack.EMPTY);
          break;
        case BAUBLES:
          compatBaubles.setItem(player, index, ItemStack.EMPTY);
          break;
        case GCC:
          compatGalacticCraft.setItem(player, index, ItemStack.EMPTY);
          break;
        case TECHGUNS:
          compatTechGuns.setItem(player, index, ItemStack.EMPTY);
          break;
      }
    }
  }

  public static void restoreOrOverflow(@Nonnull final InventorySlot slot, @Nonnull final NonNullList<ItemStack> inventory, @Nonnull final List<ItemStack> overflow) {
    if (inventory.get(slot.slot).isEmpty()) {
      inventory.set(slot.slot, slot.itemStack.copy());
    } else {
      overflow.add(slot.itemStack);
    }
  }
}