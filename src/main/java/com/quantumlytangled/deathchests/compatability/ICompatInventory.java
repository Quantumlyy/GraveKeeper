package com.quantumlytangled.deathchests.compatability;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

public interface ICompatInventory {

    static ICompatInventory INSTANCE() {
        return null;
    }

    NonNullList<ItemStack> getAllContents(EntityPlayerMP player);

    void setItem(int slot, ItemStack item, EntityPlayerMP player);

    boolean isSlotEmpty(int slot, EntityPlayerMP player);

    void clearInventory(EntityPlayerMP player);
}
