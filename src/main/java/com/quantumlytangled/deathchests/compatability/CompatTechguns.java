package com.quantumlytangled.deathchests.compatability;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;


// TODO: Re-enable
public class CompatTechguns {

    public static NonNullList<ItemStack> getAllContents(EntityPlayer player) {
        /* TGPlayerInventory inventory = getTGInventory(player);

        NonNullList<ItemStack> invContents = NonNullList.withSize(inventory.getSizeInventory(), ItemStack.EMPTY);
        for (int i = 0; i < inventory.getSizeInventory(); i++)
            invContents.set(i, inventory.getStackInSlot(i));

        return invContents; */
        return NonNullList.withSize(1, ItemStack.EMPTY);
    }

    public static void setItem(int slot, ItemStack item, EntityPlayer player) {
        /* TGPlayerInventory inventory = getTGInventory(player);
        inventory.setInventorySlotContents(slot, item); */
    }

    public static boolean isSlotEmpty(int slot, EntityPlayer player) {
        /* TGPlayerInventory inventory = getTGInventory(player);
        return inventory.getStackInSlot(slot).isEmpty(); */
        return true;
    }

    public static void clearInventory(EntityPlayer player) {
        /* TGPlayerInventory inventory = getTGInventory(player);

        for (int i = 0; i < inventory.getSizeInventory(); ++i)
            inventory.setInventorySlotContents(i, ItemStack.EMPTY); */
    }

    /* private static TGPlayerInventory getTGInventory(EntityPlayer player) {
        return TGExtendedPlayer.get(player).tg_inventory;
    } */

}
