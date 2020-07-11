package com.quantumlytangled.chestedgravestones.util;

import com.quantumlytangled.chestedgravestones.core.InventoryDeathSlot;
import java.util.List;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

public class DeathHandler {

  public static void restoreInvSlot(InventoryDeathSlot slot, NonNullList<ItemStack> inventory,
      List<ItemStack> overflow) {
    if (inventory.get(slot.slot).isEmpty()) {
      inventory.set(slot.slot, slot.content.copy());
    } else {
      overflow.add(slot.content);
    }
  }
}
