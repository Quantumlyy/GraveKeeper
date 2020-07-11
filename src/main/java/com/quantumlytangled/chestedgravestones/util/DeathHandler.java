package com.quantumlytangled.chestedgravestones.util;

import com.quantumlytangled.chestedgravestones.core.InventoryDeathSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

import java.util.List;

public class DeathHandler {
    public static void restoreInvSlot(InventoryDeathSlot slot, NonNullList<ItemStack> inventory, List<ItemStack> overflow) {
            if (inventory.get(slot.slot).isEmpty()) inventory.set(slot.slot, slot.content.copy());
            else overflow.add(slot.content);
    }
}
