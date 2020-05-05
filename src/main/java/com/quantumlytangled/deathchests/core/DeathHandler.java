package com.quantumlytangled.deathchests.core;

import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import java.util.List;

public final class DeathHandler {

    public static void restoreVanillaInventory(NonNullList<ItemStack> contents, NonNullList<ItemStack> inventory, List<ItemStack> overflow) {
        for (int i = 0; i < contents.size(); ++i) {
            final ItemStack item = contents.get(i);
            if (item.isEmpty()) continue;
            if (inventory.get(i).isEmpty()) {
                inventory.set(i, item.copy());
                continue;
            }
            overflow.add(item);
        }
    }

}
