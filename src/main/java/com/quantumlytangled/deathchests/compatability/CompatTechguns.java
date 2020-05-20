package com.quantumlytangled.deathchests.compatability;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;


// TODO: Re-enable
public class CompatTechguns implements ICompatInventory {

    private static final CompatTechguns _INSTANCE = new CompatTechguns();

    public static CompatTechguns INSTANCE() {
        return _INSTANCE;
    }

    public NonNullList<ItemStack> getAllContents(EntityPlayerMP player) {
        /*TGPlayerInventory inventory = getTGInventory(player);

        NonNullList<ItemStack> invContents = NonNullList.withSize(inventory.getSizeInventory(), ItemStack.EMPTY);
        for (int i = 0; i < inventory.getSizeInventory(); i++)
            invContents.set(i, inventory.getStackInSlot(i));

        return invContents;*/
        return NonNullList.withSize(15, ItemStack.EMPTY);
    }

    public void setItem(int slot, ItemStack item, EntityPlayerMP player) {
        /* TGPlayerInventory inventory = getTGInventory(player);
        inventory.setInventorySlotContents(slot, item); */
    }

    public boolean isSlotEmpty(int slot, EntityPlayerMP player) {
        /* TGPlayerInventory inventory = getTGInventory(player);
        return inventory.getStackInSlot(slot).isEmpty(); */
        return true;
    }

    public void clearInventory(EntityPlayerMP player) {
        /*TGPlayerInventory inventory = getTGInventory(player);

        for (int i = 0; i < inventory.getSizeInventory(); ++i)
            inventory.setInventorySlotContents(i, ItemStack.EMPTY); */
    }

    /* private static TGPlayerInventory getTGInventory(EntityPlayer player) {
        return TGExtendedPlayer.get(player).tg_inventory;
    } */

}
