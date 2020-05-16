package com.quantumlytangled.deathchests.compatability;

import micdoodle8.mods.galacticraft.api.inventory.AccessInventoryGC;
import micdoodle8.mods.galacticraft.api.inventory.IInventoryGC;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

public class CompatGalacticCraftCore {

    public static NonNullList<ItemStack> getAllContents(EntityPlayerMP player) {
        IInventoryGC inventory = AccessInventoryGC.getGCInventoryForPlayer(player);

        NonNullList<ItemStack> invContents = NonNullList.withSize(inventory.getSizeInventory(), ItemStack.EMPTY);
        for (int i = 0; i < inventory.getSizeInventory(); i++)
            invContents.set(i, inventory.getStackInSlot(i));

        return invContents;
    }

    public static void setItem(int slot, ItemStack item, EntityPlayerMP player) {
        IInventoryGC inventory = AccessInventoryGC.getGCInventoryForPlayer(player);
        inventory.setInventorySlotContents(slot, item);
    }

    public static boolean isSlotEmpty(int slot, EntityPlayerMP player) {
        IInventoryGC inventory = AccessInventoryGC.getGCInventoryForPlayer(player);
        return inventory.getStackInSlot(slot).isEmpty();
    }

    public static void clearInventory(EntityPlayerMP player) {
        IInventoryGC inventory = AccessInventoryGC.getGCInventoryForPlayer(player);

        for (int i = 0; i < inventory.getSizeInventory(); ++i)
            inventory.setInventorySlotContents(i, ItemStack.EMPTY);
    }

}