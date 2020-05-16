package com.quantumlytangled.deathchests.compatability;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import techguns.capabilities.TGExtendedPlayer;
import techguns.gui.player.TGPlayerInventory;

public class CompatTechguns {

    public static NonNullList<ItemStack> getAllContents(EntityPlayer player) {
        TGPlayerInventory inventory = getTGInventory(player);

        NonNullList<ItemStack> invContents = NonNullList.withSize(inventory.getSizeInventory(), ItemStack.EMPTY);
        for (int i = 0; i < inventory.getSizeInventory(); i++)
            invContents.set(i, inventory.getStackInSlot(i));

        return invContents;
    }

    public static void setItem(int slot, ItemStack item, EntityPlayer player) {
        TGPlayerInventory inventory = getTGInventory(player);
        inventory.setInventorySlotContents(slot, item);
    }

    public static boolean isSlotEmpty(int slot, EntityPlayer player) {
        TGPlayerInventory inventory = getTGInventory(player);
        return inventory.getStackInSlot(slot).isEmpty();
    }

    public static void clearInventory(EntityPlayer player) {
        TGPlayerInventory inventory = getTGInventory(player);

        for (int i = 0; i < inventory.getSizeInventory(); ++i)
            inventory.setInventorySlotContents(i, ItemStack.EMPTY);
    }

    private static TGPlayerInventory getTGInventory(EntityPlayer player) {
        return TGExtendedPlayer.get(player).tg_inventory;
    }

}
